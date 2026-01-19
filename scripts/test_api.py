"""
Test script for clustering API
Run this to verify the API is working correctly
"""

import requests
import json

# API base URL
BASE_URL = "http://localhost:5000"

def test_health():
    """Test health endpoint"""
    print("=" * 60)
    print("Testing Health Endpoint")
    print("=" * 60)

    try:
        response = requests.get(f"{BASE_URL}/health", timeout=5)
        print(f"Status Code: {response.status_code}")
        print(f"Response: {json.dumps(response.json(), indent=2)}")

        if response.status_code == 200 and response.json().get('model_loaded'):
            print("✅ Health check passed!")
            return True
        else:
            print("❌ Health check failed!")
            return False
    except Exception as e:
        print(f"❌ Error: {e}")
        print("\n⚠️ Make sure the API server is running:")
        print("   python cluster_prediction_api.py")
        return False

def test_clustering():
    """Test clustering endpoint with sample data"""
    print("\n" + "=" * 60)
    print("Testing Clustering Endpoint")
    print("=" * 60)

    # Sample orders (Hanoi to Quang Ninh)
    sample_data = {
        "orders": [
            {
                "documentId": "test_001",
                "clientId": "client_001",
                "departureDate": "20/01/2026",
                "departureTime": "08:00",
                "pickup_coordinates_lat": 21.0285,
                "pickup_coordinates_lng": 105.8542,
                "customerName": "Nguyễn Văn A",
                "customerPhone": "0912345678"
            },
            {
                "documentId": "test_002",
                "clientId": "client_002",
                "departureDate": "20/01/2026",
                "departureTime": "08:15",
                "pickup_coordinates_lat": 21.0295,
                "pickup_coordinates_lng": 105.8552,
                "customerName": "Trần Thị B",
                "customerPhone": "0912345679"
            },
            {
                "documentId": "test_003",
                "clientId": "client_003",
                "departureDate": "20/01/2026",
                "departureTime": "14:00",
                "pickup_coordinates_lat": 20.9500,
                "pickup_coordinates_lng": 105.8000,
                "customerName": "Lê Văn C",
                "customerPhone": "0912345680"
            },
            {
                "documentId": "test_004",
                "clientId": "client_004",
                "departureDate": "20/01/2026",
                "departureTime": "14:30",
                "pickup_coordinates_lat": 20.9510,
                "pickup_coordinates_lng": 105.8010,
                "customerName": "Phạm Thị D",
                "customerPhone": "0912345681"
            }
        ],
        "max_passengers": 10
    }

    try:
        print(f"Sending {len(sample_data['orders'])} orders...")
        response = requests.post(
            f"{BASE_URL}/api/cluster-customers",
            json=sample_data,
            timeout=10
        )

        print(f"Status Code: {response.status_code}")

        if response.status_code == 200:
            result = response.json()
            print(f"\n✅ Clustering successful!")
            print(f"Total customers: {result['total_customers']}")
            print(f"Total trips: {result['total_trips']}")
            print(f"\nSuggested Trips:")
            print("-" * 60)

            for i, trip in enumerate(result['trips'], 1):
                print(f"\nTrip {i}:")
                print(f"  - Cluster ID: {trip['cluster_id']}")
                print(f"  - Passengers: {trip['num_passengers']}")
                print(f"  - Departure Time: {trip['suggested_departure_time']}")
                print(f"  - Center: ({trip['center_lat']:.4f}, {trip['center_lng']:.4f})")
                print(f"  - Customer IDs: {', '.join(trip['customer_ids'])}")

            return True
        else:
            print(f"❌ Clustering failed!")
            print(f"Response: {response.text}")
            return False

    except Exception as e:
        print(f"❌ Error: {e}")
        return False

def main():
    print("\n🧪 BookCar Clustering API Test")
    print("=" * 60)

    # Test health first
    if not test_health():
        print("\n⛔ Cannot proceed. Please fix the health check issue.")
        return

    # Test clustering
    test_clustering()

    print("\n" + "=" * 60)
    print("✅ All tests completed!")
    print("=" * 60)

if __name__ == "__main__":
    main()

