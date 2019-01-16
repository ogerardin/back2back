@echo off
REM
REM Generate the back2back Windows installer using Inno Setup compiler (Windows native)
REM Prerequisites: scoop
REM

REM Make sure the Inno Setup is installed.
REM Note: We use a custom Scoop Manifest as Inno Setup is not available in the default bucket
cmd /c scoop install inno-setup.json

REM Compile the installer using the command-line compiler (ISCC.exe)
iscc install.iss