@echo off
setlocal

cd /d "%~dp0"

echo ============================================================
echo Matter Overdrive guarded local build
echo ============================================================
echo.

where py >nul 2>nul
if %errorlevel%==0 (
    py -3 scripts\validate_model_bounds.py
) else (
    where python >nul 2>nul
    if not %errorlevel%==0 (
        echo ERROR: Python 3 was not found. Install Python 3 or run the validator manually before building.
        exit /b 2
    )
    python scripts\validate_model_bounds.py
)

if not %errorlevel%==0 (
    echo.
    echo Build stopped because a block model failed validation.
    exit /b 1
)

echo.
echo Model validation passed. Starting clean Gradle build...
call gradlew.bat clean build --stacktrace
set BUILD_RESULT=%errorlevel%

if not %BUILD_RESULT%==0 (
    echo.
    echo Gradle build failed with exit code %BUILD_RESULT%.
    exit /b %BUILD_RESULT%
)

echo.
echo ============================================================
echo BUILD PASSED
echo Production JAR(s) are in build\libs\
echo ============================================================
exit /b 0
