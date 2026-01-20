@echo off
echo ========================================
echo   FIRESTORE DATA SEEDING TOOL
echo ========================================
echo.

REM Check if Python is installed
python --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Python is not installed or not in PATH
    echo Please install Python from https://www.python.org/downloads/
    pause
    exit /b 1
)

REM Check if firebase-admin is installed
python -c "import firebase_admin" >nul 2>&1
if errorlevel 1 (
    echo [INFO] Installing Firebase Admin SDK...
    pip install firebase-admin
    if errorlevel 1 (
        echo [ERROR] Failed to install firebase-admin
        pause
        exit /b 1
    )
)

REM Check if serviceAccountKey.json exists
if not exist "serviceAccountKey.json" (
    echo.
    echo [ERROR] serviceAccountKey.json NOT FOUND!
    echo.
    echo HOW TO GET IT:
    echo 1. Go to: https://console.firebase.google.com/project/bookcar-ce16f/settings/serviceaccounts/adminsdk
    echo 2. Click "Generate new private key"
    echo 3. Save the file as: serviceAccountKey.json
    echo 4. Place it in: D:\code\Java_Android\BookCar\scripts\
    echo.
    pause
    exit /b 1
)

echo [OK] All dependencies ready
echo.

REM Run the script
python seed_firestore_data.py

pause
