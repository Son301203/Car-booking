#!/usr/bin/env python3
"""
Generate mock long-distance bus bookings between Hanoi and Quang Ninh.
Outputs CSV with these fields only:
client_id,created_at,departureDate,departureTime,destination,destination_coordinates_lat,destination_coordinates_lng,
pickup,pickup_coordinates_lat,pickup_coordinates_lng,returnDate,state,timestamp,trip_id

Rules:
- If pickup is in Hanoi then destination is in Quang Ninh and vice versa.
- trip_id is empty string.
- state is always 'Completed'.
- created_at is ISO datetime in Vietnam timezone (+07:00), timestamp is epoch milliseconds.
- Coordinates are saved as explicit lat and lng numeric values to avoid encoding issues.
- departureTime is stored as HH:mm (customer selected departure time)
"""
import argparse
import csv
import os
import random
import uuid
from datetime import datetime, timedelta, timezone
import math

# Bounding boxes
REGIONS = {
    'hanoi': (20.70, 21.30, 105.50, 106.10),
    'quang_ninh': (20.60, 21.20, 106.40, 107.50)
}

# Realistic stop names to mock pickup/destination strings
HANOI_STOPS = [
    'Hanoi - My Dinh Bus Station', 'Hanoi - Gia Lam Station', 'Hanoi - Long Bien Station',
    'Hanoi - Giap Bat Station', 'Hanoi - Luong Yen Station', 'Hanoi - Yen Nghia Station'
]
QUANG_NINH_STOPS = [
    'Ha Long - Bai Chay Station', 'Ha Long - Tuan Chau Port', 'Cam Pha Bus Station',
    'Mong Cai Bus Station', 'Uong Bi Bus Station', 'Van Don Ferry Terminal'
]


def sample_in_bbox(bbox):
    lat_min, lat_max, lng_min, lng_max = bbox
    lat = random.uniform(lat_min, lat_max)
    lng = random.uniform(lng_min, lng_max)
    return lat, lng


def sample_client_id():
    # Generate 28-char alphanumeric ID similar to Firestore UID in example
    chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz0123456789'
    return ''.join(random.choice(chars) for _ in range(28))


def sample_created_at(days_back=90):
    now = datetime.now(timezone(timedelta(hours=7)))
    start = now - timedelta(days=days_back)
    dt = start + timedelta(seconds=random.uniform(0, days_back*24*3600))
    # keep timezone +07:00
    dt = dt.replace(tzinfo=timezone(timedelta(hours=7)))
    return dt


def sample_departure_time():
    # For long-distance buses, common departure windows: 04:00 - 23:00
    hour = random.randint(4, 23)
    minute = random.choice([0, 15, 30, 45])
    return f"{hour:02d}:{minute:02d}"


def main():
    parser = argparse.ArgumentParser(description='Generate long-distance mock bookings CSV')
    parser.add_argument('--n', type=int, default=1000)
    parser.add_argument('--seed', type=int, default=2026)
    parser.add_argument('--out', type=str, default='data/mock_bookings_long_distance_1000.csv')
    args = parser.parse_args()

    random.seed(args.seed)

    out_dir = os.path.dirname(args.out)
    if out_dir and not os.path.exists(out_dir):
        os.makedirs(out_dir, exist_ok=True)

    fieldnames = ['client_id','created_at','departureDate','departureTime','destination','destination_coordinates_lat','destination_coordinates_lng',
                  'pickup','pickup_coordinates_lat','pickup_coordinates_lng','returnDate','state','timestamp','trip_id']

    rows = []
    # create a small pool of client_ids to have repeats
    clients = [sample_client_id() for _ in range(200)]

    for i in range(args.n):
        # choose direction: 0 means pickup Hanoi -> destination Quang Ninh; 1 vice versa
        direction = random.choice([0,1])
        if direction == 0:
            pickup_region = 'hanoi'
            dest_region = 'quang_ninh'
            pickup_label = random.choice(HANOI_STOPS)
            destination_label = random.choice(QUANG_NINH_STOPS)
        else:
            pickup_region = 'quang_ninh'
            dest_region = 'hanoi'
            pickup_label = random.choice(QUANG_NINH_STOPS)
            destination_label = random.choice(HANOI_STOPS)

        pl_lat, pl_lng = sample_in_bbox(REGIONS[pickup_region])
        dl_lat, dl_lng = sample_in_bbox(REGIONS[dest_region])

        created_dt = sample_created_at(days_back=90)
        created_at_iso = created_dt.isoformat()
        timestamp_ms = int(created_dt.timestamp() * 1000)

        # departureDate format dd/MM/YYYY (use created date)
        departure_date = created_dt.strftime('%d/%m/%Y')
        departure_time = sample_departure_time()

        row = {
            'client_id': random.choice(clients),
            'created_at': created_at_iso,
            'departureDate': departure_date,
            'departureTime': departure_time,
            'destination': destination_label,
            'destination_coordinates_lat': f"{dl_lat:.14f}",
            'destination_coordinates_lng': f"{dl_lng:.14f}",
            'pickup': pickup_label,
            'pickup_coordinates_lat': f"{pl_lat:.14f}",
            'pickup_coordinates_lng': f"{pl_lng:.14f}",
            'returnDate': '',
            'state': 'Completed',
            'timestamp': timestamp_ms,
            'trip_id': ''
        }
        rows.append(row)

    with open(args.out, 'w', newline='', encoding='utf-8') as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        for r in rows:
            writer.writerow(r)

    meta = {
        'rows': args.n,
        'seed': args.seed,
        'path': args.out,
        'notes': 'Long-distance: pickup in Hanoi <-> destination in Quang Ninh (and vice versa). Coordinates saved as separate lat/lng.'
    }
    with open(os.path.splitext(args.out)[0] + '.json', 'w', encoding='utf-8') as mf:
        import json
        json.dump(meta, mf, ensure_ascii=False, indent=2)

    print(f"Wrote {len(rows)} rows to {args.out}")

if __name__ == '__main__':
    main()
