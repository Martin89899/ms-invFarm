# ms-invFarm - Microservicio de Inventario para PawPet

Microservicio Spring Boot para la gestión de inventario de productos agrícolas.

## Características

- Gestión completa de productos (CRUD)
- Control de stock con alertas de bajo stock
- Historial de cambios de stock
- Estadísticas de inventario
- Búsqueda y filtrado avanzado
- Gestión por categorías y ubicaciones
- Valoración de inventario

## Tecnologías

- Java 24
- Spring Boot 3.2.0
- Spring Data JPA
- PostgreSQL
- Maven

## Estructura del Proyecto

```
ms-invFarm-spring/
├── src/
│   ├── main/
│   │   ├── java/com/pawpet/msinvfarm/
│   │   │   ├── config/           # Configuraciones globales
│   │   │   ├── controller/       # Controladores REST
│   │   │   ├── model/            # Entidades JPA
│   │   │   ├── repository/       # Repositorios JPA
│   │   │   ├── service/          # Lógica de negocio
│   │   │   └── MsInvFarmApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── pom.xml
└── README.md
```

## Configuración

### Base de Datos

El proyecto utiliza PostgreSQL. Configura la conexión en `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/pawpet_inventory
spring.datasource.username=postgres
spring.datasource.password=postgres
```

O usa variables de entorno:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/pawpet_inventory
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
```

## Ejecución

### Usando Maven Wrapper

```bash
./mvnw spring-boot:run
```

### Usando Maven instalado

```bash
mvn spring-boot:run
```

El servicio estará disponible en `http://localhost:8080`

## API Endpoints

### Productos

- `GET /api/inventory/products` - Listar productos (paginado)
- `GET /api/inventory/products/{id}` - Obtener producto por ID
- `GET /api/inventory/products/sku/{sku}` - Obtener producto por SKU
- `POST /api/inventory/products` - Crear producto
- `PUT /api/inventory/products/{id}` - Actualizar producto
- `DELETE /api/inventory/products/{id}` - Eliminar producto

### Stock

- `PATCH /api/inventory/products/{id}/stock` - Actualizar stock
- `GET /api/inventory/products/low-stock` - Productos con bajo stock
- `GET /api/inventory/products/{id}/history` - Historial de stock

### Estadísticas y Búsqueda

- `GET /api/inventory/stats` - Estadísticas generales
- `GET /api/inventory/search` - Buscar productos
- `GET /api/inventory/categories` - Listar categorías
- `GET /api/inventory/category/{category}` - Productos por categoría
- `GET /api/inventory/location/{location}` - Productos por ubicación
- `GET /api/inventory/valuation` - Valoración de inventario

### Umbrales

- `PATCH /api/inventory/products/{id}/thresholds` - Actualizar umbrales de stock

## Ejemplos de Uso

### Crear Producto

```bash
curl -X POST http://localhost:8080/api/inventory/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "PROD-001",
    "name": "Producto Ejemplo",
    "description": "Descripción del producto",
    "category": "Alimentos",
    "price": 10.50,
    "cost": 5.00,
    "stock": 100,
    "minStock": 10
  }'
```

### Actualizar Stock

```bash
curl -X PATCH "http://localhost:8080/api/inventory/products/1/stock?quantity=10&updateType=VENTA"
```

### Buscar Productos

```bash
curl "http://localhost:8080/api/inventory/search?query=alimento&page=1&limit=10"
```

## Modelo de Datos

### Product

- `id`: Identificador único
- `sku`: Código único del producto
- `name`: Nombre del producto
- `description`: Descripción
- `category`: Categoría
- `price`: Precio de venta
- `cost`: Costo
- `stock`: Cantidad en stock
- `minStock`: Umbral mínimo de stock
- `maxStock`: Umbral máximo de stock
- `location`: Ubicación en almacén
- `supplier`: Proveedor
- `isActive`: Estado activo/inactivo
- `lastStockAlert`: Fecha última alerta de stock

### StockHistory

- `id`: Identificador único
- `productId`: ID del producto
- `oldStock`: Stock anterior
- `newStock`: Stock nuevo
- `change`: Cambio (diferencia)
- `updateType`: Tipo de actualización (VENTA, COMPRA, MANUAL, etc.)
- `timestamp`: Fecha y hora del cambio

## Desarrollo

### Ejecutar Tests

```bash
./mvnw test
```

### Compilar

```bash
./mvnw clean install
```

## Licencia

LICENSE
