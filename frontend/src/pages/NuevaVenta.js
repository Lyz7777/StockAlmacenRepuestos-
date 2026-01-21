import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Paper,
  Typography,
  TextField,
  Button,
  Grid,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  IconButton,
  InputAdornment,
  Divider,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
  Chip,
  Autocomplete,
  CircularProgress,
} from '@mui/material';
import {
  Add as AddIcon,
  Remove as RemoveIcon,
  Delete as DeleteIcon,
  Search as SearchIcon,
  ShoppingCart as CartIcon,
  Print as PrintIcon,
  Clear as ClearIcon,
} from '@mui/icons-material';
import toast from 'react-hot-toast';
import { productoService, ventaService } from '../services/api';

const NuevaVenta = () => {
  const navigate = useNavigate();
  const inputRef = useRef(null);
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [carrito, setCarrito] = useState([]);
  const [observaciones, setObservaciones] = useState('');
  const [confirmDialog, setConfirmDialog] = useState(false);
  const [ventaCompletada, setVentaCompletada] = useState(null);

  // Enfocar el input al cargar
  useEffect(() => {
    inputRef.current?.focus();
  }, []);

  // Calcular totales
  const total = carrito.reduce((sum, item) => sum + (item.precioVenta * item.cantidad), 0);
  const cantidadItems = carrito.reduce((sum, item) => sum + item.cantidad, 0);

  // Buscar productos
  const handleSearch = async (texto) => {
    setSearchText(texto);
    if (texto.length >= 2) {
      try {
        const response = await productoService.busquedaGeneral(texto);
        setSearchResults(response.data.filter(p => p.stockActual > 0));
      } catch (error) {
        console.error(error);
      }
    } else {
      setSearchResults([]);
    }
  };

  // Agregar producto al carrito
  const agregarProducto = (producto) => {
    const existente = carrito.find(item => item.codigoBarras === producto.codigoBarras);

    if (existente) {
      if (existente.cantidad >= producto.stockActual) {
        toast.error(`Stock insuficiente. Disponible: ${producto.stockActual}`);
        return;
      }
      setCarrito(carrito.map(item =>
        item.codigoBarras === producto.codigoBarras
          ? { ...item, cantidad: item.cantidad + 1 }
          : item
      ));
    } else {
      setCarrito([...carrito, { ...producto, cantidad: 1 }]);
    }

    setSearchText('');
    setSearchResults([]);
    inputRef.current?.focus();
    toast.success(`${producto.nombre} agregado`);
  };

  // Buscar por código de barras (escáner)
  const handleCodigoBarras = async (e) => {
    if (e.key === 'Enter' && searchText.trim()) {
      try {
        // Primero intentar búsqueda exacta por código de barras
        const response = await productoService.obtener(searchText.trim());
        if (response.data) {
          agregarProducto(response.data);
        }
      } catch (error) {
        // Si no encuentra, buscar por texto
        if (searchResults.length === 1) {
          agregarProducto(searchResults[0]);
        } else if (searchResults.length > 1) {
          toast('Seleccione un producto de la lista', { icon: '👆' });
        } else {
          toast.error('Producto no encontrado');
        }
      }
    }
  };

  // Modificar cantidad
  const modificarCantidad = (codigoBarras, delta) => {
    setCarrito(carrito.map(item => {
      if (item.codigoBarras === codigoBarras) {
        const nuevaCantidad = item.cantidad + delta;
        if (nuevaCantidad <= 0) return item;
        if (nuevaCantidad > item.stockActual) {
          toast.error(`Stock máximo disponible: ${item.stockActual}`);
          return item;
        }
        return { ...item, cantidad: nuevaCantidad };
      }
      return item;
    }));
  };

  // Eliminar del carrito
  const eliminarDelCarrito = (codigoBarras) => {
    setCarrito(carrito.filter(item => item.codigoBarras !== codigoBarras));
  };

  // Limpiar carrito
  const limpiarCarrito = () => {
    setCarrito([]);
    setObservaciones('');
  };

  // Confirmar venta
  const confirmarVenta = async () => {
    if (carrito.length === 0) {
      toast.error('El carrito está vacío');
      return;
    }

    try {
      setLoading(true);
      const ventaData = {
        observaciones,
        detalles: carrito.map(item => ({
          productoCodigoBarras: item.codigoBarras,
          cantidad: item.cantidad,
        })),
      };

      const response = await ventaService.crear(ventaData);
      setVentaCompletada(response.data);
      setConfirmDialog(false);
      toast.success('¡Venta registrada exitosamente!');
      limpiarCarrito();
    } catch (error) {
      const message = error.response?.data?.message || 'Error al procesar la venta';
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  // Imprimir ticket
  const imprimirTicket = async () => {
    if (ventaCompletada) {
      try {
        const response = await ventaService.generarTicket(ventaCompletada.id);
        const blob = new Blob([response.data], { type: 'application/pdf' });
        const url = window.URL.createObjectURL(blob);
        window.open(url, '_blank');
      } catch (error) {
        toast.error('Error al generar ticket');
      }
    }
  };

  return (
    <Box>
      <Typography variant="h4" sx={{ fontWeight: 'bold', mb: 3 }}>
        Nueva Venta
      </Typography>

      <Grid container spacing={3}>
        {/* Panel de búsqueda */}
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 2, mb: 2 }}>
            <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <SearchIcon /> Buscar Producto
            </Typography>
            <Autocomplete
              freeSolo
              options={searchResults}
              getOptionLabel={(option) =>
                typeof option === 'string' ? option : `${option.codigoBarras} - ${option.nombre}`
              }
              inputValue={searchText}
              onInputChange={(e, value) => handleSearch(value)}
              onChange={(e, value) => {
                if (value && typeof value !== 'string') {
                  agregarProducto(value);
                }
              }}
              renderOption={(props, option) => (
                <li {...props} key={option.codigoBarras}>
                  <Box sx={{ width: '100%' }}>
                    <Typography variant="body1">{option.nombre}</Typography>
                    <Typography variant="body2" color="textSecondary">
                      Código: {option.codigoBarras} | Stock: {option.stockActual} | ${option.precioVenta?.toFixed(2)}
                    </Typography>
                  </Box>
                </li>
              )}
              renderInput={(params) => (
                <TextField
                  {...params}
                  inputRef={inputRef}
                  placeholder="Escanear código de barras o buscar por nombre..."
                  onKeyDown={handleCodigoBarras}
                  InputProps={{
                    ...params.InputProps,
                    startAdornment: (
                      <InputAdornment position="start">
                        <SearchIcon />
                      </InputAdornment>
                    ),
                  }}
                />
              )}
            />
            <Typography variant="caption" color="textSecondary" sx={{ mt: 1, display: 'block' }}>
              💡 Escanee el código de barras o escriba para buscar. Presione Enter para agregar.
            </Typography>
          </Paper>

          {/* Tabla del carrito */}
          <Paper sx={{ p: 2 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <CartIcon /> Carrito de Venta
                {cantidadItems > 0 && (
                  <Chip label={`${cantidadItems} items`} size="small" color="primary" />
                )}
              </Typography>
              {carrito.length > 0 && (
                <Button
                  size="small"
                  color="error"
                  startIcon={<ClearIcon />}
                  onClick={limpiarCarrito}
                >
                  Limpiar
                </Button>
              )}
            </Box>

            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Código</TableCell>
                    <TableCell>Producto</TableCell>
                    <TableCell align="right">Precio</TableCell>
                    <TableCell align="center">Cantidad</TableCell>
                    <TableCell align="right">Subtotal</TableCell>
                    <TableCell align="center">Acciones</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {carrito.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={6} align="center" sx={{ py: 4 }}>
                        <Typography color="textSecondary">
                          El carrito está vacío. Escanee o busque productos para agregar.
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ) : (
                    carrito.map((item) => (
                      <TableRow key={item.codigoBarras}>
                        <TableCell>
                          <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                            {item.codigoBarras}
                          </Typography>
                        </TableCell>
                        <TableCell>{item.nombre}</TableCell>
                        <TableCell align="right">${item.precioVenta?.toFixed(2)}</TableCell>
                        <TableCell align="center">
                          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 1 }}>
                            <IconButton
                              size="small"
                              onClick={() => modificarCantidad(item.codigoBarras, -1)}
                              disabled={item.cantidad <= 1}
                            >
                              <RemoveIcon />
                            </IconButton>
                            <Typography sx={{ minWidth: 30, textAlign: 'center' }}>
                              {item.cantidad}
                            </Typography>
                            <IconButton
                              size="small"
                              onClick={() => modificarCantidad(item.codigoBarras, 1)}
                              disabled={item.cantidad >= item.stockActual}
                            >
                              <AddIcon />
                            </IconButton>
                          </Box>
                        </TableCell>
                        <TableCell align="right">
                          <Typography fontWeight="bold">
                            ${(item.precioVenta * item.cantidad).toFixed(2)}
                          </Typography>
                        </TableCell>
                        <TableCell align="center">
                          <IconButton
                            size="small"
                            color="error"
                            onClick={() => eliminarDelCarrito(item.codigoBarras)}
                          >
                            <DeleteIcon />
                          </IconButton>
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Grid>

        {/* Panel de resumen */}
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3, position: 'sticky', top: 80 }}>
            <Typography variant="h6" gutterBottom>
              Resumen de Venta
            </Typography>
            <Divider sx={{ my: 2 }} />

            <Box sx={{ mb: 2 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                <Typography>Productos:</Typography>
                <Typography>{cantidadItems}</Typography>
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                <Typography variant="h5" fontWeight="bold">TOTAL:</Typography>
                <Typography variant="h5" fontWeight="bold" color="primary">
                  ${total.toFixed(2)}
                </Typography>
              </Box>
            </Box>

            <TextField
              fullWidth
              multiline
              rows={2}
              label="Observaciones (opcional)"
              value={observaciones}
              onChange={(e) => setObservaciones(e.target.value)}
              sx={{ mb: 2 }}
            />

            <Button
              fullWidth
              variant="contained"
              size="large"
              color="success"
              disabled={carrito.length === 0 || loading}
              onClick={() => setConfirmDialog(true)}
              startIcon={loading ? <CircularProgress size={20} /> : <CartIcon />}
            >
              {loading ? 'Procesando...' : 'Confirmar Venta'}
            </Button>
          </Paper>
        </Grid>
      </Grid>

      {/* Diálogo de confirmación */}
      <Dialog open={confirmDialog} onClose={() => setConfirmDialog(false)}>
        <DialogTitle>Confirmar Venta</DialogTitle>
        <DialogContent>
          <Typography>¿Está seguro de confirmar esta venta?</Typography>
          <Box sx={{ mt: 2, p: 2, bgcolor: 'grey.100', borderRadius: 1 }}>
            <Typography>Productos: {cantidadItems}</Typography>
            <Typography variant="h6" fontWeight="bold">Total: ${total.toFixed(2)}</Typography>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmDialog(false)}>Cancelar</Button>
          <Button onClick={confirmarVenta} variant="contained" color="success">
            Confirmar
          </Button>
        </DialogActions>
      </Dialog>

      {/* Diálogo de venta completada */}
      <Dialog open={Boolean(ventaCompletada)} onClose={() => setVentaCompletada(null)}>
        <DialogTitle sx={{ bgcolor: 'success.main', color: 'white' }}>
          ¡Venta Completada!
        </DialogTitle>
        <DialogContent sx={{ mt: 2 }}>
          <Alert severity="success" sx={{ mb: 2 }}>
            La venta #{ventaCompletada?.id} se ha registrado correctamente.
          </Alert>
          <Typography>Total: <strong>${ventaCompletada?.total?.toFixed(2)}</strong></Typography>
          <Typography>Fecha: {new Date(ventaCompletada?.fechaHora).toLocaleString()}</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setVentaCompletada(null)}>Cerrar</Button>
          <Button onClick={imprimirTicket} startIcon={<PrintIcon />} variant="contained">
            Imprimir Ticket
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default NuevaVenta;

