@echo off
REM
REM Generate the back2back Mac package using jar2app
REM Prerequisites: scoop
REM

where scoop >NUL 2>NUL || ( echo Missing prerequisite: scoop & exit /b )

cmd /c scoop install jar2app.json

jar2app %*