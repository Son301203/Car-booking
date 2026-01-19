"""
API Service for Customer Clustering Prediction
Provides endpoints for automatic customer grouping based on trained ML model
"""

import pickle
import numpy as np
import pandas as pd
from flask import Flask, request, jsonify
from flask_cors import CORS
from datetime import datetime
import os
import json

app = Flask(__name__)
CORS(app)

# Paths to model files
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MODEL_DIR = os.path.join(BASE_DIR, 'data', 'models')
KMEANS_MODEL_PATH = os.path.join(MODEL_DIR, 'kmeans_model.pkl')
SCALER_PATH = os.path.join(MODEL_DIR, 'scaler.pkl')
CONFIG_PATH = os.path.join(MODEL_DIR, 'train_config.json')

# Global variables
kmeans_model = None
scaler = None
config = None

def load_model():
    """Load trained model and scaler"""
    global kmeans_model, scaler, config

    try:
        with open(KMEANS_MODEL_PATH, 'rb') as f:
            kmeans_model = pickle.load(f)

        with open(SCALER_PATH, 'rb') as f:
            scaler = pickle.load(f)

        with open(CONFIG_PATH, 'r') as f:
            config = json.load(f)

        print("✓ Model loaded successfully")
        print(f"  - Clusters: {config['n_clusters']}")
        print(f"  - Coord weight: {config['coord_weight']}")
        print(f"  - Time weight: {config['time_weight']}")
        return True
    except Exception as e:
        print(f"✗ Error loading model: {e}")
        return False

def parse_time_to_minutes(time_str):
    """Convert HH:mm string to minutes from midnight"""
    try:
        hour, minute = map(int, time_str.split(':'))
        return hour * 60 + minute
    except:
        return 0

def preprocess_orders(orders):
    """Preprocess order data for clustering"""
    df = pd.DataFrame(orders)

    # Extract features
    df['departure_time_minutes'] = df['departureTime'].apply(parse_time_to_minutes)

    # Create feature matrix
    features = df[[
        'pickup_coordinates_lat',
        'pickup_coordinates_lng',
        'departure_time_minutes'
    ]].values

    return df, features

def apply_clustering(features, coord_weight=1.0, time_weight=0.5):
    """Apply clustering with feature weighting"""
    # Scale features
    features_scaled = scaler.transform(features)

    # Apply weights
    features_scaled[:, 0] *= coord_weight  # lat
    features_scaled[:, 1] *= coord_weight  # lng
    features_scaled[:, 2] *= time_weight   # time

    # Predict clusters
    cluster_labels = kmeans_model.predict(features_scaled)

    return cluster_labels

def split_large_clusters(df_clustered, max_passengers=10):
    """Split clusters that exceed max passengers"""
    trips = []

    for cluster_id in df_clustered['cluster'].unique():
        cluster_data = df_clustered[df_clustered['cluster'] == cluster_id].copy()
        n_passengers = len(cluster_data)

        if n_passengers <= max_passengers:
            # Create single trip
            trips.append(create_trip(cluster_data, cluster_id, 0))
        else:
            # Split into multiple trips
            n_trips = (n_passengers + max_passengers - 1) // max_passengers

            # Sort by departure time for better grouping
            cluster_data = cluster_data.sort_values('departure_time_minutes')

            for i in range(n_trips):
                start_idx = i * max_passengers
                end_idx = min((i + 1) * max_passengers, n_passengers)
                trip_data = cluster_data.iloc[start_idx:end_idx]
                trips.append(create_trip(trip_data, cluster_id, i))

    return trips

def create_trip(trip_data, cluster_id, sub_trip_index):
    """Create trip object from cluster data"""
    # Calculate center coordinates
    center_lat = trip_data['pickup_coordinates_lat'].mean()
    center_lng = trip_data['pickup_coordinates_lng'].mean()

    # Calculate suggested departure time (median)
    median_time_minutes = int(trip_data['departure_time_minutes'].median())
    suggested_time = f"{median_time_minutes // 60:02d}:{median_time_minutes % 60:02d}"

    # Get departure date (assume all same date)
    departure_date = trip_data['departureDate'].iloc[0]

    # Get customer IDs
    customer_ids = trip_data['documentId'].tolist() if 'documentId' in trip_data else trip_data.index.tolist()

    return {
        'cluster_id': int(cluster_id),
        'sub_trip_index': sub_trip_index,
        'num_passengers': len(trip_data),
        'suggested_departure_time': suggested_time,
        'departure_date': departure_date,
        'center_lat': float(center_lat),
        'center_lng': float(center_lng),
        'customer_ids': customer_ids,
        'customer_details': trip_data.to_dict('records')
    }

