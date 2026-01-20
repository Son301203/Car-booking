"""
Script để seed dữ liệu test vào Firestore cho việc test mô hình phân cụm
"""

import firebase_admin
from firebase_admin import credentials, firestore
from datetime import datetime, timedelta
import random
import json

# ===== HƯỚNG DẪN LẤY SERVICE ACCOUNT KEY =====
# 1. Vào Firebase Console: https://console.firebase.google.com/
# 2. Chọn project "bookcar-ce16f"
# 3. Settings (⚙️) → Project Settings → Service Accounts
# 4. Click "Generate new private key"
# 5. Lưu file JSON vào: scripts/serviceAccountKey.json

# Khởi tạo Firebase Admin
try:
    cred = credentials.Certificate('serviceAccountKey.json')
    firebase_admin.initialize_app(cred)
    print("✅ Firebase Admin initialized successfully")
except Exception as e:
    print(f"❌ Error: {e}")
    print("\n📋 HƯỚNG DẪN:")
    print("1. Vào: https://console.firebase.google.com/project/bookcar-ce16f/settings/serviceaccounts/adminsdk")
    print("2. Click 'Generate new private key'")
    print("3. Lưu file vào: scripts/serviceAccountKey.json")
    print("4. Chạy lại script này")
    exit(1)

db = firestore.client()

# ===== DỮ LIỆU MẪU =====

# Tọa độ các điểm đón phổ biến ở Hà Nội (để tạo clusters rõ ràng)
PICKUP_LOCATIONS = {
    # Cluster 1: Khu vực Cầu Giấy - Đống Đa (Trung tâm Hà Nội)
    "cluster_1": [
        {"name": "Keangnam", "lat": 21.0178, "lng": 105.7843, "address": "Keangnam Hanoi Landmark Tower"},
        {"name": "Mỹ Đình", "lat": 21.0285, "lng": 105.7670, "address": "Sân vận động Mỹ Đình"},
        {"name": "Cầu Giấy", "lat": 21.0333, "lng": 105.7949, "address": "Ngã tư Cầu Giấy"},
        {"name": "Nghĩa Tân", "lat": 21.0395, "lng": 105.8001, "address": "Phố Nghĩa Tân"},
        {"name": "Láng Hạ", "lat": 21.0145, "lng": 105.8120, "address": "Đường Láng Hạ"},
    ],
    
    # Cluster 2: Khu vực Hoàn Kiếm - Hai Bà Trưng (Trung tâm lịch sử)
    "cluster_2": [
        {"name": "Hồ Gươm", "lat": 21.0285, "lng": 105.8542, "address": "Hồ Hoàn Kiếm"},
        {"name": "Chợ Đồng Xuân", "lat": 21.0361, "lng": 105.8479, "address": "Chợ Đồng Xuân"},
        {"name": "Hoàng Cầu", "lat": 21.0162, "lng": 105.8356, "address": "Ngã tư Hoàng Cầu"},
        {"name": "Bách Khoa", "lat": 21.0053, "lng": 105.8433, "address": "ĐH Bách Khoa Hà Nội"},
        {"name": "Giảng Võ", "lat": 21.0278, "lng": 105.8185, "address": "Đường Giảng Võ"},
    ],
    
    # Cluster 3: Khu vực Long Biên - Gia Lâm (Gần đường đi Quảng Ninh)
    "cluster_3": [
        {"name": "Long Biên", "lat": 21.0453, "lng": 105.8695, "address": "Cầu Long Biên"},
        {"name": "Gia Lâm", "lat": 21.0362, "lng": 105.9285, "address": "Huyện Gia Lâm"},
        {"name": "Ngọc Lâm", "lat": 21.0298, "lng": 105.8851, "address": "Phố Ngọc Lâm"},
        {"name": "Sài Đồng", "lat": 21.0545, "lng": 105.9410, "address": "Khu đô thị Sài Đồng"},
        {"name": "Vĩnh Tuy", "lat": 21.0191, "lng": 105.8731, "address": "Cầu Vĩnh Tuy"},
    ],
}

