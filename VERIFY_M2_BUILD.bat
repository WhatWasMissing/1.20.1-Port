@echo off
setlocal EnableExtensions
cd /d "%~dp0"

set "EXITCODE=0"
set "PYTHON_CMD="

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

where py.exe >nul 2>&1
if not errorlevel 1 set "PYTHON_CMD=py -3"
if not defined PYTHON_CMD (
  where python.exe >nul 2>&1
  if not errorlevel 1 set "PYTHON_CMD=python"
)
if not defined PYTHON_CMD (
  echo [FAIL] Python 3 is required for the static structure and consistency gates.
  set "EXITCODE=28"
  goto :finish
)

echo.
echo [1/12] Facility layout reachability gate...
%PYTHON_CMD% scripts\facility_layout_lab.py --check-only
if errorlevel 1 (
  set "EXITCODE=29"
  goto :finish
)

echo.
echo [2/12] Native structure expansion gate...
%PYTHON_CMD% scripts\validate_structure_expansion.py
if errorlevel 1 (
  set "EXITCODE=30"
  goto :finish
)

echo.
echo [3/12] Lore event routing gate...
%PYTHON_CMD% scripts\validate_lore_events.py
if errorlevel 1 (
  set "EXITCODE=36"
  goto :finish
)

echo.
echo [4/12] Whole-port consistency audit...
%PYTHON_CMD% scripts\validate_port_consistency.py
if errorlevel 1 (
  set "EXITCODE=31"
  goto :finish
)

echo.
echo [5/12] Android power/progression consistency gate...
%PYTHON_CMD% scripts\validate_android_consistency.py
if errorlevel 1 (
  set "EXITCODE=32"
  goto :finish
)

echo.
echo [6/12] Energy-weapon consistency gate...
%PYTHON_CMD% scripts\validate_weapon_consistency.py
if errorlevel 1 (
  set "EXITCODE=33"
  goto :finish
)

echo.
echo [7/12] Network/Transporter consistency gate...
%PYTHON_CMD% scripts\validate_network_transport_consistency.py
if errorlevel 1 (
  set "EXITCODE=34"
  goto :finish
)

echo.
echo [8/12] Weapon Renderer 2.0 consistency gate...
%PYTHON_CMD% scripts\validate_weapon_renderer_consistency.py
if errorlevel 1 (
  set "EXITCODE=35"
  goto :finish
)

echo.
echo [9/12] Gradle bootstrap...
call gradlew.bat --version
if errorlevel 1 (
  set "EXITCODE=23"
  goto :finish
)

echo.
echo [10/12] Re-running the verified M1 resource gate...
call gradlew.bat verifyM1Resources --stacktrace
if errorlevel 1 (
  set "EXITCODE=24"
  goto :finish
)

echo.
echo [11/12] Checking the M2 functional-machines source gate...
call gradlew.bat verifyM2Sources --stacktrace
if errorlevel 1 (
  set "EXITCODE=25"
  goto :finish
)

echo.
echo [12/12] Clean Forge compilation...
call gradlew.bat clean build --stacktrace
if errorlevel 1 (
  set "EXITCODE=26"
  goto :finish
)

if not exist "build\libs\*.jar" (
  echo [FAIL] Build completed but no JAR was found under build\libs.
  set "EXITCODE=27"
  goto :finish
)

echo.
echo ============================================================
echo [PASS] M2 BUILD GATE PASSED
echo ============================================================
echo Built JARs:
dir /b "build\libs\*.jar"
echo.
echo Static report: build\reports\m2-port-consistency.md
echo Facility report: build\reports\facility_layout_lab\facility_layout_lab.json
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
