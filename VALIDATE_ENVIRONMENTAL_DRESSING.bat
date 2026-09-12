@echo off
setlocal
cd /d "%~dp0"
where py >nul 2>nul
if %errorlevel%==0 (
  py -3 validate_environmental_dressing.py
) else (
  python validate_environmental_dressing.py
)
if errorlevel 1 exit /b 1
echo.
echo Environmental dressing validation complete.
endlocal