@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({
        'status': 'ok',
        'model_loaded': kmeans_model is not None,
        'config': config
    })

@app.route('/api/cluster-customers', methods=['POST'])
def cluster_customers():
    """
    Cluster customers and generate trip suggestions

    Request body:
    {
        "orders": [
            {
                "documentId": "order_id",
                "clientId": "client_id",
                "departureDate": "dd/MM/yyyy",
                "departureTime": "HH:mm",
                "pickup_coordinates_lat": 20.xx,
                "pickup_coordinates_lng": 105.xx,
                "customerName": "Name",
                "customerPhone": "Phone"
            }
        ],
        "max_passengers": 10,  // Optional, default 10
        "coord_weight": 1.0,   // Optional, default from config
        "time_weight": 0.5     // Optional, default from config
    }

    Response:
    {
        "success": true,
        "trips": [
            {
                "cluster_id": 0,
                "sub_trip_index": 0,
                "num_passengers": 8,
                "suggested_departure_time": "08:30",
                "departure_date": "20/01/2026",
                "center_lat": 20.xx,
                "center_lng": 105.xx,
                "customer_ids": ["id1", "id2", ...],
                "customer_details": [...]
            }
        ],
        "total_customers": 50,
        "total_trips": 5
    }
    """
    try:
        data = request.json
        orders = data.get('orders', [])
        max_passengers = data.get('max_passengers', 10)
        coord_weight = data.get('coord_weight', config['coord_weight'])
        time_weight = data.get('time_weight', config['time_weight'])

        if not orders:
            return jsonify({
                'success': False,
                'error': 'No orders provided'
            }), 400

        # Preprocess
        df, features = preprocess_orders(orders)

        # Cluster
        cluster_labels = apply_clustering(features, coord_weight, time_weight)
        df['cluster'] = cluster_labels

        # Generate trips with max passenger constraint
        trips = split_large_clusters(df, max_passengers)

        return jsonify({
            'success': True,
            'trips': trips,
            'total_customers': len(orders),
            'total_trips': len(trips),
            'parameters': {
                'max_passengers': max_passengers,
                'coord_weight': coord_weight,
                'time_weight': time_weight
            }
        })

    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/predict-cluster', methods=['POST'])
def predict_cluster():
    """
    Predict cluster for a single order

    Request body:
    {
        "pickup_coordinates_lat": 20.xx,
        "pickup_coordinates_lng": 105.xx,
        "departureTime": "HH:mm"
    }
    """
    try:
        data = request.json
        lat = data.get('pickup_coordinates_lat')
        lng = data.get('pickup_coordinates_lng')
        time_str = data.get('departureTime')

        if lat is None or lng is None or time_str is None:
            return jsonify({
                'success': False,
                'error': 'Missing required fields'
            }), 400

        # Convert time to minutes
        time_minutes = parse_time_to_minutes(time_str)

        # Create feature array
        features = np.array([[lat, lng, time_minutes]])

        # Predict
        cluster_label = apply_clustering(features)[0]

        return jsonify({
            'success': True,
            'cluster_id': int(cluster_label)
        })

    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

if __name__ == '__main__':
    print("=" * 60)
    print("Customer Clustering API Service")
    print("=" * 60)

    # Load model
    if not load_model():
        print("\n⚠ Warning: Model not loaded. Please check model files.")
        exit(1)

    print("\nStarting server...")
    print("API Endpoints:")
    print("  - GET  /health")
    print("  - POST /api/cluster-customers")
    print("  - POST /api/predict-cluster")
    print("\n" + "=" * 60)

    app.run(host='0.0.0.0', port=5000, debug=True)

