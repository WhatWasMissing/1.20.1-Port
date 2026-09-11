@echo off
setlocal
cd /d "%~dp0"
where py >nul 2>nul
if %errorlevel%==0 (
  py -3 validate_structure_topology.py
) else (
  python validate_structure_topology.py
)
set RC=%errorlevel%
if not "%RC%"=="0" (
  echo.
  echo Structure topology validation FAILED with exit code %RC%.
) else (
  echo.
  echo Structure topology validation PASSED.
)
exit /b %RC%
