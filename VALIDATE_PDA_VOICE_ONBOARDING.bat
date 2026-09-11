@echo off
setlocal
cd /d "%~dp0"
where py >nul 2>nul
if %errorlevel%==0 (
  py -3 validate_pda_voice_onboarding.py
) else (
  python validate_pda_voice_onboarding.py
)
if errorlevel 1 exit /b 1
endlocal
