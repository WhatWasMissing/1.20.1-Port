@echo off
setlocal
cd /d "%~dp0"
where py >nul 2>nul
if %ERRORLEVEL%==0 (
  py -3 validate_pda_guideme_integration.py
) else (
  python validate_pda_guideme_integration.py
)
set RESULT=%ERRORLEVEL%
if not %RESULT%==0 (
  echo.
  echo PDA / GuideME validation failed.
  exit /b %RESULT%
)
echo.
echo PDA / GuideME validation passed.
exit /b 0
