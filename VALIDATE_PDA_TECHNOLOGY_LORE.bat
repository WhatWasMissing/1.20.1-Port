@echo off
setlocal
cd /d "%~dp0"
python validate_pda_technology_lore.py
if errorlevel 1 (
  echo.
  echo PDA TECHNOLOGY LORE VALIDATION FAILED.
  exit /b 1
)
echo.
echo PDA TECHNOLOGY LORE VALIDATION PASSED.
endlocal
