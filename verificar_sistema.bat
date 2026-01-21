@echo off
echo ============================================
echo   VERIFICADOR DE POSTGRESQL Y SISTEMA
echo   Sistema de Inventario - Repuestos Motos
echo ============================================
echo.

echo [1/4] Verificando servicio de PostgreSQL...
sc query postgresql-x64-16 >nul 2>&1
if %errorlevel% equ 0 (
    echo ✓ Servicio PostgreSQL encontrado
) else (
    sc query postgresql-x64-15 >nul 2>&1
    if %errorlevel% equ 0 (
        echo ✓ Servicio PostgreSQL 15 encontrado
    ) else (
        echo ✗ Servicio PostgreSQL NO encontrado
        echo   Instala PostgreSQL desde: https://www.postgresql.org/download/windows/
    )
)
echo.

echo [2/4] Verificando puerto 5432...
netstat -ano | findstr ":5432" >nul 2>&1
if %errorlevel% equ 0 (
    echo ✓ Puerto 5432 activo - PostgreSQL escuchando
) else (
    echo ✗ Puerto 5432 no activo
    echo   Inicia PostgreSQL: net start postgresql-x64-16
)
echo.

echo [3/4] Verificando puerto 8080 (Backend)...
netstat -ano | findstr ":8080" >nul 2>&1
if %errorlevel% equ 0 (
    echo ✓ Puerto 8080 activo - Backend corriendo
) else (
    echo - Puerto 8080 libre - Backend no iniciado
    echo   Para iniciar: cd backend ^&^& mvn spring-boot:run
)
echo.

echo [4/4] Verificando puerto 3000 (Frontend)...
netstat -ano | findstr ":3000" >nul 2>&1
if %errorlevel% equ 0 (
    echo ✓ Puerto 3000 activo - Frontend corriendo
) else (
    echo - Puerto 3000 libre - Frontend no iniciado
    echo   Para iniciar: cd frontend ^&^& npm start
)
echo.

echo ============================================
echo   INSTRUCCIONES RAPIDAS
echo ============================================
echo.
echo 1. Si PostgreSQL no esta corriendo:
echo    net start postgresql-x64-16
echo.
echo 2. Crear base de datos (solo primera vez):
echo    psql -U postgres -c "CREATE DATABASE inventario_motos;"
echo.
echo 3. Iniciar Backend (en una terminal):
echo    cd D:\Almacen\backend
echo    mvn spring-boot:run
echo.
echo 4. Iniciar Frontend (en otra terminal):
echo    cd D:\Almacen\frontend
echo    npm install
echo    npm start
echo.
echo 5. Abrir en navegador:
echo    http://localhost:3000
echo.
pause

