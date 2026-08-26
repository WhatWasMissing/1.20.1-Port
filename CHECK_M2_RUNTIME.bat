@echo off
setlocal EnableExtensions
cd /d "%~dp0"

set "EXITCODE=0"
set "LOG=run\logs\latest.log"

echo ============================================================
echo  Matter Overdrive 1.20.1 - M2 runtime verification
echo ============================================================
echo.

if not exist "%LOG%" (
  echo [FAIL] %LOG% does not exist.
  set "EXITCODE=30"
  goto :finish
)

findstr /c:"M1 VERIFY: Matter Overdrive registry shell initialized - blocks=75, blockItems=72, standaloneItems=98, sounds=57" "%LOG%" >nul
if errorlevel 1 (
  echo [FAIL] M1 registry marker is missing.
  set "EXITCODE=31"
  goto :finish
)
echo [PASS] M1 registry counts are still intact.

findstr /c:"M2 VERIFY: machine foundation initialized - blockEntities=6, menus=6, decomposer=enabled, recycler=enabled, analyzer=enabled, replicator=enabled, patternStorage=enabled, patternMonitor=enabled, networkPipe=enabled, matterPipe=enabled, creativeBattery=enabled" "%LOG%" >nul
if errorlevel 1 (
  echo [FAIL] M2 machine marker is missing.
  set "EXITCODE=32"
  goto :finish
)
echo [PASS] M2 machine/network framework marker found.

findstr /c:"Missing textures in model matteroverdrive:" "%LOG%" >nul
if not errorlevel 1 (
  echo [FAIL] Matter Overdrive missing-texture model warnings are present.
  set "EXITCODE=33"
  goto :finish
)
echo [PASS] No Matter Overdrive missing-texture model warnings found.

findstr /i /c:"matteroverdrive encountered an error" /c:"Failed to load mod matteroverdrive" /c:"Exception ticking block entity" "%LOG%" >nul
if not errorlevel 1 (
  echo [FAIL] A mod-loading or block-entity failure marker was found.
  set "EXITCODE=34"
  goto :finish
)
echo [PASS] No Matter Overdrive runtime failure marker found.

echo.
echo ============================================================
echo [PASS] M2 RUNTIME LOG GATE PASSED
echo ============================================================

:finish
if not "%EXITCODE%"=="0" (
  echo.
  echo ============================================================
  echo [FAIL] M2 runtime verification stopped with error code %EXITCODE%.
  echo ============================================================
)
echo.
pause
exit /b %EXITCODE%
