@echo off
setlocal EnableExtensions
cd /d "%~dp0"

set "LOG=run\logs\latest.log"

echo ============================================================
echo  Matter Overdrive 1.20.1 - M2 runtime verification
echo ============================================================
echo.

if not exist "%LOG%" goto :fail30

findstr /c:"M1 VERIFY: Matter Overdrive registry shell initialized - blocks=90, blockItems=87, standaloneItems=122, sounds=57" "%LOG%" >nul
if errorlevel 1 goto :fail31
echo [PASS] Live registry counts are correct.
echo        blocks=90, blockItems=87, standaloneItems=122, sounds=57

findstr /c:"M2 VERIFY: machine foundation initialized - blockEntities=19, menus=16" "%LOG%" >nul
if errorlevel 1 goto :fail32
findstr /c:"gunSystem=enabled" "%LOG%" >nul
if errorlevel 1 goto :fail32
findstr /c:"weaponStation=enabled" "%LOG%" >nul
if errorlevel 1 goto :fail32
findstr /c:"energyPipe=enabled" "%LOG%" >nul
if errorlevel 1 goto :fail32
findstr /c:"fusionReactor=enabled" "%LOG%" >nul
if errorlevel 1 goto :fail32
findstr /c:"transporter=enabled" "%LOG%" >nul
if errorlevel 1 goto :fail32
findstr /c:"inscriber=enabled" "%LOG%" >nul
if errorlevel 1 goto :fail32
findstr /c:"decomposer=enabled" "%LOG%" >nul
if errorlevel 1 goto :fail32
findstr /c:"recycler=enabled" "%LOG%" >nul
if errorlevel 1 goto :fail32
findstr /c:"microwave=enabled" "%LOG%" >nul
if errorlevel 1 goto :fail32
findstr /c:"spacetimeAccelerator=enabled" "%LOG%" >nul
if errorlevel 1 goto :fail32
findstr /c:"handheldMatterTools=enabled" "%LOG%" >nul
if errorlevel 1 goto :fail32
findstr /c:"androidAbilities=enabled" "%LOG%" >nul
if errorlevel 1 goto :fail32
findstr /c:"documentationItems=enabled" "%LOG%" >nul
if errorlevel 1 goto :fail32
findstr /c:"systemGuide=enabled" "%LOG%" >nul
if errorlevel 1 goto :fail32
findstr /c:"analyzer=enabled" "%LOG%" >nul
if errorlevel 1 goto :fail32
findstr /c:"replicator=enabled" "%LOG%" >nul
if errorlevel 1 goto :fail32
findstr /c:"patternStorage=enabled" "%LOG%" >nul
if errorlevel 1 goto :fail32
findstr /c:"patternMonitor=enabled" "%LOG%" >nul
if errorlevel 1 goto :fail32
findstr /c:"solarPanel=enabled" "%LOG%" >nul
if errorlevel 1 goto :fail32
findstr /c:"tritaniumCrate=enabled" "%LOG%" >nul
if errorlevel 1 goto :fail32
findstr /c:"networkPipe=enabled" "%LOG%" >nul
if errorlevel 1 goto :fail32
findstr /c:"matterPipe=enabled" "%LOG%" >nul
if errorlevel 1 goto :fail32
findstr /c:"droneFabricator=enabled" "%LOG%" >nul
if errorlevel 1 goto :fail32
findstr /c:"legacyEntities=enabled" "%LOG%" >nul
if errorlevel 1 goto :fail32
findstr /c:"nativeStructures=enabled" "%LOG%" >nul
if errorlevel 1 goto :fail32
findstr /c:"creativeBattery=enabled" "%LOG%" >nul
if errorlevel 1 goto :fail32
echo [PASS] Integrated M2 machine/network/energy/weapon framework marker found.

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
echo [FAIL] M2 machine marker is missing or incomplete.
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
