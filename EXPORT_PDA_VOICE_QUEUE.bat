@echo off
setlocal
cd /d "%~dp0"
python tools\pda_voicebank\export_generation_queue.py
if errorlevel 1 (
  echo.
  echo PDA voice queue export FAILED.
  exit /b 1
)
echo.
echo PDA voice queue export complete.
endlocal
