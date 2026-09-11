@echo off
setlocal
where py >nul 2>nul
if %errorlevel%==0 (
    py -3 validate_pda_presentation.py
) else (
    python validate_pda_presentation.py
)
set EXIT_CODE=%errorlevel%
if not %EXIT_CODE%==0 (
    echo.
    echo PDA presentation validation failed.
)
exit /b %EXIT_CODE%
