@echo off
setlocal
where py >nul 2>nul
if %errorlevel%==0 (
  py -3 scripts\validate_lore_events.py
) else (
  python scripts\validate_lore_events.py
)
exit /b %errorlevel%
