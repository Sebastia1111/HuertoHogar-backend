@echo off
setlocal
title HuertoHogar - Detener backend
cd /d "%~dp0"

echo ==================================================
echo    HuertoHogar - Deteniendo el backend
echo ==================================================
echo.

rem ---- 1) Detener Gradle: sus procesos en segundo plano bloquean la carpeta ----
if exist "usuarios\gradlew.bat" (
    pushd "usuarios"
    call gradlew.bat --stop >nul 2>nul
    popd
)
echo [OK] Procesos de Gradle detenidos.

rem ---- 2) Cerrar lo que siga corriendo: ----
rem   a) lo que escuche en los puertos del backend (8761, 8082, 8083)
rem   b) los procesos Java que se ejecutan desde esta carpeta
rem   c) las ventanas que abrio iniciar.bat
set "HH_RAIZ=%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -Command "$r = $env:HH_RAIZ.TrimEnd('\').ToLower(); $ids = @(); $ids += Get-NetTCPConnection -State Listen -LocalPort 8761,8082,8083 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess; $ids += Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -and (($_.Name -eq 'java.exe' -and $_.CommandLine.ToLower().Contains($r)) -or ($_.Name -eq 'cmd.exe' -and $_.CommandLine.Contains('gradlew.bat bootRun'))) } | Select-Object -ExpandProperty ProcessId; $ids = @($ids | Where-Object { $_ -gt 0 } | Sort-Object -Unique); foreach ($i in $ids) { Stop-Process -Id $i -Force -ErrorAction SilentlyContinue }; Write-Host ('[OK] Procesos cerrados: ' + $ids.Count)"

echo.
echo Listo: Eureka, Usuarios y Tienda estan detenidos.
echo Ahora puedes mover o eliminar la carpeta sin el aviso de "Acceso denegado".
echo (MySQL de Laragon sigue encendido; se detiene desde Laragon.)
echo.
pause
