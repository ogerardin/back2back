@echo off

cd ${INSTALL_PATH}

reg Query "HKLM\Hardware\Description\System\CentralProcessor\0" | find /i "x86" > NUL && set BITNESS=32 || set BITNESS=64
set PATH=.\nssm\win%BITNESS%;%PATH%

nssm.exe install ${serviceName} "java -jar ${INSTALL_PATH}\lib\${back2back.core.jar}"
nssm.exe start ${serviceName}
