@echo off
echo Starting SWNIH - Smart Web Notification Intelligence Hub...
echo.
echo Checking if port 8080 is available...
netstat -ano | findstr :8080 >nul
if %errorlevel% == 0 (
    echo Port 8080 is in use. Attempting to free it...
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080') do (
        echo Killing process %%a...
        taskkill /PID %%a /F >nul 2>&1
    )
    timeout /t 2 >nul
)

echo Starting Spring Boot application...
echo.
echo Application will be available at: http://localhost:8080
echo Press Ctrl+C to stop the application
echo.
mvn spring-boot:run