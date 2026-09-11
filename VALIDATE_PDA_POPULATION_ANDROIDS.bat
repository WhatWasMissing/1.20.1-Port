@echo off
setlocal
where py >nul 2>nul
if %errorlevel%==0 (
  py -3 validate_pda_population_androids.py
) else (
  python validate_pda_population_androids.py
)
exit /b %errorlevel%
