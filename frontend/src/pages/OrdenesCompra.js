import React, { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  Button,
  Chip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Grid,
  TextField,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Autocomplete,
  CircularProgress,
} from '@mui/material';
import { DataGrid } from '@mui/x-data-grid';
import {
  Add as AddIcon,
  Visibility as ViewIcon,
  CheckCircle as ReceiveIcon,
  Cancel as CancelIcon,
  Lightbulb as SuggestIcon,
  Delete as DeleteIcon,
} from '@mui/icons-material';
import toast from 'react-hot-toast';
import { ordenCompraService, proveedorService, productoService } from '../services/api';

const OrdenesCompra = () => {
  const [ordenes, setOrdenes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [paginationModel, setPaginationModel] = useState({ page: 0, pageSize: 20 });
  const [totalRows, setTotalRows] = useState(0);
  const [proveedores, setProveedores] = useState([]);
  const [productos, setProductos] = useState([]);

  const [dialogNueva, setDialogNueva] = useState(false);
  const [dialogDetalle, setDialogDetalle] = useState(null);
  const [dialogRecibir, setDialogRecibir] = useState(null);

  const [nuevaOrden, setNuevaOrden] = useState({
    proveedorId: null,
    observaciones: '',
    detalles: [],
  });
  const [nuevoItem, setNuevoItem] = useState({ producto: null, cantidad: 1, precioCompra: 0 });

  const fetchOrdenes = async () => {
    try {
      setLoading(true);
      const response = await ordenCompraService.listar(paginationModel.page, paginationModel.pageSize);
      setOrdenes(response.data.content);
      setTotalRows(response.data.totalElements);
    } catch (error) {
      toast.error('Error al cargar órdenes');
    } finally {
      setLoading(false);
    }
  };

  const fetchProveedores = async () => {
    try {
      const response = await proveedorService.listar();
      setProveedores(response.data);
    } catch (error) {
      console.error(error);
    }
  };

  useEffect(() => {
    fetchOrdenes();
    fetchProveedores();
  }, [paginationModel]);

  const handleBuscarProductos = async (texto) => {
    if (texto.length >= 2) {
      try {
        const response = await productoService.busquedaGeneral(texto);
        setProductos(response.data);
      } catch (error) {
        console.error(error);
      }
    }
  };

  const handleAgregarItem = () => {
    if (!nuevoItem.producto || nuevoItem.cantidad < 1) {
      toast.error('Seleccione un producto y cantidad válida');
      return;
    }

    const existe = nuevaOrden.detalles.find(d => d.productoCodigoBarras === nuevoItem.producto.codigoBarras);
    if (existe) {
      toast.error('El producto ya está en la orden');
      return;
    }

    setNuevaOrden({
      ...nuevaOrden,
      detalles: [...nuevaOrden.detalles, {
        productoCodigoBarras: nuevoItem.producto.codigoBarras,
        productoNombre: nuevoItem.producto.nombre,
        cantidadSolicitada: nuevoItem.cantidad,
        precioCompra: nuevoItem.precioCompra || nuevoItem.producto.precioVenta * 0.7,
      }],
    });
    setNuevoItem({ producto: null, cantidad: 1, precioCompra: 0 });
  };

  const handleEliminarItem = (codigoBarras) => {
    setNuevaOrden({
      ...nuevaOrden,
      detalles: nuevaOrden.detalles.filter(d => d.productoCodigoBarras !== codigoBarras),
    });
  };

  const handleCrearOrden = async () => {
    if (!nuevaOrden.proveedorId || nuevaOrden.detalles.length === 0) {
      toast.error('Seleccione un proveedor y agregue productos');
      return;
    }

    try {
      await ordenCompraService.crear(nuevaOrden);
      toast.success('Orden de compra creada');
      setDialogNueva(false);
      setNuevaOrden({ proveedorId: null, observaciones: '', detalles: [] });
      fetchOrdenes();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Error al crear orden');
    }
  };

  const handleGenerarSugerida = async () => {
    if (!nuevaOrden.proveedorId) {
      toast.error('Seleccione un proveedor primero');
      return;
    }

    try {
      const response = await ordenCompraService.sugerida(nuevaOrden.proveedorId);
      setNuevaOrden({
        ...nuevaOrden,
        detalles: response.data.detalles || [],
      });
      toast.success('Orden sugerida generada');
    } catch (error) {
      toast.error(error.response?.data?.message || 'No hay productos con stock bajo para este proveedor');
    }
  };

  const handleVerDetalle = async (id) => {
    try {
      const response = await ordenCompraService.obtener(id);
      setDialogDetalle(response.data);
    } catch (error) {
      toast.error('Error al cargar detalle');
    }
  };

  const handleCancelar = async (id) => {
    if (window.confirm('¿Está seguro de cancelar esta orden?')) {
      try {
        await ordenCompraService.cancelar(id);
        toast.success('Orden cancelada');
        fetchOrdenes();
        setDialogDetalle(null);
      } catch (error) {
        toast.error(error.response?.data?.message || 'Error al cancelar');
      }
    }
  };

  const handleRecibir = async () => {
    try {
      await ordenCompraService.recibir(dialogRecibir.id, dialogRecibir.detalles);
      toast.success('Recepción registrada correctamente');
      setDialogRecibir(null);
      fetchOrdenes();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Error al registrar recepción');
    }
  };

  const getEstadoColor = (estado) => {
    switch (estado) {
      case 'PENDIENTE': return 'warning';
      case 'ENVIADA': return 'info';
      case 'RECIBIDA_PARCIAL': return 'secondary';
      case 'RECIBIDA': return 'success';
      case 'CANCELADA': return 'error';
      default: return 'default';
    }
  };

  const columns = [
    { field: 'id', headerName: 'ID', width: 70 },
    {
      field: 'fechaOrden',
      headerName: 'Fecha',
      width: 170,
      renderCell: (params) => new Date(params.value).toLocaleString(),
    },
    { field: 'proveedorNombre', headerName: 'Proveedor', flex: 1, minWidth: 200 },
    {
      field: 'estado',
      headerName: 'Estado',
      width: 140,
      renderCell: (params) => (
        <Chip label={params.value} size="small" color={getEstadoColor(params.value)} />
      ),
    },
    {
      field: 'total',
      headerName: 'Total',
      width: 120,
      renderCell: (params) => `$${params.value?.toFixed(2)}`,
    },
    {
      field: 'acciones',
      headerName: 'Acciones',
      width: 180,
      sortable: false,
      renderCell: (params) => (
        <Box>
          <IconButton size="small" onClick={() => handleVerDetalle(params.row.id)} title="Ver detalle">
            <ViewIcon />
          </IconButton>
          {['PENDIENTE', 'ENVIADA', 'RECIBIDA_PARCIAL'].includes(params.row.estado) && (
            <IconButton
              size="small"
              color="success"
              onClick={() => {
                handleVerDetalle(params.row.id);
                setTimeout(() => setDialogRecibir(params.row), 500);
              }}
              title="Recibir"
            >
              <ReceiveIcon />
            </IconButton>
          )}
          {params.row.estado !== 'RECIBIDA' && params.row.estado !== 'CANCELADA' && (
            <IconButton size="small" color="error" onClick={() => handleCancelar(params.row.id)} title="Cancelar">
              <CancelIcon />
            </IconButton>
          )}
        </Box>
      ),
    },
  ];

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" sx={{ fontWeight: 'bold' }}>
          Órdenes de Compra
        </Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogNueva(true)}>
          Nueva Orden
        </Button>
      </Box>

      <Paper sx={{ height: 600 }}>
        <DataGrid
          rows={ordenes}
          columns={columns}
          loading={loading}
          paginationModel={paginationModel}
          onPaginationModelChange={setPaginationModel}
          pageSizeOptions={[10, 20, 50]}
          rowCount={totalRows}
          paginationMode="server"
          disableRowSelectionOnClick
        />
      </Paper>

      {/* Diálogo Nueva Orden */}
      <Dialog open={dialogNueva} onClose={() => setDialogNueva(false)} maxWidth="md" fullWidth>
        <DialogTitle>Nueva Orden de Compra</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} sm={8}>
              <Autocomplete
                options={proveedores}
                getOptionLabel={(option) => option.nombre}
                onChange={(e, value) => setNuevaOrden({ ...nuevaOrden, proveedorId: value?.id })}
                renderInput={(params) => <TextField {...params} label="Proveedor" required />}
              />
            </Grid>
            <Grid item xs={12} sm={4}>
              <Button
                fullWidth
                variant="outlined"
                startIcon={<SuggestIcon />}
                onClick={handleGenerarSugerida}
                sx={{ height: '100%' }}
              >
                Generar Sugerida
              </Button>
            </Grid>

            <Grid item xs={12}>
              <Typography variant="subtitle1" sx={{ mt: 2, mb: 1 }}>Agregar Productos</Typography>
            </Grid>
            <Grid item xs={12} sm={5}>
              <Autocomplete
                options={productos}
                getOptionLabel={(option) => `${option.codigoBarras} - ${option.nombre}`}
                value={nuevoItem.producto}
                onInputChange={(e, value) => handleBuscarProductos(value)}
                onChange={(e, value) => setNuevoItem({ ...nuevoItem, producto: value })}
                renderInput={(params) => <TextField {...params} label="Buscar Producto" size="small" />}
              />
            </Grid>
            <Grid item xs={6} sm={2}>
              <TextField
                fullWidth
                size="small"
                type="number"
                label="Cantidad"
                value={nuevoItem.cantidad}
                onChange={(e) => setNuevoItem({ ...nuevoItem, cantidad: parseInt(e.target.value) || 1 })}
              />
            </Grid>
            <Grid item xs={6} sm={3}>
              <TextField
                fullWidth
                size="small"
                type="number"
                label="Precio Compra"
                value={nuevoItem.precioCompra}
                onChange={(e) => setNuevoItem({ ...nuevoItem, precioCompra: parseFloat(e.target.value) || 0 })}
              />
            </Grid>
            <Grid item xs={12} sm={2}>
              <Button fullWidth variant="contained" onClick={handleAgregarItem}>
                Agregar
              </Button>
            </Grid>

            <Grid item xs={12}>
              <TableContainer component={Paper} variant="outlined">
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Producto</TableCell>
                      <TableCell align="right">Cantidad</TableCell>
                      <TableCell align="right">Precio</TableCell>
                      <TableCell align="right">Subtotal</TableCell>
                      <TableCell></TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {nuevaOrden.detalles.map((item) => (
                      <TableRow key={item.productoCodigoBarras}>
                        <TableCell>{item.productoNombre}</TableCell>
                        <TableCell align="right">{item.cantidadSolicitada}</TableCell>
                        <TableCell align="right">${item.precioCompra?.toFixed(2)}</TableCell>
                        <TableCell align="right">
                          ${(item.cantidadSolicitada * item.precioCompra).toFixed(2)}
                        </TableCell>
                        <TableCell>
                          <IconButton size="small" onClick={() => handleEliminarItem(item.productoCodigoBarras)}>
                            <DeleteIcon />
                          </IconButton>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </Grid>

            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={2}
                label="Observaciones"
                value={nuevaOrden.observaciones}
                onChange={(e) => setNuevaOrden({ ...nuevaOrden, observaciones: e.target.value })}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogNueva(false)}>Cancelar</Button>
          <Button onClick={handleCrearOrden} variant="contained">
            Crear Orden
          </Button>
        </DialogActions>
      </Dialog>

      {/* Diálogo Detalle */}
      <Dialog open={Boolean(dialogDetalle)} onClose={() => setDialogDetalle(null)} maxWidth="md" fullWidth>
        <DialogTitle>
          Orden de Compra #{dialogDetalle?.id}
          <Chip label={dialogDetalle?.estado} size="small" color={getEstadoColor(dialogDetalle?.estado)} sx={{ ml: 2 }} />
        </DialogTitle>
        <DialogContent>
          <Typography>Proveedor: {dialogDetalle?.proveedorNombre}</Typography>
          <Typography>Fecha: {new Date(dialogDetalle?.fechaOrden).toLocaleString()}</Typography>

          <TableContainer sx={{ mt: 2 }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Producto</TableCell>
                  <TableCell align="right">Solicitado</TableCell>
                  <TableCell align="right">Recibido</TableCell>
                  <TableCell align="right">Precio</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {dialogDetalle?.detalles?.map((item) => (
                  <TableRow key={item.id}>
                    <TableCell>{item.productoNombre}</TableCell>
                    <TableCell align="right">{item.cantidadSolicitada}</TableCell>
                    <TableCell align="right">{item.cantidadRecibida}</TableCell>
                    <TableCell align="right">${item.precioCompra?.toFixed(2)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
          <Typography variant="h6" sx={{ mt: 2 }} align="right">
            Total: ${dialogDetalle?.total?.toFixed(2)}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogDetalle(null)}>Cerrar</Button>
        </DialogActions>
      </Dialog>

      {/* Diálogo Recibir */}
      <Dialog open={Boolean(dialogRecibir)} onClose={() => setDialogRecibir(null)} maxWidth="md" fullWidth>
        <DialogTitle>Recibir Mercancía - Orden #{dialogRecibir?.id}</DialogTitle>
        <DialogContent>
          <Typography sx={{ mb: 2 }}>Ingrese las cantidades recibidas:</Typography>
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Producto</TableCell>
                  <TableCell align="right">Solicitado</TableCell>
                  <TableCell align="right">Ya Recibido</TableCell>
                  <TableCell align="right">Recibir Ahora</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {dialogRecibir?.detalles?.map((item, index) => (
                  <TableRow key={item.id || index}>
                    <TableCell>{item.productoNombre}</TableCell>
                    <TableCell align="right">{item.cantidadSolicitada}</TableCell>
                    <TableCell align="right">{item.cantidadRecibida || 0}</TableCell>
                    <TableCell align="right">
                      <TextField
                        type="number"
                        size="small"
                        sx={{ width: 80 }}
                        defaultValue={0}
                        inputProps={{ min: 0 }}
                        onChange={(e) => {
                          const newDetalles = [...dialogRecibir.detalles];
                          newDetalles[index] = {
                            ...newDetalles[index],
                            cantidadRecibida: parseInt(e.target.value) || 0,
                          };
                          setDialogRecibir({ ...dialogRecibir, detalles: newDetalles });
                        }}
                      />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogRecibir(null)}>Cancelar</Button>
          <Button onClick={handleRecibir} variant="contained" color="success">
            Confirmar Recepción
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default OrdenesCompra;

