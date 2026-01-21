import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Box,
  Paper,
  Typography,
  TextField,
  Button,
  Grid,
  Autocomplete,
  InputAdornment,
  CircularProgress,
  Alert,
  Divider,
} from '@mui/material';
import {
  Save as SaveIcon,
  ArrowBack as BackIcon,
  QrCode as QrCodeIcon,
} from '@mui/icons-material';
import toast from 'react-hot-toast';
import { productoService, categoriaService, proveedorService } from '../services/api';

const ProductoForm = () => {
  const navigate = useNavigate();
  const { codigoBarras } = useParams();
  const isEditing = Boolean(codigoBarras);

  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [categorias, setCategorias] = useState([]);
  const [proveedores, setProveedores] = useState([]);
  const [marcasSugeridas, setMarcasSugeridas] = useState([]);
  const [modelosSugeridos, setModelosSugeridos] = useState([]);
  const [codigoBarrasImagen, setCodigoBarrasImagen] = useState(null);

  const [formData, setFormData] = useState({
    codigoBarras: '',
    codigoInterno: '',
    nombre: '',
    descripcion: '',
    marca: '',
    modeloCompatible: '',
    categoriaId: null,
    precioVenta: '',
    stockActual: 0,
    stockMinimo: 5,
    proveedorId: null,
    ubicacion: '',
    imagenUrl: '',
  });

  const [errors, setErrors] = useState({});

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const [catRes, provRes] = await Promise.all([
          categoriaService.listar(),
          proveedorService.listar(),
        ]);
        setCategorias(catRes.data);
        setProveedores(provRes.data);

        if (isEditing) {
          const prodRes = await productoService.obtener(codigoBarras);
          const producto = prodRes.data;
          setFormData({
            codigoBarras: producto.codigoBarras,
            codigoInterno: producto.codigoInterno || '',
            nombre: producto.nombre,
            descripcion: producto.descripcion || '',
            marca: producto.marca || '',
            modeloCompatible: producto.modeloCompatible || '',
            categoriaId: producto.categoriaId,
            precioVenta: producto.precioVenta,
            stockActual: producto.stockActual,
            stockMinimo: producto.stockMinimo,
            proveedorId: producto.proveedorId,
            ubicacion: producto.ubicacion || '',
            imagenUrl: producto.imagenUrl || '',
          });
        }
      } catch (error) {
        toast.error('Error al cargar datos');
        console.error(error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [codigoBarras, isEditing]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: null }));
    }
  };

  const handleGenerarCodigo = async () => {
    try {
      const response = await productoService.generarCodigo();
      setFormData(prev => ({ ...prev, codigoBarras: response.data.codigoBarras }));
      setCodigoBarrasImagen(response.data.imagenBase64);
      toast.success('Código de barras generado');
    } catch (error) {
      toast.error('Error al generar código');
    }
  };

  const handleBuscarMarcas = async (texto) => {
    if (texto.length >= 2) {
      try {
        const response = await productoService.autocompletarMarcas(texto);
        setMarcasSugeridas(response.data);
      } catch (error) {
        console.error(error);
      }
    }
  };

  const handleBuscarModelos = async (texto) => {
    if (texto.length >= 2) {
      try {
        const response = await productoService.autocompletarModelos(texto);
        setModelosSugeridos(response.data);
      } catch (error) {
        console.error(error);
      }
    }
  };

  const validate = () => {
    const newErrors = {};
    if (!formData.nombre.trim()) newErrors.nombre = 'El nombre es obligatorio';
    if (!formData.precioVenta || formData.precioVenta <= 0) newErrors.precioVenta = 'El precio debe ser mayor a 0';
    if (formData.stockActual < 0) newErrors.stockActual = 'El stock no puede ser negativo';
    if (formData.stockMinimo < 0) newErrors.stockMinimo = 'El stock mínimo no puede ser negativo';

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    try {
      setSaving(true);
      const dataToSend = {
        ...formData,
        precioVenta: parseFloat(formData.precioVenta),
        stockActual: parseInt(formData.stockActual),
        stockMinimo: parseInt(formData.stockMinimo),
      };

      if (isEditing) {
        await productoService.actualizar(codigoBarras, dataToSend);
        toast.success('Producto actualizado correctamente');
      } else {
        await productoService.crear(dataToSend);
        toast.success('Producto creado correctamente');
      }
      navigate('/productos');
    } catch (error) {
      const message = error.response?.data?.message || 'Error al guardar producto';
      toast.error(message);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
        <Button startIcon={<BackIcon />} onClick={() => navigate('/productos')} sx={{ mr: 2 }}>
          Volver
        </Button>
        <Typography variant="h4" sx={{ fontWeight: 'bold' }}>
          {isEditing ? 'Editar Producto' : 'Nuevo Producto'}
        </Typography>
      </Box>

      <Paper sx={{ p: 3 }}>
        <form onSubmit={handleSubmit}>
          <Grid container spacing={3}>
            {/* Códigos */}
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>Identificación</Typography>
              <Divider sx={{ mb: 2 }} />
            </Grid>

            <Grid item xs={12} md={4}>
              <TextField
                fullWidth
                label="Código de Barras"
                name="codigoBarras"
                value={formData.codigoBarras}
                onChange={handleChange}
                disabled={isEditing}
                placeholder="Escanear o generar automáticamente"
                InputProps={{
                  endAdornment: !isEditing && (
                    <InputAdornment position="end">
                      <Button size="small" onClick={handleGenerarCodigo} startIcon={<QrCodeIcon />}>
                        Generar
                      </Button>
                    </InputAdornment>
                  ),
                }}
                helperText={!isEditing && "Deje vacío para generar automáticamente"}
              />
              {codigoBarrasImagen && (
                <Box sx={{ mt: 1, textAlign: 'center' }}>
                  <img src={codigoBarrasImagen} alt="Código de barras" style={{ maxWidth: '100%' }} />
                </Box>
              )}
            </Grid>

            <Grid item xs={12} md={4}>
              <TextField
                fullWidth
                label="Código Interno"
                name="codigoInterno"
                value={formData.codigoInterno}
                onChange={handleChange}
                placeholder="Ej: PRD-FRE-001"
              />
            </Grid>

            <Grid item xs={12} md={4}>
              <TextField
                fullWidth
                label="Ubicación en Almacén"
                name="ubicacion"
                value={formData.ubicacion}
                onChange={handleChange}
                placeholder="Ej: Estante A-1"
              />
            </Grid>

            {/* Información del producto */}
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>Información del Producto</Typography>
              <Divider sx={{ mb: 2 }} />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                required
                label="Nombre del Producto"
                name="nombre"
                value={formData.nombre}
                onChange={handleChange}
                error={Boolean(errors.nombre)}
                helperText={errors.nombre}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <Autocomplete
                freeSolo
                options={marcasSugeridas}
                value={formData.marca}
                onInputChange={(e, value) => {
                  handleChange({ target: { name: 'marca', value } });
                  handleBuscarMarcas(value);
                }}
                renderInput={(params) => (
                  <TextField {...params} label="Marca / Fabricante" />
                )}
              />
            </Grid>

            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={2}
                label="Descripción"
                name="descripcion"
                value={formData.descripcion}
                onChange={handleChange}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <Autocomplete
                freeSolo
                options={modelosSugeridos}
                value={formData.modeloCompatible}
                onInputChange={(e, value) => {
                  handleChange({ target: { name: 'modeloCompatible', value } });
                  handleBuscarModelos(value);
                }}
                renderInput={(params) => (
                  <TextField {...params} label="Modelo de Moto Compatible" placeholder="Ej: Honda CBR 250, Yamaha R15" />
                )}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <Autocomplete
                options={categorias}
                getOptionLabel={(option) => option.nombre || ''}
                value={categorias.find(c => c.id === formData.categoriaId) || null}
                onChange={(e, value) => setFormData(prev => ({ ...prev, categoriaId: value?.id || null }))}
                renderInput={(params) => (
                  <TextField {...params} label="Categoría" />
                )}
              />
            </Grid>

            {/* Stock y Precio */}
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>Stock y Precio</Typography>
              <Divider sx={{ mb: 2 }} />
            </Grid>

            <Grid item xs={12} md={3}>
              <TextField
                fullWidth
                required
                type="number"
                label="Precio de Venta"
                name="precioVenta"
                value={formData.precioVenta}
                onChange={handleChange}
                error={Boolean(errors.precioVenta)}
                helperText={errors.precioVenta}
                InputProps={{
                  startAdornment: <InputAdornment position="start">$</InputAdornment>,
                }}
              />
            </Grid>

            <Grid item xs={12} md={3}>
              <TextField
                fullWidth
                type="number"
                label="Stock Actual"
                name="stockActual"
                value={formData.stockActual}
                onChange={handleChange}
                error={Boolean(errors.stockActual)}
                helperText={errors.stockActual}
                disabled={isEditing}
              />
            </Grid>

            <Grid item xs={12} md={3}>
              <TextField
                fullWidth
                type="number"
                label="Stock Mínimo"
                name="stockMinimo"
                value={formData.stockMinimo}
                onChange={handleChange}
                error={Boolean(errors.stockMinimo)}
                helperText={errors.stockMinimo || "Alerta cuando llegue a este nivel"}
              />
            </Grid>

            <Grid item xs={12} md={3}>
              <Autocomplete
                options={proveedores}
                getOptionLabel={(option) => option.nombre || ''}
                value={proveedores.find(p => p.id === formData.proveedorId) || null}
                onChange={(e, value) => setFormData(prev => ({ ...prev, proveedorId: value?.id || null }))}
                renderInput={(params) => (
                  <TextField {...params} label="Proveedor" />
                )}
              />
            </Grid>

            {/* Botones */}
            <Grid item xs={12}>
              <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end', mt: 2 }}>
                <Button variant="outlined" onClick={() => navigate('/productos')}>
                  Cancelar
                </Button>
                <Button
                  type="submit"
                  variant="contained"
                  startIcon={saving ? <CircularProgress size={20} /> : <SaveIcon />}
                  disabled={saving}
                >
                  {saving ? 'Guardando...' : 'Guardar Producto'}
                </Button>
              </Box>
            </Grid>
          </Grid>
        </form>
      </Paper>
    </Box>
  );
};