# Điểm đến phổ biến ở Quảng Ninh
DESTINATIONS = [
    {"name": "Bãi Cháy", "lat": 20.9598, "lng": 107.0845, "address": "Bãi Cháy, Hạ Long"},
    {"name": "Tuần Châu", "lat": 20.9357, "lng": 107.0475, "address": "Đảo Tuần Châu"},
    {"name": "Hòn Gai", "lat": 20.9519, "lng": 107.0767, "address": "Hòn Gai, Hạ Long"},
    {"name": "Cẩm Phả", "lat": 21.0147, "lng": 107.3089, "address": "Thành phố Cẩm Phả"},
    {"name": "Móng Cái", "lat": 21.5274, "lng": 107.9621, "address": "Thành phố Móng Cái"},
]

# Khung giờ khởi hành phổ biến
DEPARTURE_TIMES = [
    "06:00", "06:30", "07:00", "07:30", "08:00", "08:30",
    "09:00", "09:30", "10:00", "14:00", "15:00", "16:00"
]

# Dữ liệu users mẫu
SAMPLE_USERS = [
    {"name": "Nguyễn Văn A", "email": "nguyenvana@gmail.com", "phone": "0912345001"},
    {"name": "Trần Thị B", "email": "tranthib@gmail.com", "phone": "0912345002"},
    {"name": "Lê Văn C", "email": "levanc@gmail.com", "phone": "0912345003"},
    {"name": "Phạm Thị D", "email": "phamthid@gmail.com", "phone": "0912345004"},
    {"name": "Hoàng Văn E", "email": "hoangvane@gmail.com", "phone": "0912345005"},
    {"name": "Vũ Thị F", "email": "vuthif@gmail.com", "phone": "0912345006"},
    {"name": "Đặng Văn G", "email": "dangvang@gmail.com", "phone": "0912345007"},
    {"name": "Bùi Thị H", "email": "buithih@gmail.com", "phone": "0912345008"},
    {"name": "Đỗ Văn I", "email": "dovani@gmail.com", "phone": "0912345009"},
    {"name": "Ngô Thị K", "email": "ngothik@gmail.com", "phone": "0912345010"},
    {"name": "Dương Văn L", "email": "duongvanl@gmail.com", "phone": "0912345011"},
    {"name": "Lý Thị M", "email": "lythim@gmail.com", "phone": "0912345012"},
    {"name": "Trịnh Văn N", "email": "trinhvann@gmail.com", "phone": "0912345013"},
    {"name": "Mai Thị O", "email": "maithio@gmail.com", "phone": "0912345014"},
    {"name": "Võ Văn P", "email": "vovanp@gmail.com", "phone": "0912345015"},
    {"name": "Phan Thị Q", "email": "phanthiq@gmail.com", "phone": "0912345016"},
    {"name": "Tô Văn R", "email": "tovanr@gmail.com", "phone": "0912345017"},
    {"name": "Hồ Thị S", "email": "hothis@gmail.com", "phone": "0912345018"},
    {"name": "Đinh Văn T", "email": "dinhvant@gmail.com", "phone": "0912345019"},
    {"name": "Chu Thị U", "email": "chuthiu@gmail.com", "phone": "0912345020"},
]

# Client role ID - Lấy từ Firestore (có thể update sau)
CLIENT_ROLE_ID = "xXuSy9EUcYO0efMANIAw"  # Thay bằng role ID thật nếu khác

# ===== FUNCTIONS =====

def generate_test_users(num_users=20):
    """
    Generate test users data
    
    Args:
        num_users: Số lượng users cần tạo (max 20)
    """
    users = []
    num_users = min(num_users, len(SAMPLE_USERS))
    
    for i in range(num_users):
        user_data = SAMPLE_USERS[i]
        user = {
            "created_at": firestore.SERVER_TIMESTAMP,
            "date_of_birth": "",
            "email": user_data["email"],
            "gender": "",
            "name": user_data["name"],
            "password": "123456",  # Default password
            "phone": user_data["phone"],
            "role_id": CLIENT_ROLE_ID,
            "_test_user": True,  # Đánh dấu là test user
        }
        users.append(user)
    
    return users


