@echo off
setlocal
cd /d "%~dp0"
if "%~1"=="" (
  echo Usage: INJECT_PDA_VOICES_INTO_JAR.bat path\to\matteroverdrive-0.6.jar
  echo.
  echo Example:
  echo   INJECT_PDA_VOICES_INTO_JAR.bat build\libs\matteroverdrive-0.6.jar
  exit /b 2
)
where py >nul 2>nul
if %errorlevel%==0 (
  py -3 tools\pda_voicebank\inject_into_jar.py "%~1"
) else (
  python tools\pda_voicebank\inject_into_jar.py "%~1"
)
if errorlevel 1 exit /b 1
echo.
echo PDA voice JAR injection complete.
endlocal
