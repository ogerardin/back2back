@echo off

REM Generate the Windows installer using Inno Setup (native)
REM Prerequisites: scoop

REM We use a custom Scoop Manifest to download and install Inno Setup
cmd /c scoop install inno-setup.json

REM Compile the installer
iscc install.iss