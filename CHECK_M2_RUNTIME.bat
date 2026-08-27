@echo off
setlocal EnableExtensions
cd /d "%~dp0"

set "LOG=run\logs\latest.log"

echo ============================================================
echo  Matter Overdrive 1.20.1 - M2 runtime verification
echo ============================================================
echo.

if not exist "%LOG%" goto :fail30

findstr /c:"M1 VERIFY: Matter Overdrive registry shell initialized - blocks=74, blockItems=71, standaloneItems=97, sounds=57" "%LOG%" >nul
if errorlevel 1 goto :fail31
echo [PASS] Live registry counts are correct.
echo        blocks=74, blockItems=71, standaloneItems=97, sounds=57

findstr /c:"M2 VERIFY: machine foundation initialized - blockEntities=6, menus=6, decomposer=enabled, recycler=enabled, analyzer=enabled, replicator=enabled, patternStorage=enabled, patternMonitor=enabled, networkPipe=enabled, matterPipe=enabled, creativeBattery=enabled" "%LOG%" >nul
if errorlevel 1 goto :fail32
echo [PASS] M2 machine/network framework marker found.

findstr /c:"Missing textures in model matteroverdrive:" "%LOG%" >nul
if not errorlevel 1 goto :fail33
echo [PASS] No Matter Overdrive missing-texture model warnings found.

findstr /i /c:"matteroverdrive encountered an error" /c:"Failed to load mod matteroverdrive" /c:"Exception ticking block entity" "%LOG%" >nul
if not errorlevel 1 goto :fail34
echo [PASS] No Matter Overdrive runtime failure marker found.

findstr /i /c:"Couldn't load tag matteroverdrive:" "%LOG%" >nul
if not errorlevel 1 goto :fail35
echo [PASS] No Matter Overdrive data-pack tag failures found.

echo.
echo ============================================================
echo [PASS] M2 RUNTIME LOG GATE PASSED
echo ============================================================
echo.
pause
exit /b 0

:fail30
echo [FAIL] %LOG% does not exist.
set "EXITCODE=30"
goto :failure

:fail31
echo [FAIL] Live registry marker is missing or has unexpected counts.
set "EXITCODE=31"
goto :failure

:fail32
echo [FAIL] M2 machine marker is missing.
set "EXITCODE=32"
goto :failure

:fail33
echo [FAIL] Matter Overdrive missing-texture model warnings are present.
set "EXITCODE=33"
goto :failure

:fail34
echo [FAIL] A mod-loading or block-entity failure marker was found.
set "EXITCODE=34"
goto :failure

:fail35
echo [FAIL] A Matter Overdrive data-pack tag failed to load.
set "EXITCODE=35"
goto :failure

:failure
echo.
echo ============================================================
echo [FAIL] M2 runtime verification stopped with error code %EXITCODE%.
echo ============================================================
echo.
pause
exit /b %EXITCODE%