export default ProductoForm;
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Grid,
  Paper,
  Typography,
  Box,
  Card,
  CardContent,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Chip,
  CircularProgress,
  Alert,
} from '@mui/material';
import {
  Inventory as InventoryIcon,
  Warning as WarningIcon,
  ShoppingCart as ShoppingCartIcon,
  AttachMoney as MoneyIcon,
  TrendingUp as TrendingUpIcon,
  Error as ErrorIcon,
} from '@mui/icons-material';
import { Chart as ChartJS, ArcElement, Tooltip, Legend, CategoryScale, LinearScale, BarElement } from 'chart.js';
import { Doughnut, Bar } from 'react-chartjs-2';
import { reporteService } from '../services/api';
import { useApp } from '../context/AppContext';

ChartJS.register(ArcElement, Tooltip, Legend, CategoryScale, LinearScale, BarElement);

const StatCard = ({ title, value, icon, color, subtitle, onClick }) => (
  <Card
    sx={{
      height: '100%',
      cursor: onClick ? 'pointer' : 'default',
      '&:hover': onClick ? { boxShadow: 6 } : {}
    }}
    onClick={onClick}
  >
    <CardContent>
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Box>
          <Typography color="textSecondary" gutterBottom variant="body2">
            {title}
          </Typography>
          <Typography variant="h4" component="div" sx={{ fontWeight: 'bold', color }}>
            {value}
          </Typography>
          {subtitle && (
            <Typography variant="body2" color="textSecondary">
              {subtitle}
            </Typography>
          )}
        </Box>
        <Box sx={{
          backgroundColor: `${color}20`,
          borderRadius: '50%',
          p: 1.5,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center'
        }}>
          {React.cloneElement(icon, { sx: { fontSize: 40, color } })}
        </Box>
      </Box>
    </CardContent>
  </Card>
);