def seed_users_to_firestore(users):
    """Upload users lên Firestore và trả về danh sách user IDs"""
    print(f"\n📤 Uploading {len(users)} users to Firestore...")
    
    batch = db.batch()
    user_ids = []
    
    for i, user in enumerate(users):
        # Tạo document với auto-generated ID
        doc_ref = db.collection('users').document()
        batch.set(doc_ref, user)
        user_ids.append(doc_ref.id)
        
        # Firestore batch limit = 500
        if (i + 1) % 500 == 0:
            batch.commit()
            print(f"  ✅ Committed batch: {i + 1} users")
            batch = db.batch()
    
    # Commit remaining
    batch.commit()
    print(f"✅ Successfully uploaded {len(users)} users")
    print(f"📝 User IDs: {user_ids[:5]}... (showing first 5)")
    
    return user_ids


def get_existing_test_user_ids():
    """Lấy danh sách user IDs đã có từ Firestore (test users)"""
    print("\n🔍 Checking for existing test users...")
    
    users_query = db.collection('users').where('_test_user', '==', True).stream()
    user_ids = [doc.id for doc in users_query]
    
    if user_ids:
        print(f"✅ Found {len(user_ids)} existing test users")
        return user_ids
    else:
        print("⚠️  No existing test users found")
        return []

def generate_orders(num_orders=30, target_date="21/01/2026", target_time=None, client_ids=None):
    """
    Generate orders với tọa độ được phân bổ vào các clusters rõ ràng
    
    Args:
        num_orders: Số lượng orders cần tạo
        target_date: Ngày khởi hành (format: DD/MM/YYYY)
        target_time: Giờ khởi hành cụ thể (None = random từ DEPARTURE_TIMES)
        client_ids: Danh sách user IDs thực tế (None = tạo mới users trước)
    """
    # Nếu không có client_ids, cần seed users trước
    if not client_ids:
        print("\n⚠️  Không có client_ids. Cần seed users trước!")
        print("Đang tự động seed users...")
        users = generate_test_users(20)
        client_ids = seed_users_to_firestore(users)
    
    orders = []
    
    # Phân bổ orders vào các clusters
    cluster_names = list(PICKUP_LOCATIONS.keys())
    orders_per_cluster = num_orders // len(cluster_names)
    
    for cluster_name in cluster_names:
        locations = PICKUP_LOCATIONS[cluster_name]
        
        for i in range(orders_per_cluster):
            # Random pickup location trong cluster
            pickup = random.choice(locations)
            
            # Thêm noise nhỏ để không trùng hoàn toàn (trong bán kính ~500m)
            lat_noise = random.uniform(-0.005, 0.005)
            lng_noise = random.uniform(-0.005, 0.005)
            
            # Random destination
            destination = random.choice(DESTINATIONS)
            
            # Random hoặc fixed departure time
            departure_time = target_time if target_time else random.choice(DEPARTURE_TIMES)
            
            # Random client từ danh sách IDs thực tế
            client_id = random.choice(client_ids)
            
            order = {
                "client_id": client_id,
                "created_at": firestore.SERVER_TIMESTAMP,
                "departureDate": target_date,
                "departureTime": departure_time,
                "destination": destination["name"],
                "destination_coordinates": firestore.GeoPoint(
                    destination["lat"], 
                    destination["lng"]
                ),
                "pickup": pickup["name"],
                "pickup_coordinates": firestore.GeoPoint(
                    pickup["lat"] + lat_noise, 
                    pickup["lng"] + lng_noise
                ),
                "state": "Booked",  # Chưa phân chuyến
                "timestamp": int(datetime.now().timestamp() * 1000),
                # Thêm metadata để debug
                "_test_cluster": cluster_name,
                "_test_pickup_base": pickup["address"],
            }
            
            orders.append(order)
    
    # Thêm vài orders lẻ để tổng đúng num_orders
    remaining = num_orders - len(orders)
    for i in range(remaining):
        cluster_name = random.choice(cluster_names)
        pickup = random.choice(PICKUP_LOCATIONS[cluster_name])
        destination = random.choice(DESTINATIONS)
        departure_time = target_time if target_time else random.choice(DEPARTURE_TIMES)
        
        order = {
            "client_id": random.choice(client_ids),
            "created_at": firestore.SERVER_TIMESTAMP,
            "departureDate": target_date,
            "departureTime": departure_time,
            "destination": destination["name"],
            "destination_coordinates": firestore.GeoPoint(destination["lat"], destination["lng"]),
            "pickup": pickup["name"],
            "pickup_coordinates": firestore.GeoPoint(
                pickup["lat"] + random.uniform(-0.005, 0.005),
                pickup["lng"] + random.uniform(-0.005, 0.005)
            ),
            "state": "Booked",
            "timestamp": int(datetime.now().timestamp() * 1000),
            "_test_cluster": cluster_name,
            "_test_pickup_base": pickup["address"],
        }
        orders.append(order)
    
    return orders


