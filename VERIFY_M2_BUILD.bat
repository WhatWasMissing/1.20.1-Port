@echo off
setlocal EnableExtensions
cd /d "%~dp0"

set "EXITCODE=0"

echo ============================================================
echo  Matter Overdrive 1.20.1 - M2 build verification
echo ============================================================
echo.

where java.exe >nul 2>&1
if errorlevel 1 (
  echo [FAIL] java.exe was not found on PATH.
  set "EXITCODE=20"
  goto :finish
)
where javac.exe >nul 2>&1
if errorlevel 1 (
  echo [FAIL] javac.exe was not found on PATH. Install a JDK 17, not only a JRE.
  set "EXITCODE=21"
  goto :finish
)

for /f "tokens=2" %%V in ('javac -version 2^>^&1') do set "JAVAC_VERSION=%%V"
for /f "tokens=1 delims=." %%M in ("%JAVAC_VERSION%") do set "JAVA_MAJOR=%%M"
if not "%JAVA_MAJOR%"=="17" (
  echo [FAIL] JDK 17 is required. javac reports %JAVAC_VERSION%.
  set "EXITCODE=22"
  goto :finish
)

echo [PASS] JDK 17 detected: %JAVAC_VERSION%
echo.

echo [1/4] Gradle bootstrap...
call gradlew.bat --version
if errorlevel 1 (
  set "EXITCODE=23"
  goto :finish
)

echo.
echo [2/4] Re-running the verified M1 resource gate...
call gradlew.bat verifyM1Resources --stacktrace
if errorlevel 1 (
  set "EXITCODE=24"
  goto :finish
)

echo.
echo [3/4] Checking the M2 functional-machines source gate...
call gradlew.bat verifyM2Sources --stacktrace
if errorlevel 1 (
  set "EXITCODE=25"
  goto :finish
)

echo.
echo [4/4] Clean Forge compilation...
call gradlew.bat clean build --stacktrace
if errorlevel 1 (
  set "EXITCODE=26"
  goto :finish
)

if not exist "build\libs\matteroverdrive-0.8.0.0-alpha.4.1.jar" (
  echo [FAIL] Build completed but expected JAR was not found.
  set "EXITCODE=27"
  goto :finish
)

echo.
echo ============================================================
echo [PASS] M2 BUILD GATE PASSED
echo ============================================================
echo JAR: build\libs\matteroverdrive-0.8.0.0-alpha.4.1.jar
echo.

:finish
if not "%EXITCODE%"=="0" (
  echo.
  echo ============================================================
  echo [FAIL] M2 build verification stopped with error code %EXITCODE%.
  echo       Send the complete console output before continuing.
  echo ============================================================
)
echo.
pause
exit /b %EXITCODE%
