@echo off
setlocal
cd /d "%~dp0"
where ffmpeg >nul 2>nul
if errorlevel 1 (
  echo ERROR: ffmpeg is required on PATH.
  exit /b 2
)
where py >nul 2>nul
if %errorlevel%==0 (
  py -3 tools\pda_voicebank\process_voice_bank.py
) else (
  python tools\pda_voicebank\process_voice_bank.py
)
if errorlevel 1 exit /b 1
endlocal
