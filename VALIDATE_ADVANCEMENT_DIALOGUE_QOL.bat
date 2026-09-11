@echo off
setlocal
cd /d "%~dp0"
where py >nul 2>nul
if %errorlevel%==0 (
  py -3 validate_advancement_dialogue_qol.py
) else (
  python validate_advancement_dialogue_qol.py
)
if errorlevel 1 exit /b 1
endlocal
