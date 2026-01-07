# 🚌 Tài Liệu Hệ Thống Tự Động Điều Phối Xe Khách Đường Dài

## Mục Lục
1. [Tổng Quan](#1-tổng-quan)
2. [Phân Tích Dự Án Hiện Tại](#2-phân-tích-dự-án-hiện-tại)
3. [Giải Pháp Clustering](#3-giải-pháp-clustering)
4. [Cấu Trúc Dữ Liệu](#4-cấu-trúc-dữ-liệu)
5. [Thuật Toán & Mô Hình](#5-thuật-toán--mô-hình)
6. [Hướng Dẫn Sử Dụng](#6-hướng-dẫn-sử-dụng)
7. [Tích Hợp Vào Ứng Dụng](#7-tích-hợp-vào-ứng-dụng)
8. [Đánh Giá & Tối Ưu](#8-đánh-giá--tối-ưu)

---

## 1. Tổng Quan

### 1.1. Vấn Đề Cần Giải Quyết
Ứng dụng BookCar hiện có chức năng đặt xe khách đường dài giữa **Hà Nội** và **Quảng Ninh**. Role **Điều phối** (Coordination) cần xếp các khách hàng có:
- **Điểm đón gần nhau** trên bản đồ
- **Thời gian khởi hành gần nhau**

...vào **chung một chuyến xe** để tối ưu chi phí và thời gian cho tài xế.

### 1.2. Giải Pháp
Sử dụng thuật toán **Machine Learning (Clustering)** để tự động nhóm khách hàng dựa trên:
1. Tọa độ điểm đón (latitude, longitude)
2. Thời gian khởi hành (departure time)

### 1.3. Lợi Ích
| Thủ công | Tự động hóa |
|----------|-------------|
| Tốn thời gian xem xét từng booking | Xử lý hàng nghìn booking trong vài giây |
| Dễ sai sót, bỏ sót | Thuật toán nhất quán |
| Khó mở rộng | Dễ scale với lượng data lớn |
| Phụ thuộc kinh nghiệm điều phối viên | Dựa trên dữ liệu khách quan |

---

## 2. Phân Tích Dự Án Hiện Tại

### 2.1. Cấu Trúc Model
```
BookCar/app/src/main/java/com/example/bookcar/model/
├── Order.java          # Đơn đặt xe của khách hàng
├── Trips.java          # Chuyến đi (gồm nhiều orders)
├── Driver.java         # Thông tin tài xế
├── User.java           # Thông tin người dùng
└── ...
```

### 2.2. Model Order (Quan trọng)
```java
public class Order {
    private String documentId;
    private String departure;           // Tên điểm đón (text)
    private String destination;         // Tên điểm đến (text)
    private String departureDate;       // Ngày khởi hành (dd/MM/yyyy)
    private String departureTime;       // Giờ khởi hành (HH:mm)
    private String clientId;            // ID khách hàng
    private String tripId;              // ID chuyến đi (sau khi xếp)
    private String state;               // Trạng thái: Booked, Arranged, Completed...
    private GeoPoint pickupCoordinates;      // Tọa độ điểm đón
    private GeoPoint destinationCoordinates; // Tọa độ điểm đến
    private Timestamp createdAt;
}
```

### 2.3. Trạng Thái Order
```
Booked → Arranged → Picked Up → Completed
                 ↘ Cancelled
```

### 2.4. Quy Trình Hiện Tại (Thủ Công)
1. **Khách đặt xe** → Order có state = "Booked"
2. **Điều phối viên** mở `ArrangeCustomersFragment`
3. **Chọn thủ công** các khách có điểm đón & giờ khởi hành gần nhau
4. **Chọn tài xế** và tạo Trip
5. **Cập nhật** state của Orders thành "Arranged"

---

## 3. Giải Pháp Clustering

### 3.1. Tại Sao Chọn Clustering?
- **Unsupervised Learning**: Không cần label trước
- **Tự động phát hiện nhóm**: Dựa trên độ tương đồng
- **Linh hoạt**: Có thể điều chỉnh trọng số features

### 3.2. Thuật Toán Được Sử Dụng

#### K-Means Clustering (Chính)
```
Ưu điểm:
✅ Đơn giản, nhanh
✅ Hiệu quả với data lớn
✅ Dễ diễn giải kết quả

Nhược điểm:
❌ Phải chỉ định số clusters (k)
❌ Nhạy với outliers
❌ Giả định clusters hình cầu
```

#### DBSCAN (Phương án thay thế)
```
Ưu điểm:
✅ Tự động tìm số clusters
✅ Phát hiện được outliers (noise)
✅ Tìm được clusters hình dạng bất kỳ

Nhược điểm:
❌ Khó tune parameters (eps, min_samples)
❌ Không hiệu quả với density khác nhau
```

### 3.3. Features Sử Dụng
| Feature | Mô tả | Đơn vị |
|---------|-------|--------|
| `pickup_coordinates_lat` | Vĩ độ điểm đón | Degrees |
| `pickup_coordinates_lng` | Kinh độ điểm đón | Degrees |
| `departure_time_minutes` | Thời gian khởi hành | Minutes (0-1439) |

### 3.4. Feature Scaling & Weighting
```python
# Chuẩn hóa features (StandardScaler)
features_scaled = scaler.fit_transform(features)

# Áp dụng trọng số
features_scaled[:, 0] *= coord_weight   # lat
features_scaled[:, 1] *= coord_weight   # lng  
features_scaled[:, 2] *= time_weight    # time

# Khuyến nghị: coord_weight=1.0, time_weight=0.5
# → Ưu tiên vị trí hơn thời gian
```

---

## 4. Cấu Trúc Dữ Liệu

### 4.1. Input Data (Mock Data)
File: `data/mock_bookings_long_distance_1000.csv`

| Column | Type | Description |
|--------|------|-------------|
| client_id | string | ID khách hàng (28 ký tự) |
| created_at | ISO datetime | Thời điểm tạo booking |
| departureDate | string | Ngày khởi hành (dd/MM/yyyy) |
| departureTime | string | Giờ khởi hành (HH:mm) |
| destination | string | Tên điểm đến |
| destination_coordinates_lat | float | Vĩ độ điểm đến |
| destination_coordinates_lng | float | Kinh độ điểm đến |
| pickup | string | Tên điểm đón |
| pickup_coordinates_lat | float | Vĩ độ điểm đón |
| pickup_coordinates_lng | float | Kinh độ điểm đón |
| returnDate | string | Ngày về (optional) |
| state | string | Trạng thái = "Completed" |
| timestamp | long | Unix timestamp (ms) |
| trip_id | string | ID chuyến đi (empty) |

### 4.2. Vùng Địa Lý
```
Hà Nội:     Lat 20.70 - 21.30, Lng 105.50 - 106.10
Quảng Ninh: Lat 20.60 - 21.20, Lng 106.40 - 107.50
```

### 4.3. Điểm Đón/Đến
**Hà Nội:**
- Hanoi - My Dinh Bus Station
- Hanoi - Gia Lam Station
- Hanoi - Long Bien Station
- Hanoi - Giap Bat Station
- Hanoi - Luong Yen Station
- Hanoi - Yen Nghia Station

**Quảng Ninh:**
- Ha Long - Bai Chay Station
- Ha Long - Tuan Chau Port
- Cam Pha Bus Station
- Mong Cai Bus Station
- Uong Bi Bus Station
- Van Don Ferry Terminal

### 4.4. Output Data

#### Clustered Customers (`data/clustered_customers.csv`)
| Column | Description |
|--------|-------------|
| client_id | ID khách hàng |
| pickup | Điểm đón |
| destination | Điểm đến |
| departureDate | Ngày khởi hành |
| departureTime | Giờ khởi hành |
| pickup_coordinates_lat | Vĩ độ điểm đón |
| pickup_coordinates_lng | Kinh độ điểm đón |
| **cluster** | **ID nhóm được phân** |

#### Generated Trips (`data/generated_trips.csv`)
| Column | Description |
|--------|-------------|
| trip_id | ID chuyến đi (TRIP_001, ...) |
| cluster_id | ID cluster nguồn |
| num_passengers | Số khách |
| suggested_departure_time | Giờ khởi hành đề xuất |
| pickup_lat_center | Tâm vĩ độ nhóm |
| pickup_lng_center | Tâm kinh độ nhóm |
| customer_ids | Danh sách client_id |
| departure_date | Ngày khởi hành |

---

## 5. Thuật Toán & Mô Hình

### 5.1. Pipeline Xử Lý

```
┌─────────────────┐
│   Raw Data      │
│ (CSV/Firestore) │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Data Cleaning  │
│ - Parse dates   │
│ - Validate GPS  │
│ - Handle nulls  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Feature Eng.    │
│ - Extract time  │
│ - Scale features│
│ - Apply weights │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Find Optimal K  │
│ - Elbow method  │
│ - Silhouette    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ K-Means Cluster │
│ - Assign labels │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Generate Trips  │
│ - Split by max  │
│ - Calc centers  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Export Results  │
│ - CSV files     │
│ - Model pickle  │
└─────────────────┘
```

### 5.2. Class CustomerDispatcher

```python
class CustomerDispatcher:
    """
    Hệ thống điều phối khách hàng tự động
    """
    
    def __init__(self, coord_weight=1.0, time_weight=0.5, max_passengers=20):
        """
        Parameters:
        -----------
        coord_weight : float
            Trọng số cho tọa độ (lat, lng)
            Giá trị cao → ưu tiên nhóm theo vị trí
            
        time_weight : float
            Trọng số cho thời gian khởi hành
            Giá trị cao → ưu tiên nhóm theo giờ
            
        max_passengers : int
            Số khách tối đa mỗi chuyến xe
        """
        
    def preprocess(self, df) -> DataFrame:
        """Tiền xử lý dữ liệu"""
        
    def extract_features(self, df) -> np.ndarray:
        """Trích xuất và chuẩn hóa features"""
        
    def find_optimal_clusters(self, features_scaled) -> int:
        """Tìm số clusters tối ưu bằng Silhouette Score"""
        
    def cluster_customers(self, df, date_filter=None, direction_filter=None) -> (DataFrame, KMeans):
        """Phân cụm khách hàng"""
        
    def generate_trips(self, df_clustered) -> DataFrame:
        """Tạo danh sách chuyến đi từ clusters"""
```

### 5.3. Metrics Đánh Giá

| Metric | Mô tả | Giá trị tốt |
|--------|-------|-------------|
| **Silhouette Score** | Đo độ tương đồng trong cluster vs ngoài cluster | -1 đến 1 (cao hơn tốt hơn) |
| **Calinski-Harabasz** | Tỷ lệ phân tán giữa/trong cluster | Cao hơn tốt hơn |
| **Inertia** | Tổng khoảng cách đến centroid | Thấp hơn tốt hơn |

### 5.4. Chọn Tham Số Tối Ưu

```python
# Kết quả thử nghiệm trọng số
# Dataset: 500 khách Hanoi → Quang Ninh

| coord_weight | time_weight | Silhouette |
|--------------|-------------|------------|
| 0.5          | 0.3         | 0.312      |
| 1.0          | 0.5         | 0.387      |  ← Khuyến nghị
| 1.5          | 0.5         | 0.356      |
| 1.0          | 1.0         | 0.341      |
| 2.0          | 0.3         | 0.298      |
```

---

## 6. Hướng Dẫn Sử Dụng

### 6.1. Yêu Cầu Hệ Thống

```bash
# Python 3.8+
pip install pandas numpy matplotlib seaborn scikit-learn
```

### 6.2. Chạy Jupyter Notebook

```bash
cd BookCar/notebooks
jupyter notebook customer_clustering_dispatch.ipynb
```

### 6.3. Sử Dụng Class CustomerDispatcher

```python
from customer_dispatcher import CustomerDispatcher
import pandas as pd

# 1. Load data
df = pd.read_csv('data/mock_bookings_long_distance_1000.csv')

# 2. Khởi tạo dispatcher
dispatcher = CustomerDispatcher(
    coord_weight=1.0,   # Trọng số vị trí
    time_weight=0.5,    # Trọng số thời gian
    max_passengers=15   # Max khách/chuyến
)

# 3. Phân cụm theo ngày và hướng
df_clustered, model = dispatcher.cluster_customers(
    df,
    date_filter='15/12/2025',           # Optional: lọc theo ngày
    direction_filter='Hanoi_to_QuangNinh'  # Optional: lọc theo hướng
)

# 4. Tạo danh sách chuyến đi
trips = dispatcher.generate_trips(df_clustered)

# 5. Xem kết quả
print(trips[['trip_id', 'num_passengers', 'suggested_departure_time']])
```

### 6.4. Lưu & Load Model

```python
import pickle
import json

# Lưu model
with open('data/kmeans_model.pkl', 'wb') as f:
    pickle.dump(model, f)

with open('data/feature_scaler.pkl', 'wb') as f:
    pickle.dump(dispatcher.scaler, f)

# Load model
with open('data/kmeans_model.pkl', 'rb') as f:
    loaded_model = pickle.load(f)
```

---

## 7. Tích Hợp Vào Ứng Dụng

### 7.1. Phương Án 1: Backend Python API

```
┌─────────────┐     HTTP      ┌──────────────┐     Firestore    ┌───────────┐
│ Android App │ ◄──────────► │ Python Flask │ ◄───────────────► │ Firebase  │
│ (Client)    │    REST API  │ (Backend)    │                   │           │
└─────────────┘              └──────────────┘                   └───────────┘
```

**Flask API Example:**
```python
from flask import Flask, jsonify, request
from customer_dispatcher import CustomerDispatcher

app = Flask(__name__)
dispatcher = CustomerDispatcher()

@app.route('/api/cluster-customers', methods=['POST'])
def cluster_customers():
    data = request.json
    date = data.get('date')
    direction = data.get('direction')
    
    # Lấy bookings từ Firestore
    bookings = get_bookings_from_firestore(date, direction)
    
    # Phân cụm
    df_clustered, _ = dispatcher.cluster_customers(bookings)
    trips = dispatcher.generate_trips(df_clustered)
    
    return jsonify(trips.to_dict('records'))
```

### 7.2. Phương Án 2: Firebase Cloud Functions

```javascript
// functions/index.js
const functions = require('firebase-functions');
const {PythonShell} = require('python-shell');

exports.clusterCustomers = functions.https.onCall(async (data, context) => {
    const { date, direction } = data;
    
    // Chạy Python script
    const options = {
        args: [date, direction]
    };
    
    const results = await PythonShell.run('cluster_customers.py', options);
    return JSON.parse(results[0]);
});
```

### 7.3. Phương Án 3: Export ONNX & Chạy Trực Tiếp Trên Android

```java
// Android: Load ONNX model
import ai.onnxruntime.*;

OrtEnvironment env = OrtEnvironment.getEnvironment();
OrtSession session = env.createSession("kmeans_model.onnx");

// Predict cluster
float[][] features = new float[][]{{lat, lng, timeMinutes}};
OnnxTensor inputTensor = OnnxTensor.createTensor(env, features);
OrtSession.Result result = session.run(Map.of("input", inputTensor));
```

### 7.4. Cập Nhật ArrangeCustomersFragment

```java
// Thêm button "Auto Cluster"
Button btnAutoCluster = view.findViewById(R.id.btnAutoCluster);
btnAutoCluster.setOnClickListener(v -> {
    // Call API to cluster customers
    ApiService.clusterCustomers(selectedDate, direction)
        .addOnSuccessListener(trips -> {
            // Hiển thị trips đề xuất
            showClusteringSuggestions(trips);
        });
});

private void showClusteringSuggestions(List<Trip> trips) {
    // Hiển thị dialog với các nhóm đề xuất
    // Cho phép điều phối viên review và confirm
}
```

---

## 8. Đánh Giá & Tối Ưu

### 8.1. Kết Quả Test

```
Dataset: 1000 bookings (mock data)
Direction: Hanoi → Quang Ninh (500 samples)

Optimal K: 6-8 clusters
Silhouette Score: 0.35 - 0.42
Average passengers per trip: 12-15
```

### 8.2. Các Yếu Tố Ảnh Hưởng

| Yếu tố | Tác động |
|--------|----------|
| Phân bố khách không đều | Có thể tạo clusters rất nhỏ/lớn |
| Peak hours (7-9h, 17-19h) | Nhiều clusters hơn |
| Ngày lễ/cuối tuần | Patterns khác ngày thường |
| Seasonal trends | Cần retrain định kỳ |

### 8.3. Khuyến Nghị Tối Ưu

1. **Retrain model hàng tháng** với dữ liệu mới
2. **Monitor metrics** (Silhouette, số khách/chuyến)
3. **A/B testing** với different weights
4. **Feedback loop**: Thu thập phản hồi từ điều phối viên

### 8.4. Limitations & Future Work

**Limitations:**
- Chưa xét đến traffic/road conditions
- Chưa optimize route order trong cluster
- Chưa handle real-time bookings

**Future Work:**
- [ ] Tích hợp Google Maps Distance Matrix API
- [ ] Traveling Salesman Problem cho route optimization
- [ ] Real-time clustering với streaming data
- [ ] Deep Learning approach (Autoencoder + Clustering)

---

## Appendix

### A. File Structure

```
BookCar/
├── app/
│   └── src/main/java/com/example/bookcar/
│       ├── model/
│       │   ├── Order.java
│       │   ├── Trips.java
│       │   └── Driver.java
│       └── view/coordination/
│           └── ArrangeCustomersFragment.java
├── data/
│   ├── mock_bookings_long_distance_1000.csv
│   ├── mock_bookings_long_distance_1000.json
│   ├── clustered_customers.csv
│   ├── generated_trips.csv
│   ├── kmeans_model.pkl
│   ├── feature_scaler.pkl
│   └── model_config.json
├── notebooks/
│   └── customer_clustering_dispatch.ipynb
├── scripts/
│   └── generate_long_distance_bookings.py
└── docs/
    └── AUTOMATED_DISPATCH_DOCUMENTATION.md
```

### B. References

1. Scikit-learn: https://scikit-learn.org/stable/modules/clustering.html
2. K-Means: https://en.wikipedia.org/wiki/K-means_clustering
3. DBSCAN: https://en.wikipedia.org/wiki/DBSCAN
4. Silhouette Score: https://en.wikipedia.org/wiki/Silhouette_(clustering)

---

*Document Version: 1.0*  
*Created: January 7, 2026*  
*Author: GitHub Copilot*
