@echo off
setlocal EnableExtensions
cd /d "%~dp0"

echo ============================================================
echo  Matter Overdrive 1.20.1 - M2 client

echo  Close Minecraft normally when testing is finished.
echo ============================================================
echo.
call gradlew.bat runClient
set "EXITCODE=%ERRORLEVEL%"
if not "%EXITCODE%"=="0" (
  echo.
  echo [FAIL] Forge client exited with code %EXITCODE%.
  echo        Send run\logs\latest.log and the newest run\crash-reports file if present.
  echo.
  pause
  exit /b %EXITCODE%
)

echo.
echo Client closed normally. Running M2 runtime log check...
call CHECK_M2_RUNTIME.bat
