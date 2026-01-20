@echo off
echo ================================
echo   TEST CLUSTERING - QUICK START
echo ================================
echo.
echo This script will help you:
echo 1. Seed 30 test orders
echo 2. Test clustering
echo.
pause

echo.
echo [Step 1/4] Checking Firebase credentials...
if not exist "serviceAccountKey.json" (
    echo.
    echo [ERROR] serviceAccountKey.json not found!
    echo.
    echo GET IT NOW:
    echo 1. Go to: https://console.firebase.google.com/project/bookcar-ce16f/settings/serviceaccounts/adminsdk
    echo 2. Click "Generate new private key"
    echo 3. Save as: serviceAccountKey.json
    echo 4. Place in this folder
    echo.
    start https://console.firebase.google.com/project/bookcar-ce16f/settings/serviceaccounts/adminsdk
    pause
    exit /b 1
)
echo [OK] serviceAccountKey.json found

echo.
echo [Step 2/4] Installing dependencies...
pip install firebase-admin >nul 2>&1
if errorlevel 1 (
    echo [WARNING] Failed to install firebase-admin
    echo Please run: pip install firebase-admin
)

echo.
echo [Step 3/4] Seeding 30 test orders...
python -c "from seed_firestore_data import generate_orders, seed_orders_to_firestore; orders = generate_orders(30, '21/01/2026'); seed_orders_to_firestore(orders); print('\n✅ 30 orders created!')"

echo.
echo [Step 4/4] Starting API server...
echo.
echo ========================================
echo   Now test on Android app:
echo   1. Run app
echo   2. Login (Coordination)
echo   3. Click "Phân cụm tự động"
echo   4. Should see 3 trips (~10 customers each)
echo ========================================
echo.
python cluster_prediction_api.py
