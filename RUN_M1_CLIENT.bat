@echo off
setlocal
cd /d "%~dp0"
echo Starting the Forge 1.20.1 development client...
echo Keep this window open while Minecraft is running.
echo.
call gradlew.bat runClient
set "RC=%ERRORLEVEL%"
echo.
if not "%RC%"=="0" (
  echo [FAIL] runClient exited with code %RC%.
  echo Send me run\logs\latest.log and the newest file in run\crash-reports if one exists.
  pause
  exit /b %RC%
)

echo runClient exited normally.
echo Running M1 runtime log checks...
call CHECK_M1_RUNTIME.bat
set "CHECK_RC=%ERRORLEVEL%"
if not "%CHECK_RC%"=="0" (
  echo.
  echo [FAIL] Client exited normally, but the M1 runtime log gate failed.
  pause
  exit /b %CHECK_RC%
)

echo.
pause
exit /b 0
