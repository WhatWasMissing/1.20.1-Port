@echo off
setlocal
cd /d "%~dp0"
where py >nul 2>nul
if %errorlevel%==0 (
  py -3 validate_immersive_world_qol_hardening.py
) else (
  python validate_immersive_world_qol_hardening.py
)
if errorlevel 1 exit /b 1
echo.
echo Immersive world/QoL hardening checks passed.
endlocal