def seed_orders_to_firestore(orders):
    """Upload orders lên Firestore"""
    print(f"\n📤 Uploading {len(orders)} orders to Firestore...")
    
    batch = db.batch()
    order_refs = []
    
    for i, order in enumerate(orders):
        # Tạo document với auto-generated ID
        doc_ref = db.collection('orders').document()
        batch.set(doc_ref, order)
        order_refs.append(doc_ref)
        
        # Firestore batch limit = 500
        if (i + 1) % 500 == 0:
            batch.commit()
            print(f"  ✅ Committed batch: {i + 1} orders")
            batch = db.batch()
    
    # Commit remaining
    batch.commit()
    print(f"✅ Successfully uploaded {len(orders)} orders")
    
    return order_refs


def generate_sample_trip(driver_id="sample_driver_001", date="21/01/2026", time="08:00"):
    """Generate 1 sample trip"""
    
    # Parse date to get day of week
    day, month, year = date.split('/')
    dt = datetime(int(year), int(month), int(day))
    day_of_week = dt.weekday()  # 0 = Monday, 6 = Sunday
    
    trip = {
        "created_at": firestore.SERVER_TIMESTAMP,
        "dateTrip": date,
        "dateTrips": date,  # Duplicate field (theo structure bạn cung cấp)
        "dayOfWeek": day_of_week,
        "driver_id": driver_id,
        "quantity": 0,  # Sẽ update sau khi thêm orders
        "startTime": time,
        "status": "pending",
        "timeTrips": time,
    }
    
    return trip


def clean_test_data():
    """Xóa tất cả test data: orders, users"""
    print("\n🧹 Cleaning existing test data...")
    
    # Delete test orders
    print("  🗑️  Deleting test orders...")
    orders_query = db.collection('orders').where('_test_cluster', '>=', '').stream()
    batch = db.batch()
    order_count = 0
    
    for doc in orders_query:
        batch.delete(doc.reference)
        order_count += 1
        if order_count % 500 == 0:
            batch.commit()
            batch = db.batch()
    
    batch.commit()
    print(f"  ✅ Deleted {order_count} test orders")
    
    # Delete test users
    print("  🗑️  Deleting test users...")
    users_query = db.collection('users').where('_test_user', '==', True).stream()
    batch = db.batch()
    user_count = 0
    
    for doc in users_query:
        batch.delete(doc.reference)
        user_count += 1
        if user_count % 500 == 0:
            batch.commit()
            batch = db.batch()
    
    batch.commit()
    print(f"  ✅ Deleted {user_count} test users")
    
    print(f"\n📊 Summary:")
    print(f"  • Orders deleted: {order_count}")
    print(f"  • Users deleted: {user_count}")
    print("  ⚠️  Trips created from test orders need manual deletion on Firestore Console")


# ===== MAIN =====

