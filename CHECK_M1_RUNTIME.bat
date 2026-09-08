@echo off
setlocal EnableExtensions
cd /d "%~dp0"

set "LOG=run\logs\latest.log"
if not exist "%LOG%" (
  echo [FAIL] %LOG% does not exist. Run RUN_M1_CLIENT.bat first.
  exit /b 30
)

echo ============================================================
echo  Matter Overdrive 1.20.1 - M1 runtime log verification
echo ============================================================

echo [1/3] Checking registry marker...
findstr /c:"M1 VERIFY: Matter Overdrive registry shell initialized - blocks=75, blockItems=72, standaloneItems=114, sounds=57" "%LOG%" >nul
if errorlevel 1 (
  echo [FAIL] Expected M1 registry marker was not found.
  exit /b 31
)
echo [PASS] Registry marker is correct: 75 / 72 / 114 / 57

echo [2/3] Checking Matter Overdrive model textures...
findstr /c:"Missing textures in model matteroverdrive:" "%LOG%" >nul
if not errorlevel 1 (
  echo [FAIL] Matter Overdrive missing-texture warnings are present.
  echo        Send run\logs\latest.log before continuing.
  exit /b 32
)
echo [PASS] No Matter Overdrive missing-texture model warnings found.

echo [3/3] Checking for Matter Overdrive mod-loading failures...
findstr /i /c:"Failed to load mod matteroverdrive" /c:"matteroverdrive encountered an error" "%LOG%" >nul
if not errorlevel 1 (
  echo [FAIL] A Matter Overdrive mod-loading error was found.
  exit /b 33
)
echo [PASS] No Matter Overdrive mod-loading failure marker found.

echo.
echo ============================================================
echo [PASS] M1 RUNTIME LOG GATE PASSED
echo Next: perform the Creative placement and save/reload gates.
echo ============================================================
exit /b 0
