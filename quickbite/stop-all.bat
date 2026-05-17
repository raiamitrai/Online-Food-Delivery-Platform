@echo off
echo Forcefully stopping all QuickBite Java Services...
taskkill /IM java.exe /F
echo.
echo All backend services have been stopped!
pause