const Dashboard = () => {
  const navigate = useNavigate();
  const { cargarDashboard } = useApp();
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchDashboard = async () => {
      try {
        setLoading(true);
        const response = await reporteService.dashboard();
        setDashboard(response.data);
        cargarDashboard();
      } catch (err) {
        setError('Error al cargar el dashboard: ' + (err.response?.data?.message || err.message));
      } finally {
        setLoading(false);
      }
    };

    fetchDashboard();
  }, [cargarDashboard]);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '50vh' }}>
        <CircularProgress size={60} />
      </Box>
    );
  }

  if (error) {
    return <Alert severity="error">{error}</Alert>;
  }

  const stockChartData = {
    labels: ['Con Stock', 'Stock Bajo', 'Agotados'],
    datasets: [{
      data: [
        dashboard?.totalProductos - dashboard?.productosStockBajo - dashboard?.productosAgotados || 0,
        dashboard?.productosStockBajo || 0,
        dashboard?.productosAgotados || 0,
      ],
      backgroundColor: ['#4caf50', '#ff9800', '#f44336'],
      borderWidth: 0,
    }],
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold', mb: 3 }}>
        Dashboard
      </Typography>

      {/* Alertas de stock */}
      {(dashboard?.productosAgotados > 0 || dashboard?.productosStockBajo > 0) && (
        <Alert
          severity="warning"
          sx={{ mb: 3 }}
          action={
            <Chip
              label="Ver productos"
              size="small"
              onClick={() => navigate('/productos/busqueda')}
              sx={{ cursor: 'pointer' }}
            />
          }
        >
          {dashboard?.productosAgotados > 0 && `${dashboard.productosAgotados} producto(s) agotado(s). `}
          {dashboard?.productosStockBajo > 0 && `${dashboard.productosStockBajo} producto(s) con stock bajo.`}
        </Alert>
      )}

      {/* Tarjetas de estadísticas */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Productos"
            value={dashboard?.totalProductos || 0}
            icon={<InventoryIcon />}
            color="#1976d2"
            onClick={() => navigate('/productos')}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Ventas Hoy"
            value={dashboard?.ventasHoy || 0}
            icon={<ShoppingCartIcon />}
            color="#4caf50"
            subtitle={`$${(dashboard?.totalVentasHoy || 0).toFixed(2)}`}
            onClick={() => navigate('/ventas')}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Stock Bajo"
            value={dashboard?.productosStockBajo || 0}
            icon={<WarningIcon />}
            color="#ff9800"
            onClick={() => navigate('/productos/busqueda')}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Valor Inventario"
            value={`$${(dashboard?.valorInventario || 0).toFixed(2)}`}
            icon={<MoneyIcon />}
            color="#9c27b0"
          />
        </Grid>
      </Grid>

      {/* Segunda fila de estadísticas */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={4}>
          <StatCard
            title="Ventas Semana"
            value={`$${(dashboard?.totalVentasSemana || 0).toFixed(2)}`}
            icon={<TrendingUpIcon />}
            color="#00bcd4"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={4}>
          <StatCard
            title="Ventas Mes"
            value={`$${(dashboard?.totalVentasMes || 0).toFixed(2)}`}
            icon={<TrendingUpIcon />}
            color="#3f51b5"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={4}>
          <StatCard
            title="Agotados"
            value={dashboard?.productosAgotados || 0}
            icon={<ErrorIcon />}
            color="#f44336"
            onClick={() => navigate('/productos/busqueda')}
          />
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        {/* Gráfico de estado de stock */}
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3, height: '100%' }}>
            <Typography variant="h6" gutterBottom>
              Estado del Inventario
            </Typography>
            <Box sx={{ height: 250, display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
              <Doughnut
                data={stockChartData}
                options={{
                  responsive: true,
                  maintainAspectRatio: false,
                  plugins: {
                    legend: {
                      position: 'bottom',
                    },
                  },
                }}
              />
            </Box>
          </Paper>
        </Grid>

        {/* Productos más vendidos */}
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3, height: '100%' }}>
            <Typography variant="h6" gutterBottom>
              Productos Más Vendidos
            </Typography>
            <List dense>
              {dashboard?.productosMasVendidos?.length > 0 ? (
                dashboard.productosMasVendidos.map((producto, index) => (
                  <ListItem key={producto.codigoBarras}>
                    <ListItemIcon>
                      <Chip
                        label={index + 1}
                        size="small"
                        color={index === 0 ? 'primary' : 'default'}
                      />
                    </ListItemIcon>
                    <ListItemText
                      primary={producto.nombre}
                      secondary={`${producto.cantidadVendida} vendidos`}
                    />
                  </ListItem>
                ))
              ) : (
                <ListItem>
                  <ListItemText primary="Sin datos de ventas" />
                </ListItem>
              )}
            </List>
          </Paper>
        </Grid>

        {/* Productos con stock crítico */}
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3, height: '100%' }}>
            <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <WarningIcon color="warning" />
              Stock Crítico
            </Typography>
            <List dense>
              {dashboard?.productosStockCritico?.length > 0 ? (
                dashboard.productosStockCritico.map((producto) => (
                  <ListItem
                    key={producto.codigoBarras}
                    sx={{ cursor: 'pointer' }}
                    onClick={() => navigate(`/productos/editar/${producto.codigoBarras}`)}
                  >
                    <ListItemText
                      primary={producto.nombre}
                      secondary={`Stock: ${producto.stockActual} / Mínimo: ${producto.stockMinimo}`}
                    />
                    <Chip
                      label={producto.stockActual === 0 ? 'AGOTADO' : 'BAJO'}
                      size="small"
                      color={producto.stockActual === 0 ? 'error' : 'warning'}
                    />
                  </ListItem>
                ))
              ) : (
                <ListItem>
                  <ListItemText
                    primary="¡Excelente!"
                    secondary="No hay productos con stock crítico"
                  />
                </ListItem>
              )}
            </List>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard;

