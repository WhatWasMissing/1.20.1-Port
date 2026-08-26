@echo off
setlocal EnableExtensions
cd /d "%~dp0"

echo ============================================================
echo  Matter Overdrive 1.20.1 - Milestone 1 build verification
echo ============================================================
echo.

where java.exe >nul 2>&1
if errorlevel 1 (
  echo [FAIL] Java was not found in PATH.
  echo Install a 64-bit JDK 17, then reopen Command Prompt.
  pause
  exit /b 10
)
where javac.exe >nul 2>&1
if errorlevel 1 (
  echo [FAIL] javac was not found. A full JDK 17 is required, not only a JRE.
  pause
  exit /b 10
)

for /f "tokens=2" %%V in ('javac -version 2^>^&1') do set "JAVAC_VERSION=%%V"
for /f "tokens=1 delims=." %%V in ("%JAVAC_VERSION%") do set "JAVA_MAJOR=%%V"
for /f "tokens=3" %%V in ('java -XshowSettings:properties -version 2^>^&1 ^| findstr /c:"sun.arch.data.model ="') do set "JAVA_BITS=%%V"
echo JDK: %JAVAC_VERSION%  ^(%JAVA_BITS%-bit^)
if not "%JAVA_MAJOR%"=="17" (
  echo [FAIL] This workspace must be tested with JDK 17.
  echo Run: java -version
  pause
  exit /b 11
)
if not "%JAVA_BITS%"=="64" (
  echo [FAIL] Forge requires a 64-bit JVM for this test.
  pause
  exit /b 12
)

echo.
echo [1/3] Checking Gradle bootstrap...
call gradlew.bat --version
if errorlevel 1 goto gradlefail

echo.
echo [2/3] Running M1 resource verification...
call gradlew.bat verifyM1Resources --stacktrace
if errorlevel 1 goto buildfail

echo.
echo [3/3] Compiling and packaging Forge mod...
call gradlew.bat clean build --stacktrace
if errorlevel 1 goto buildfail

echo.
echo ============================================================
echo [PASS] M1 BUILD GATE PASSED
echo Expected jar: build\libs\matteroverdrive-0.8.0.0-alpha.4.1.jar
echo Next: run RUN_M1_CLIENT.bat
echo ============================================================
pause
exit /b 0

:gradlefail
echo.
echo [FAIL] Gradle bootstrap failed.
echo Send me this window output.
pause
exit /b 20

:buildfail
echo.
echo [FAIL] M1 build/resource verification failed.
echo Send me this window output and, if present, build\reports or the stacktrace.
pause
exit /b 21
