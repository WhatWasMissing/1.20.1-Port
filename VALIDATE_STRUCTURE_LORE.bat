@echo off
setlocal
where py >nul 2>nul
if %errorlevel%==0 (
  py -3 validate_structure_lore.py
) else (
  python validate_structure_lore.py
)
exit /b %errorlevel%
