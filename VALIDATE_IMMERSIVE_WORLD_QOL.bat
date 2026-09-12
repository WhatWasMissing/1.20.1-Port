@echo off
setlocal
cd /d "%~dp0"
where py >nul 2>nul
if %errorlevel%==0 (
  py -3 validate_immersive_world_qol.py
) else (
  python validate_immersive_world_qol.py
)
if errorlevel 1 (
  echo.
  echo Immersive world / QoL validation FAILED.
  exit /b 1
)
echo.
echo Immersive world / QoL validation complete.
endlocal
