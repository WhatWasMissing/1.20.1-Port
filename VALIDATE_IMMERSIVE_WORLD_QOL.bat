@echo off
setlocal
cd /d "%~dp0"
where py >nul 2>nul
if %errorlevel%==0 (
  py -3 validate_immersive_world_qol.py
  if errorlevel 1 goto :failed
  py -3 validate_environmental_dressing.py
  if errorlevel 1 goto :failed
) else (
  python validate_immersive_world_qol.py
  if errorlevel 1 goto :failed
  python validate_environmental_dressing.py
  if errorlevel 1 goto :failed
)
echo.
echo Immersive world / QoL validation complete.
exit /b 0

:failed
echo.
echo Immersive world / QoL validation FAILED.
exit /b 1
