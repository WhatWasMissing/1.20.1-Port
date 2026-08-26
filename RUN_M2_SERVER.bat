@echo off
setlocal EnableExtensions
cd /d "%~dp0"

echo ============================================================
echo  Matter Overdrive 1.20.1 - M2 dedicated server test
echo ============================================================
echo When the server reaches "Done", the M2 server gate has passed.
echo Because Gradle userdev may not forward the Minecraft "stop" command,
echo use Ctrl+C after you have seen Done.
echo.
call gradlew.bat --console=plain runServer
set "EXITCODE=%ERRORLEVEL%"
echo.
if "%EXITCODE%"=="0" (
  echo Server process exited normally.
) else (
  echo Server process exited with code %EXITCODE%.
  echo If you intentionally used Ctrl+C after seeing Done, that is expected.
)
echo.
pause
exit /b %EXITCODE%
