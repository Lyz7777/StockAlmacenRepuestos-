import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor para manejar errores
api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error.response?.data || error.message);
    return Promise.reject(error);
  }
);

// ==================== PRODUCTOS ====================
export const productoService = {
  listar: (page = 0, size = 20, sortBy = 'nombre', sortDirection = 'ASC') =>
    api.get(`/productos?page=${page}&size=${size}&sortBy=${sortBy}&sortDirection=${sortDirection}`),

  obtener: (codigoBarras) => api.get(`/productos/${codigoBarras}`),

  crear: (producto) => api.post('/productos', producto),

  actualizar: (codigoBarras, producto) => api.put(`/productos/${codigoBarras}`, producto),

  eliminar: (codigoBarras) => api.delete(`/productos/${codigoBarras}`),

  busquedaAvanzada: (filtros) => api.post('/productos/busqueda-avanzada', filtros),

  busquedaGeneral: (texto) => api.get(`/productos/buscar?texto=${encodeURIComponent(texto)}`),

  stockBajo: () => api.get('/productos/stock-bajo'),

  agotados: () => api.get('/productos/agotados'),

  registrarEntrada: (codigoBarras, cantidad, motivo) =>
    api.post(`/productos/${codigoBarras}/entrada?cantidad=${cantidad}&motivo=${encodeURIComponent(motivo || '')}`),

  ajustarStock: (codigoBarras, cantidad, motivo, esEntrada) =>
    api.post(`/productos/${codigoBarras}/ajuste?cantidad=${cantidad}&motivo=${encodeURIComponent(motivo)}&esEntrada=${esEntrada}`),

  autocompletarMarcas: (texto) => api.get(`/productos/autocompletar/marcas?texto=${encodeURIComponent(texto)}`),

  autocompletarModelos: (texto) => api.get(`/productos/autocompletar/modelos?texto=${encodeURIComponent(texto)}`),

  generarCodigo: () => api.get('/productos/generar-codigo'),

  obtenerImagenCodigo: (codigoBarras) => api.get(`/productos/${codigoBarras}/imagen-codigo`),

  estadisticas: () => api.get('/productos/estadisticas'),
};

// ==================== CATEGORÍAS ====================
export const categoriaService = {
  listar: () => api.get('/categorias'),
  obtener: (id) => api.get(`/categorias/${id}`),
  crear: (categoria) => api.post('/categorias', categoria),
  actualizar: (id, categoria) => api.put(`/categorias/${id}`, categoria),
  eliminar: (id) => api.delete(`/categorias/${id}`),
  buscar: (nombre) => api.get(`/categorias/buscar?nombre=${encodeURIComponent(nombre)}`),
};

// ==================== PROVEEDORES ====================
export const proveedorService = {
  listar: () => api.get('/proveedores'),
  obtener: (id) => api.get(`/proveedores/${id}`),
  crear: (proveedor) => api.post('/proveedores', proveedor),
  actualizar: (id, proveedor) => api.put(`/proveedores/${id}`, proveedor),
  eliminar: (id) => api.delete(`/proveedores/${id}`),
  buscar: (nombre) => api.get(`/proveedores/buscar?nombre=${encodeURIComponent(nombre)}`),
  autocompletar: (texto) => api.get(`/proveedores/autocompletar?texto=${encodeURIComponent(texto)}`),
};

// ==================== VENTAS ====================
export const ventaService = {
  listar: (page = 0, size = 20) => api.get(`/ventas?page=${page}&size=${size}`),
  obtener: (id) => api.get(`/ventas/${id}`),
  ventasHoy: () => api.get('/ventas/hoy'),
  ventasPorFecha: (fechaInicio, fechaFin) =>
    api.get(`/ventas/por-fecha?fechaInicio=${fechaInicio}&fechaFin=${fechaFin}`),
  crear: (venta) => api.post('/ventas', venta),
  cancelar: (id) => api.post(`/ventas/${id}/cancelar`),
  estadisticas: () => api.get('/ventas/estadisticas'),
  ventasPorProducto: (codigoProducto) => api.get(`/ventas/por-producto/${codigoProducto}`),
  generarTicket: (id) => api.get(`/ventas/${id}/ticket`, { responseType: 'blob' }),
};

// ==================== MOVIMIENTOS ====================
export const movimientoService = {
  listar: (page = 0, size = 20) => api.get(`/movimientos?page=${page}&size=${size}`),
  listarPorProducto: (codigoBarras) => api.get(`/movimientos/producto/${codigoBarras}`),
  listarPorFecha: (fechaInicio, fechaFin) =>
    api.get(`/movimientos/por-fecha?fechaInicio=${fechaInicio}&fechaFin=${fechaFin}`),
  registrarEntrada: (codigoBarras, cantidad, motivo) =>
    api.post(`/movimientos/entrada?codigoBarras=${codigoBarras}&cantidad=${cantidad}&motivo=${encodeURIComponent(motivo)}`),
  registrarAjuste: (codigoBarras, cantidad, motivo, esPositivo) =>
    api.post(`/movimientos/ajuste?codigoBarras=${codigoBarras}&cantidad=${cantidad}&motivo=${encodeURIComponent(motivo)}&esPositivo=${esPositivo}`),
  ultimos: (cantidad = 10) => api.get(`/movimientos/ultimos?cantidad=${cantidad}`),
};

// ==================== ÓRDENES DE COMPRA ====================
export const ordenCompraService = {
  listar: (page = 0, size = 20) => api.get(`/ordenes-compra?page=${page}&size=${size}`),
  obtener: (id) => api.get(`/ordenes-compra/${id}`),
  pendientes: () => api.get('/ordenes-compra/pendientes'),
  porProveedor: (proveedorId) => api.get(`/ordenes-compra/proveedor/${proveedorId}`),
  crear: (orden) => api.post('/ordenes-compra', orden),
  actualizarEstado: (id, estado) => api.patch(`/ordenes-compra/${id}/estado?estado=${estado}`),
  recibir: (id, detalles) => api.post(`/ordenes-compra/${id}/recibir`, detalles),
  cancelar: (id) => api.post(`/ordenes-compra/${id}/cancelar`),
  sugerida: (proveedorId) => api.get(`/ordenes-compra/sugerida/${proveedorId}`),
  contarPendientes: () => api.get('/ordenes-compra/estadisticas/pendientes'),
};

// ==================== REPORTES ====================
export const reporteService = {
  dashboard: () => api.get('/reportes/dashboard'),
  inventario: () => api.get('/reportes/inventario'),
  ventas: (fechaInicio, fechaFin) =>
    api.get(`/reportes/ventas?fechaInicio=${fechaInicio}&fechaFin=${fechaFin}`),
  inventarioPdf: () => api.get('/reportes/inventario/pdf', { responseType: 'blob' }),
  ventasPdf: (fechaInicio, fechaFin) =>
    api.get(`/reportes/ventas/pdf?fechaInicio=${fechaInicio}&fechaFin=${fechaFin}`, { responseType: 'blob' }),
};

export default api;

