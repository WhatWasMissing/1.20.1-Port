@echo off
setlocal EnableExtensions EnableDelayedExpansion

cd /d "%~dp0"

echo ============================================================
echo Matter Overdrive guarded local build
echo ============================================================
echo.

set PYTHON_CMD=
where py >nul 2>nul
if %errorlevel%==0 (
    set PYTHON_CMD=py -3
) else (
    where python >nul 2>nul
    if not %errorlevel%==0 (
        echo ERROR: Python 3 was not found. Install Python 3 or run the validators manually before building.
        exit /b 2
    )
    set PYTHON_CMD=python
)

%PYTHON_CMD% scripts\validate_model_bounds.py
if not %errorlevel%==0 (
    echo.
    echo Build stopped because a block model failed validation.
    exit /b 1
)

if exist "scripts\validate_structure_expansion.py" (
    echo.
    echo Running dormant-structure placement validation...
    %PYTHON_CMD% scripts\validate_structure_expansion.py
    if not %errorlevel%==0 (
        echo.
        echo Build stopped because the structure placement gate failed.
        exit /b 1
    )
)

if exist "scripts\validate_lore_events.py" (
    echo.
    echo Running player-event lore validation...
    %PYTHON_CMD% scripts\validate_lore_events.py
    if not %errorlevel%==0 (
        echo.
        echo Build stopped because the lore event gate failed.
        exit /b 1
    )
)

if exist "scripts\validate_tech_overhaul.py" (
    echo.
    echo Running tech-overhaul packaging validation...
    %PYTHON_CMD% scripts\validate_tech_overhaul.py
    if not %errorlevel%==0 (
        echo.
        echo Build stopped because the tech-overhaul packaging gate failed.
        exit /b 1
    )
)

echo.
echo Static validation passed. Starting clean Gradle build...
call gradlew.bat clean build --stacktrace
set BUILD_RESULT=%errorlevel%

if not %BUILD_RESULT%==0 (
    echo.
    echo Gradle build failed with exit code %BUILD_RESULT%.
    exit /b %BUILD_RESULT%
)

set JAR_COUNT=0
echo.
echo ============================================================
echo BUILD PASSED
echo ============================================================
echo Production JAR output:
for %%F in ("build\libs\*.jar") do (
    if exist "%%~fF" (
        set /a JAR_COUNT+=1
        echo   %%~fF
        for %%S in ("%%~fF") do echo     size: %%~zS bytes
        where certutil >nul 2>nul
        if !errorlevel!==0 (
            echo     SHA-256:
            certutil -hashfile "%%~fF" SHA256 | findstr /R /V /C:"hash of file" /C:"CertUtil"
        )
    )
)

if !JAR_COUNT!==0 (
    echo.
    echo ERROR: Gradle reported success but no JAR exists in build\libs\.
    exit /b 3
)

echo.
echo Built !JAR_COUNT! JAR file(s). Copy the production MatterOverdrive JAR from build\libs\ into the test instance mods folder.
echo ============================================================
exit /b 0
