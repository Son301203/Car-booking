@echo off
echo ============================================================
echo   Testing BookCar Clustering API
echo ============================================================
echo.

cd /d "%~dp0"

echo Checking if API server is running...
echo.

python -c "import requests; requests.get('http://localhost:5000/health')" 2>nul
if errorlevel 1 (
    echo ERROR: API server is not running
    echo Please start the server first:
    echo    start_server.bat
    echo.
    pause
    exit /b 1
)

echo Running tests...
echo.
python test_api.py

echo.
pause

