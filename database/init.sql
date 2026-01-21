-- =============================================
-- SCRIPT SQL COMPLETO PARA POSTGRESQL
-- Sistema de Inventario - Repuestos de Motos
-- =============================================

-- Crear extensión para búsquedas de texto (opcional pero recomendado)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- =============================================
-- TABLA: categorias
-- =============================================
CREATE TABLE IF NOT EXISTS categorias (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(500),
    activo BOOLEAN NOT NULL DEFAULT true
);

-- =============================================
-- TABLA: proveedores
-- =============================================
CREATE TABLE IF NOT EXISTS proveedores (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    ruc VARCHAR(20),
    direccion VARCHAR(500),
    telefono VARCHAR(20),
    email VARCHAR(100),
    contacto_principal VARCHAR(100),
    productos_suministra VARCHAR(1000),
    activo BOOLEAN NOT NULL DEFAULT true
);

-- =============================================
-- TABLA: productos
-- =============================================
CREATE TABLE IF NOT EXISTS productos (
    codigo_barras VARCHAR(50) PRIMARY KEY,
    codigo_interno VARCHAR(50),
    nombre VARCHAR(200) NOT NULL,
    descripcion VARCHAR(1000),
    marca VARCHAR(100),
    modelo_compatible VARCHAR(200),
    categoria_id BIGINT REFERENCES categorias(id),
    precio_venta DECIMAL(12,2) NOT NULL,
    stock_actual INTEGER NOT NULL DEFAULT 0,
    stock_minimo INTEGER NOT NULL DEFAULT 5,
    proveedor_id BIGINT REFERENCES proveedores(id),
    fecha_ingreso DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_ultima_venta TIMESTAMP,
    ubicacion VARCHAR(100),
    imagen_url VARCHAR(500),
    activo BOOLEAN NOT NULL DEFAULT true,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP
);

-- Índices para búsquedas rápidas
CREATE INDEX IF NOT EXISTS idx_producto_nombre ON productos(nombre);
CREATE INDEX IF NOT EXISTS idx_producto_codigo_interno ON productos(codigo_interno);
CREATE INDEX IF NOT EXISTS idx_producto_marca ON productos(marca);
CREATE INDEX IF NOT EXISTS idx_producto_modelo ON productos(modelo_compatible);
CREATE INDEX IF NOT EXISTS idx_producto_stock ON productos(stock_actual);
CREATE INDEX IF NOT EXISTS idx_producto_categoria ON productos(categoria_id);
CREATE INDEX IF NOT EXISTS idx_producto_proveedor ON productos(proveedor_id);

