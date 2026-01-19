@echo off
pause

python cluster_prediction_api.py

echo.
echo ============================================================
echo.
echo Press Ctrl+C to stop the server
echo Server will run at: http://localhost:5000
echo.
echo [3/3] Starting API server...
echo.

)
    echo Dependencies OK
) else (
    )
        exit /b 1
        pause
        echo ERROR: Failed to install dependencies
    if errorlevel 1 (
    pip install -r requirements.txt
    echo Dependencies not found. Installing...
    echo.
if errorlevel 1 (
python -c "import flask, pandas, sklearn" 2>nul
echo [2/3] Checking dependencies...
echo.

python --version
echo [1/3] Checking Python version...

)
    exit /b 1
    pause
    echo Please install Python 3.8+ from https://www.python.org/
    echo ERROR: Python is not installed or not in PATH
if errorlevel 1 (
python --version >nul 2>&1
REM Check if Python is installed

cd /d "%~dp0"
REM Change to scripts directory

echo.
echo ============================================================
echo   BookCar - Clustering API Server Startup
echo ============================================================

