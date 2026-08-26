@echo off
setlocal EnableExtensions
set "GRADLE_VERSION=8.1.1"
set "GRADLE_SHA256=e111cb9948407e26351227dabce49822fb88c37ee72f1d1582a69c68af2e702f"
set "DIST_URL=https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip"
set "BOOT_ROOT=%USERPROFILE%\.gradle\matteroverdrive-bootstrap"
set "DIST_DIR=%BOOT_ROOT%\gradle-%GRADLE_VERSION%"
set "ZIP_FILE=%BOOT_ROOT%\gradle-%GRADLE_VERSION%-bin.zip"
set "GRADLE_BAT=%DIST_DIR%\bin\gradle.bat"

if exist "%GRADLE_BAT%" goto run

where powershell.exe >nul 2>&1
if errorlevel 1 (
  echo ERROR: PowerShell is required for the first Gradle bootstrap.
  exit /b 2
)

if not exist "%BOOT_ROOT%" mkdir "%BOOT_ROOT%"

echo [MatterOverdrive] Gradle %GRADLE_VERSION% is not cached.
echo [MatterOverdrive] Downloading %DIST_URL%
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest -UseBasicParsing -Uri '%DIST_URL%' -OutFile '%ZIP_FILE%'"
if errorlevel 1 (
  echo ERROR: Gradle download failed.
  exit /b 3
)

for /f "usebackq delims=" %%H in (`powershell.exe -NoProfile -Command "(Get-FileHash -Algorithm SHA256 -LiteralPath '%ZIP_FILE%').Hash.ToLowerInvariant()"`) do set "ACTUAL_SHA=%%H"
if /I not "%ACTUAL_SHA%"=="%GRADLE_SHA256%" (
  echo ERROR: Gradle checksum mismatch.
  echo Expected: %GRADLE_SHA256%
  echo Actual:   %ACTUAL_SHA%
  del /q "%ZIP_FILE%" >nul 2>&1
  exit /b 4
)

echo [MatterOverdrive] Extracting Gradle...
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -LiteralPath '%ZIP_FILE%' -DestinationPath '%BOOT_ROOT%' -Force"
if errorlevel 1 (
  echo ERROR: Gradle extraction failed.
  exit /b 5
)
del /q "%ZIP_FILE%" >nul 2>&1

:run
if not exist "%GRADLE_BAT%" (
  echo ERROR: Gradle bootstrap did not produce %GRADLE_BAT%
  exit /b 6
)
call "%GRADLE_BAT%" %*
exit /b %ERRORLEVEL%