-- =============================================
-- TABLA: ventas
-- =============================================
CREATE TABLE IF NOT EXISTS ventas (
    id BIGSERIAL PRIMARY KEY,
    fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(12,2) NOT NULL DEFAULT 0,
    estado VARCHAR(20) NOT NULL DEFAULT 'COMPLETADA',
    observaciones VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS idx_venta_fecha ON ventas(fecha_hora);
CREATE INDEX IF NOT EXISTS idx_venta_estado ON ventas(estado);

-- =============================================
-- TABLA: detalle_ventas
-- =============================================
CREATE TABLE IF NOT EXISTS detalle_ventas (
    id BIGSERIAL PRIMARY KEY,
    venta_id BIGINT NOT NULL REFERENCES ventas(id) ON DELETE CASCADE,
    producto_codigo VARCHAR(50) NOT NULL REFERENCES productos(codigo_barras),
    cantidad INTEGER NOT NULL,
    precio_unitario DECIMAL(12,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_detalle_venta ON detalle_ventas(venta_id);
CREATE INDEX IF NOT EXISTS idx_detalle_producto ON detalle_ventas(producto_codigo);

-- =============================================
-- TABLA: movimientos_inventario
-- =============================================
CREATE TABLE IF NOT EXISTS movimientos_inventario (
    id BIGSERIAL PRIMARY KEY,
    producto_codigo VARCHAR(50) NOT NULL REFERENCES productos(codigo_barras),
    tipo_movimiento VARCHAR(20) NOT NULL,
    cantidad INTEGER NOT NULL,
    fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motivo VARCHAR(500),
    stock_anterior INTEGER,
    stock_nuevo INTEGER,
    referencia VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_movimiento_fecha ON movimientos_inventario(fecha_hora);
CREATE INDEX IF NOT EXISTS idx_movimiento_tipo ON movimientos_inventario(tipo_movimiento);
CREATE INDEX IF NOT EXISTS idx_movimiento_producto ON movimientos_inventario(producto_codigo);

-- =============================================
-- TABLA: ordenes_compra
-- =============================================
CREATE TABLE IF NOT EXISTS ordenes_compra (
    id BIGSERIAL PRIMARY KEY,
    proveedor_id BIGINT NOT NULL REFERENCES proveedores(id),
    fecha_orden TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_entrega_estimada DATE,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    total DECIMAL(12,2) DEFAULT 0,
    observaciones VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS idx_orden_fecha ON ordenes_compra(fecha_orden);
CREATE INDEX IF NOT EXISTS idx_orden_estado ON ordenes_compra(estado);
CREATE INDEX IF NOT EXISTS idx_orden_proveedor ON ordenes_compra(proveedor_id);

-- =============================================
-- TABLA: detalle_ordenes_compra
-- =============================================
CREATE TABLE IF NOT EXISTS detalle_ordenes_compra (
    id BIGSERIAL PRIMARY KEY,
    orden_id BIGINT NOT NULL REFERENCES ordenes_compra(id) ON DELETE CASCADE,
    producto_codigo VARCHAR(50) NOT NULL REFERENCES productos(codigo_barras),
    cantidad_solicitada INTEGER NOT NULL,
    cantidad_recibida INTEGER DEFAULT 0,
    precio_compra DECIMAL(12,2) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_detalle_orden ON detalle_ordenes_compra(orden_id);

-- =============================================
-- DATOS DE EJEMPLO
-- =============================================

-- Categorías
INSERT INTO categorias (nombre, descripcion) VALUES
('Frenos', 'Pastillas, discos, zapatas, líquido de frenos y componentes del sistema de frenado'),
('Motor', 'Pistones, anillos, válvulas, juntas, empaques y componentes internos del motor'),
('Suspensión', 'Amortiguadores, resortes, bujes, rodamientos y componentes de suspensión'),
('Sistema Eléctrico', 'Baterías, cables, bujías, bobinas, CDI, reguladores y luces'),
('Transmisión', 'Cadenas, piñones, coronas, cables de embrague y acelerador'),
('Carrocería', 'Guardabarros, tanques, tapas laterales, espejos, manubrios'),
('Filtros y Lubricantes', 'Filtros de aire, aceite, combustible y aceites lubricantes'),
('Neumáticos', 'Llantas, cámaras, válvulas y accesorios de ruedas'),
('Accesorios', 'Cascos, guantes, protecciones, maletas, alarmas y accesorios varios'),
('Escape', 'Silenciadores, tubos de escape, catalizadores y abrazaderas')
ON CONFLICT (nombre) DO NOTHING;

-- Proveedores
INSERT INTO proveedores (nombre, ruc, direccion, telefono, email, contacto_principal, productos_suministra) VALUES
('Distribuidora Moto Parts S.A.', '20123456789', 'Av. Industrial 456', '555-1234', 'ventas@motoparts.com', 'Juan Pérez', 'Frenos, Suspensión, Motor'),
('Importaciones Racing', '20987654321', 'Jr. Comercio 789', '555-5678', 'contacto@racing.com', 'María García', 'Transmisión, Sistema Eléctrico'),
('Lubricantes del Perú', '20456789123', 'Av. Grau 123', '555-9012', 'pedidos@lubriperu.com', 'Carlos López', 'Filtros y Lubricantes'),
('Accesorios Moto World', '20789123456', 'Calle Lima 321', '555-3456', 'info@motoworld.com', 'Ana Rodríguez', 'Accesorios, Cascos, Protecciones'),
('Repuestos Honda Perú', '20321654987', 'Av. Javier Prado 500', '555-7890', 'ventas@hondaperu.com', 'Roberto Silva', 'Repuestos originales Honda')
ON CONFLICT DO NOTHING;

-- Productos de ejemplo
INSERT INTO productos (codigo_barras, codigo_interno, nombre, descripcion, marca, modelo_compatible, categoria_id, precio_venta, stock_actual, stock_minimo, proveedor_id, ubicacion) VALUES
('7991234567890', 'PRD-FRE-001', 'Pastillas de Freno Delanteras', 'Pastillas de freno de alto rendimiento', 'Brembo', 'Honda CBR 250, Yamaha R15', 1, 45.00, 25, 10, 1, 'Estante A-1'),
('7991234567891', 'PRD-FRE-002', 'Disco de Freno Delantero', 'Disco de freno ventilado 260mm', 'EBC', 'Honda CB 190R, Suzuki Gixxer', 1, 120.00, 8, 5, 1, 'Estante A-1'),
('7991234567892', 'PRD-FRE-003', 'Líquido de Frenos DOT4', 'Líquido de frenos de alta performance 500ml', 'Castrol', 'Universal', 1, 25.00, 30, 15, 3, 'Estante A-2'),
('7991234567893', 'PRD-MOT-001', 'Kit de Pistón 150cc', 'Kit completo de pistón con anillos y pasador', 'Wiseco', 'Honda CG 150, Titan 150', 2, 85.00, 12, 5, 1, 'Estante B-1'),
('7991234567894', 'PRD-MOT-002', 'Juego de Juntas de Motor', 'Kit completo de empaques para motor', 'Vesrah', 'Yamaha YBR 125', 2, 35.00, 18, 8, 1, 'Estante B-1'),
('7991234567895', 'PRD-ELE-001', 'Batería 12V 7Ah', 'Batería sellada libre de mantenimiento', 'Yuasa', 'Universal', 4, 95.00, 10, 5, 2, 'Estante D-1'),
('7991234567896', 'PRD-ELE-002', 'Bujía NGK Iridium', 'Bujía de alto rendimiento', 'NGK', 'Universal', 4, 18.00, 50, 20, 2, 'Estante D-2'),
('7991234567897', 'PRD-TRA-001', 'Kit de Arrastre Completo', 'Cadena + Piñón + Corona', 'DID', 'Honda CB 190R', 5, 150.00, 7, 4, 2, 'Estante E-1'),
('7991234567898', 'PRD-FIL-001', 'Filtro de Aire', 'Filtro de aire de alto flujo', 'K&N', 'Honda CBR 250', 7, 55.00, 10, 5, 3, 'Estante F-1'),
('7991234567899', 'PRD-FIL-002', 'Aceite Motor 10W40 1L', 'Aceite sintético para motos 4T', 'Motul', 'Universal', 7, 42.00, 40, 20, 3, 'Estante F-2'),
('7991234567900', 'PRD-ACC-001', 'Casco Integral', 'Casco integral con doble visor certificado DOT', 'LS2', 'Talla M', 9, 250.00, 6, 3, 4, 'Estante G-1'),
('7991234567901', 'PRD-ACC-002', 'Guantes de Cuero', 'Guantes de cuero con protecciones', 'Alpinestars', 'Talla L', 9, 85.00, 8, 4, 4, 'Estante G-2'),
-- Productos con stock bajo
('7991234567902', 'PRD-FRE-004', 'Zapatas de Freno Traseras', 'Zapatas de freno de tambor', 'EBC', 'Honda Wave, Biz', 1, 18.00, 3, 10, 1, 'Estante A-2'),
('7991234567903', 'PRD-MOT-003', 'Empaque de Culata', 'Empaque de cabeza de cilindro', 'Vesrah', 'Suzuki GN 125', 2, 15.00, 2, 5, 1, 'Estante B-2'),
-- Productos agotados
('7991234567904', 'PRD-SUS-001', 'Aceite de Horquilla', 'Aceite para suspensión delantera 1L', 'Motul', 'Universal', 3, 38.00, 0, 5, 3, 'Estante C-2'),
('7991234567905', 'PRD-ELE-003', 'Regulador de Voltaje', 'Regulador rectificador', 'Rick', 'Yamaha YBR 125', 4, 55.00, 0, 3, 2, 'Estante D-3')
ON CONFLICT (codigo_barras) DO NOTHING;

-- =============================================
-- FIN DEL SCRIPT
-- =============================================
# Sistema de Gestión de Inventario - Repuestos de Motocicletas

Sistema completo para la gestión de inventario de un almacén de repuestos de motocicletas. Desarrollado con **Spring Boot** (Backend) y **React** (Frontend).

## 🚀 Características Principales

- ✅ **Gestión de Productos** con código de barras (generación automática)
- ✅ **Sistema de Ventas** con lector de código de barras
- ✅ **Control de Inventario** con alertas de stock bajo
- ✅ **Búsqueda Avanzada** con múltiples filtros
- ✅ **Gestión de Proveedores** y Categorías
- ✅ **Órdenes de Compra** con sugerencias automáticas
- ✅ **Reportes en PDF** (Inventario y Ventas)
- ✅ **Dashboard** con estadísticas en tiempo real
- ✅ **Autocompletado Inteligente** en formularios

## 📋 Requisitos Previos

- **Java 17** o superior
- **Node.js 18** o superior
- **PostgreSQL 14** o superior
- **Maven 3.8** o superior

## 🗄️ Configuración de Base de Datos

### 1. Crear la Base de Datos

Abrir PostgreSQL y ejecutar:

```sql
CREATE DATABASE inventario_motos;
```

### 2. Configurar Credenciales

Editar el archivo `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/inventario_motos
spring.datasource.username=postgres
spring.datasource.password=TU_CONTRASEÑA
```

## 🔧 Instalación y Ejecución

### Backend (Spring Boot)

```bash
# Navegar al directorio del backend
cd backend

# Compilar el proyecto
mvn clean install

# Ejecutar la aplicación
mvn spring-boot:run
```

El backend estará disponible en: `http://localhost:8080`

### Frontend (React)

```bash
# Navegar al directorio del frontend
cd frontend

# Instalar dependencias
npm install

# Ejecutar en modo desarrollo
npm start
```

El frontend estará disponible en: `http://localhost:3000`

## 📁 Estructura del Proyecto

```
Almacen/
├── backend/
│   ├── src/main/java/com/inventario/
│   │   ├── config/          # Configuraciones (CORS, Swagger)
│   │   ├── controller/      # Controladores REST
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entity/          # Entidades JPA
│   │   ├── exception/       # Manejo de excepciones
│   │   ├── repository/      # Repositorios Spring Data
│   │   ├── service/         # Lógica de negocio
│   │   └── util/            # Utilidades (PDF, Códigos de barras)
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── data.sql         # Datos de ejemplo
│   └── pom.xml
│
├── frontend/
│   ├── public/
│   ├── src/
│   │   ├── components/      # Componentes reutilizables
│   │   ├── context/         # Context API
│   │   ├── pages/           # Páginas/Vistas
│   │   ├── services/        # Servicios API
│   │   ├── App.js
│   │   └── index.js
│   └── package.json
│
└── README.md
```

## 🔌 API Endpoints

### Productos
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/productos` | Listar productos (paginado) |
| GET | `/api/productos/{codigo}` | Obtener producto por código |
| POST | `/api/productos` | Crear producto |
| PUT | `/api/productos/{codigo}` | Actualizar producto |
| DELETE | `/api/productos/{codigo}` | Eliminar producto |
| POST | `/api/productos/busqueda-avanzada` | Búsqueda con filtros |
| GET | `/api/productos/stock-bajo` | Productos con stock bajo |
| GET | `/api/productos/generar-codigo` | Generar código de barras |

### Ventas
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/ventas` | Listar ventas |
| GET | `/api/ventas/{id}` | Obtener venta |
| POST | `/api/ventas` | Crear venta |
| POST | `/api/ventas/{id}/cancelar` | Cancelar venta |
| GET | `/api/ventas/{id}/ticket` | Generar ticket PDF |

### Categorías
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/categorias` | Listar categorías |
| POST | `/api/categorias` | Crear categoría |
| PUT | `/api/categorias/{id}` | Actualizar categoría |
| DELETE | `/api/categorias/{id}` | Eliminar categoría |

### Proveedores
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/proveedores` | Listar proveedores |
| POST | `/api/proveedores` | Crear proveedor |
| PUT | `/api/proveedores/{id}` | Actualizar proveedor |
| GET | `/api/proveedores/autocompletar` | Autocompletado |

### Reportes
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/reportes/dashboard` | Datos del dashboard |
| GET | `/api/reportes/inventario` | Reporte de inventario |
| GET | `/api/reportes/ventas` | Reporte de ventas |
| GET | `/api/reportes/inventario/pdf` | PDF de inventario |
| GET | `/api/reportes/ventas/pdf` | PDF de ventas |

## 📖 Documentación API (Swagger)

Una vez ejecutado el backend, acceder a:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/api-docs`

## 🔍 Uso del Lector de Código de Barras

El sistema es compatible con lectores de código de barras USB estándar que funcionan como teclado (HID):

1. Conectar el lector USB al computador
2. En la pantalla de **Nueva Venta**, el cursor estará automáticamente en el campo de búsqueda
3. Escanear el código de barras del producto
4. El producto se agregará automáticamente al carrito

**Configuración del Lector:**
- Configurar el lector para agregar ENTER al final del código
- La mayoría de lectores vienen con esta configuración por defecto

## 💡 Funcionalidades Destacadas

### Búsqueda Avanzada
- Búsqueda por nombre, código, marca, modelo
- Filtros por categoría y proveedor
- Filtros por rango de stock
- Filtros para productos agotados o con stock bajo
- Ordenamiento flexible

### Alertas de Stock
- Notificación visual en el dashboard
- Badge en el menú lateral
- Productos destacados en rojo (agotados) o amarillo (stock bajo)

### Generación de Códigos de Barras
- Formato EAN-13 para nuevos productos
- Generación automática si no se proporciona
- Visualización de imagen del código

### Órdenes de Compra Sugeridas
- Basadas en productos con stock bajo
- Agrupadas por proveedor
- Cálculo automático de cantidades sugeridas

## 🛠️ Tecnologías Utilizadas

### Backend
- Java 17
- Spring Boot 3.2
- Spring Data JPA
- PostgreSQL
- Swagger/OpenAPI
- ZXing (Códigos de barras)
- iText 7 (PDFs)
- Lombok
- MapStruct

### Frontend
- React 18
- Material-UI (MUI) 5
- React Router 6
- Axios
- Chart.js
- Day.js
- React Hot Toast

## 📝 Notas Importantes

1. **Sin Autenticación**: El sistema está diseñado para uso personal, no requiere login.

2. **Modo Offline**: Funciona completamente local sin necesidad de internet.

3. **Backup**: Se recomienda hacer respaldos periódicos de la base de datos PostgreSQL.

4. **Primera Ejecución**: El sistema cargará automáticamente datos de ejemplo (categorías, proveedores y productos).

## 🐛 Solución de Problemas

### El backend no inicia
- Verificar que PostgreSQL esté ejecutándose
- Verificar credenciales en `application.properties`
- Verificar que el puerto 8080 esté disponible

### El frontend no conecta con el backend
- Verificar que el backend esté ejecutándose en el puerto 8080
- Verificar configuración de CORS en el backend

### Error al generar PDFs
- Verificar que las dependencias de iText estén correctamente instaladas
- Revisar logs del backend para más detalles

## 📄 Licencia

Este proyecto es de uso privado.

---

**Desarrollado para la gestión eficiente de inventario de repuestos de motocicletas** 🏍️

