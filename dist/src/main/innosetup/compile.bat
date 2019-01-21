@echo off
REM
REM Generate the back2back Windows installer using Inno Setup compiler (Windows native)
REM Prerequisites: scoop
REM

where scoop >NUL 2>NUL || (
    echo Missing prerequisite: scoop
    exit /b
)

REM Make sure the Inno Setup is installed.
REM Note: We use a custom Scoop Manifest as Inno Setup is not available in the default bucket
cmd /c scoop install inno-setup.json
FOR /F "tokens=*" %%g IN ('scoop prefix inno-setup') do (SET INNO_SETUP_HOME=%%g)
echo INNO_SETUP_HOME=%INNO_SETUP_HOME%

REM Make sure the Inno Download Plugin is installed
REM Note: We use a custom Scoop Manifest as Inno Download Plugin is not available in the default bucket
cmd /c scoop install inno-download-plugin.json
FOR /F "tokens=*" %%g IN ('scoop prefix inno-download-plugin') do (SET INNO_DOWNLOAD_PLUGIN_HOME=%%g)
echo INNO_DOWNLOAD_PLUGIN_HOME=%INNO_DOWNLOAD_PLUGIN_HOME%

REM add the include path to the default Inno Setup config
set ISSPPBUILTINS_FILE=%INNO_SETUP_HOME%\ISPPBuiltins.iss
echo ISSPPBUILTINS_FILE=%ISSPPBUILTINS_FILE%
find /c "%INNO_DOWNLOAD_PLUGIN_HOME%" "%ISSPPBUILTINS_FILE%" >NUL || (
    echo Updating %ISSPPBUILTINS_FILE%
    echo #pragma include __INCLUDE__ + ";" + "%INNO_DOWNLOAD_PLUGIN_HOME%" >> %ISSPPBUILTINS_FILE%
)

REM Compile the installer using the command-line compiler (ISCC.exe)
iscc install.iss