def main():
    print("=" * 60)
    print("🌱 FIRESTORE DATA SEEDING SCRIPT")
    print("=" * 60)
    
    # Menu
    print("\nChọn hành động:")
    print("1. Seed users + orders (30 orders, 3 clusters) - KHUYẾN NGHỊ")
    print("2. Seed 60 orders (test với nhiều data hơn)")
    print("3. Seed 100 orders (stress test)")
    print("4. Seed orders với giờ khởi hành cố định")
    print("5. Seed ONLY users (20 users)")
    print("6. Clean tất cả test data (orders + users)")
    print("7. View cluster distribution")
    print("0. Exit")
    
    choice = input("\nNhập lựa chọn (0-7): ").strip()
    
    if choice == "1":
        print("\n📋 Seeding users + 30 orders với 3 clusters rõ ràng...")
        # Seed users trước
        users = generate_test_users(20)
        client_ids = seed_users_to_firestore(users)
        # Seed orders với user IDs thực tế
        orders = generate_orders(num_orders=30, target_date="21/01/2026", client_ids=client_ids)
        seed_orders_to_firestore(orders)
        print_cluster_info(orders)
        
    elif choice == "2":
        print("\n📋 Seeding 60 orders...")
        # Kiểm tra xem đã có test users chưa
        client_ids = get_existing_test_user_ids()
        orders = generate_orders(num_orders=60, target_date="21/01/2026", client_ids=client_ids if client_ids else None)
        seed_orders_to_firestore(orders)
        print_cluster_info(orders)
        
    elif choice == "3":
        print("\n📋 Seeding 100 orders...")
        client_ids = get_existing_test_user_ids()
        orders = generate_orders(num_orders=100, target_date="21/01/2026", client_ids=client_ids if client_ids else None)
        seed_orders_to_firestore(orders)
        print_cluster_info(orders)
        
    elif choice == "4":
        fixed_time = input("Nhập giờ khởi hành (VD: 08:00): ").strip()
        num = int(input("Số lượng orders: ").strip())
        print(f"\n📋 Seeding {num} orders với giờ {fixed_time}...")
        client_ids = get_existing_test_user_ids()
        orders = generate_orders(num_orders=num, target_date="21/01/2026", target_time=fixed_time, client_ids=client_ids if client_ids else None)
        seed_orders_to_firestore(orders)
        print_cluster_info(orders)
        
    elif choice == "5":
        print("\n📋 Seeding 20 test users...")
        users = generate_test_users(20)
        seed_users_to_firestore(users)
        print("✅ Users seeded successfully. You can now seed orders with option 1-4.")
        
    elif choice == "6":
        confirm = input("⚠️  Xóa tất cả test data (orders + users)? (yes/no): ").strip().lower()
        if confirm == "yes":
            clean_test_data()
        else:
            print("❌ Cancelled")
            
    elif choice == "7":
        view_firestore_data()
        
    elif choice == "0":
        print("👋 Goodbye!")
        
    else:
        print("❌ Invalid choice")
    
    print("\n" + "=" * 60)
    print("✅ DONE")
    print("=" * 60)


def print_cluster_info(orders):
    """In thống kê cluster distribution"""
    from collections import Counter
    
    cluster_counts = Counter([o.get('_test_cluster', 'unknown') for o in orders])
    time_counts = Counter([o['departureTime'] for o in orders])
    
    print("\n📊 Cluster Distribution:")
    for cluster, count in sorted(cluster_counts.items()):
        print(f"  {cluster}: {count} orders")
    
    print("\n⏰ Departure Time Distribution:")
    for time, count in sorted(time_counts.items()):
        print(f"  {time}: {count} orders")
    
    print(f"\n📍 Pickup Locations:")
    for cluster_name, locations in PICKUP_LOCATIONS.items():
        print(f"  {cluster_name}:")
        for loc in locations:
            print(f"    - {loc['name']} ({loc['address']})")


def view_firestore_data():
    """Xem dữ liệu đã seed trên Firestore"""
    print("\n📊 Current Firestore Data:")
    
    # Count users
    all_users = db.collection('users').stream()
    test_users = db.collection('users').where('_test_user', '==', True).stream()
    
    all_users_count = sum(1 for _ in all_users)
    test_users_count = sum(1 for _ in test_users)
    
    print(f"\nUsers:")
    print(f"  👥 Total users: {all_users_count}")
    print(f"  🧪 Test users: {test_users_count}")
    
    # Count orders by state
    booked_orders = db.collection('orders').where('state', '==', 'Booked').stream()
    arranged_orders = db.collection('orders').where('state', '==', 'Arranged').stream()
    
    booked_count = sum(1 for _ in booked_orders)
    arranged_count = sum(1 for _ in arranged_orders)
    
    print(f"\nOrders:")
    print(f"  📦 Booked: {booked_count}")
    print(f"  ✅ Arranged: {arranged_count}")
    print(f"  📊 Total: {booked_count + arranged_count}")
    
    # Count test orders
    test_orders = db.collection('orders').where('_test_cluster', '>=', '').stream()
    test_count = sum(1 for _ in test_orders)
    print(f"  🧪 Test orders: {test_count}")


if __name__ == "__main__":
    main()
