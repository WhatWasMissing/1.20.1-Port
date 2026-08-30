@echo off
setlocal EnableExtensions
cd /d "%~dp0"

echo ============================================================
echo  Matter Overdrive 1.20.1 - Test JAR packager
echo ============================================================
echo.

call gradlew.bat jar
if errorlevel 1 goto :failed

echo.
echo ============================================================
echo [PASS] Test JAR created:
for %%F in (build\libs\matteroverdrive-*.jar) do echo        %%F
echo ============================================================
echo.
pause
exit /b 0

:failed
echo.
echo ============================================================
echo [FAIL] JAR packaging failed.
echo ============================================================
echo.
pause
exit /b 1
