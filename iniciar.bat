@echo off
setlocal
title HuertoHogar - Iniciar backend
cd /d "%~dp0"

echo ==================================================
echo    HuertoHogar - Iniciando el backend
echo ==================================================
echo.

rem ---- 1) Java instalado ----
where java >nul 2>nul
if errorlevel 1 (
    echo [ERROR] No se encontro Java. Instala el JDK 21 o superior y vuelve a intentarlo.
    pause
    exit /b 1
)

rem ---- 2) Version de Java: se compila con el JDK que tengas instalado ----
set "JAVA_TXT="
set "JAVA_MAYOR="
for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do set "JAVA_TXT=%%~v"
for /f "delims=." %%m in ("%JAVA_TXT%") do set "JAVA_MAYOR=%%m"
if "%JAVA_MAYOR%"=="" set "JAVA_MAYOR=21"
if %JAVA_MAYOR% LSS 21 (
    echo [ERROR] Tienes Java %JAVA_MAYOR%. El proyecto necesita Java 21 o superior.
    pause
    exit /b 1
)
echo [OK] Java %JAVA_MAYOR% detectado.

rem ---- 3) MySQL de Laragon (puerto 3306) ----
netstat -an | find ":3306 " >nul
if errorlevel 1 (
    echo.
    echo [AVISO] MySQL no responde en el puerto 3306.
    echo         Abre Laragon, presiona "Iniciar todo" y luego cualquier tecla aqui.
    pause >nul
) else (
    echo [OK] MySQL responde en el puerto 3306.
)

rem ---- 4) Abrir los 3 servicios, cada uno en su propia ventana ----
rem El orden no importa: usuarios y tienda funcionan aunque Eureka tarde en arrancar.
echo.
echo Abriendo Eureka, Usuarios y Tienda...
start "HuertoHogar - Eureka (8761)"   /d "%~dp0eureka-server" cmd /k gradlew.bat bootRun -PjavaVersion=%JAVA_MAYOR%
start "HuertoHogar - Usuarios (8082)" /d "%~dp0usuarios"      cmd /k gradlew.bat bootRun -PjavaVersion=%JAVA_MAYOR%
start "HuertoHogar - Tienda (8083)"   /d "%~dp0tienda"        cmd /k gradlew.bat bootRun -PjavaVersion=%JAVA_MAYOR%

echo.
echo Listo. Cuando cada ventana muestre "Started ...Application", el servicio esta arriba.
echo La primera vez tarda varios minutos (descarga Gradle y las librerias).
echo.
echo    Eureka    http://localhost:8761
echo    Usuarios  http://localhost:8082/swagger-ui.html
echo    Tienda    http://localhost:8083/swagger-ui.html
echo.
echo Para detener todo (y liberar la carpeta), usa detener.bat.
echo.
pause
