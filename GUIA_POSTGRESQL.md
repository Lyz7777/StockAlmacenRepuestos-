# Guía de Configuración de PostgreSQL para el Sistema de Inventario

## 📥 PASO 1: Instalar PostgreSQL

### Opción A: Descargar desde el sitio oficial
1. Ve a: https://www.postgresql.org/download/windows/
2. Descarga el instalador de PostgreSQL 16 (o la versión más reciente)
3. Ejecuta el instalador

### Durante la instalación:
- **Puerto**: Deja el puerto por defecto **5432**
- **Contraseña**: Establece una contraseña para el usuario `postgres` (¡ANÓTALA!)
- **Locale**: Selecciona "Spanish, Spain" o "Default locale"
- Marca la opción de instalar **pgAdmin 4** (herramienta gráfica)

---

## 🔧 PASO 2: Verificar que PostgreSQL está funcionando

### Método 1: Usando la línea de comandos

Abre CMD como administrador y ejecuta:

```cmd
# Verificar si el servicio está corriendo
sc query postgresql-x64-16

# O con este comando
net start | findstr -i postgres
```

### Método 2: Conectarse a PostgreSQL

Abre CMD y ejecuta:

```cmd
# Ir al directorio de PostgreSQL (ajusta la versión si es diferente)
cd "C:\Program Files\PostgreSQL\16\bin"

# Conectarse a PostgreSQL
psql -U postgres -h localhost -p 5432
```

Te pedirá la contraseña que estableciste durante la instalación.

Si ves esto, ¡PostgreSQL está funcionando!:
```
psql (16.x)
Type "help" for help.

postgres=#
```

---

## 🗄️ PASO 3: Crear la Base de Datos

### Opción A: Usando psql (línea de comandos)

Una vez conectado a psql, ejecuta:

```sql
-- Crear la base de datos
CREATE DATABASE inventario_motos;

-- Verificar que se creó
\l

-- Conectarse a la nueva base de datos
\c inventario_motos

-- Salir
\q
```

### Opción B: Usando pgAdmin 4 (interfaz gráfica)

1. Abre **pgAdmin 4** desde el menú de inicio
2. En el panel izquierdo, expande "Servers"
3. Haz clic derecho en "PostgreSQL 16" → "Connect Server"
4. Ingresa tu contraseña
5. Haz clic derecho en "Databases" → "Create" → "Database"
6. Nombre: `inventario_motos`
7. Click en "Save"

---

## ⚙️ PASO 4: Configurar el Backend

Edita el archivo `D:\Almacen\backend\src\main\resources\application.properties`:

```properties
# Configuración de la base de datos
spring.datasource.url=jdbc:postgresql://localhost:5432/inventario_motos
spring.datasource.username=postgres
spring.datasource.password=TU_CONTRASEÑA_AQUI
```

**¡IMPORTANTE!** Reemplaza `TU_CONTRASEÑA_AQUI` con la contraseña que estableciste.

---

## 🧪 PASO 5: Probar la conexión

### Test rápido con psql:

```cmd
cd "C:\Program Files\PostgreSQL\16\bin"
psql -U postgres -d inventario_motos -h localhost -p 5432
```

Si te conecta, ejecuta:
```sql
-- Ver las tablas (al inicio estará vacío)
\dt

-- Probar una consulta simple
SELECT version();
```

---

## 🚀 PASO 6: Ejecutar el Sistema

### Terminal 1 - Backend:
```cmd
cd D:\Almacen\backend
mvn spring-boot:run
```

Espera hasta ver:
```
Started InventarioMotosApplication in X.XX seconds
```

### Terminal 2 - Frontend:
```cmd
cd D:\Almacen\frontend
npm install
npm start
```

---

## ✅ PASO 7: Verificar que todo funciona

### 1. Verificar Backend:
Abre en el navegador: http://localhost:8080/swagger-ui.html
- Si ves la documentación de la API, ¡el backend funciona!

### 2. Verificar Frontend:
Abre en el navegador: http://localhost:3000
- Si ves el Dashboard, ¡el frontend funciona!

### 3. Verificar Base de Datos:
En psql o pgAdmin, ejecuta:
```sql
\c inventario_motos
\dt
```
Deberías ver las tablas creadas:
- categorias
- proveedores
- productos
- ventas
- detalle_ventas
- movimientos_inventario
- ordenes_compra
- detalle_ordenes_compra

---

## 🔥 Comandos Útiles de PostgreSQL

```cmd
# Iniciar servicio PostgreSQL
net start postgresql-x64-16

# Detener servicio PostgreSQL
net stop postgresql-x64-16

# Conectarse a la base de datos
psql -U postgres -d inventario_motos

# Backup de la base de datos
pg_dump -U postgres -d inventario_motos > backup.sql

# Restaurar backup
psql -U postgres -d inventario_motos < backup.sql
```

---

## 🐛 Solución de Problemas Comunes

### Error: "Connection refused"
- Verifica que el servicio PostgreSQL esté corriendo
- Ejecuta: `net start postgresql-x64-16`

### Error: "Password authentication failed"
- Verifica la contraseña en application.properties
- Asegúrate de usar la contraseña correcta

### Error: "Database does not exist"
- Crea la base de datos primero:
```sql
CREATE DATABASE inventario_motos;
```

### El puerto 5432 está ocupado
- Verifica qué lo está usando: `netstat -ano | findstr 5432`
- O cambia el puerto en PostgreSQL y en application.properties

---

## 📊 Ver datos en la base de datos

Después de usar el sistema, puedes ver los datos:

```sql
-- Conectarse
\c inventario_motos

-- Ver categorías
SELECT * FROM categorias;

-- Ver productos
SELECT * FROM productos;

-- Ver ventas
SELECT * FROM ventas;

-- Contar productos
SELECT COUNT(*) FROM productos;
```

