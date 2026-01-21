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
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Grid,
} from '@mui/material';
import { DataGrid } from '@mui/x-data-grid';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import {
  Visibility as ViewIcon,
  Cancel as CancelIcon,
  Print as PrintIcon,
  FilterList as FilterIcon,
} from '@mui/icons-material';
import dayjs from 'dayjs';
import toast from 'react-hot-toast';
import { ventaService } from '../services/api';

const Ventas = () => {
  const [ventas, setVentas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [paginationModel, setPaginationModel] = useState({ page: 0, pageSize: 20 });
  const [totalRows, setTotalRows] = useState(0);
  const [ventaDetalle, setVentaDetalle] = useState(null);
  const [fechaInicio, setFechaInicio] = useState(dayjs().subtract(30, 'day'));
  const [fechaFin, setFechaFin] = useState(dayjs());

  const fetchVentas = async () => {
    try {
      setLoading(true);
      const response = await ventaService.listar(paginationModel.page, paginationModel.pageSize);
      setVentas(response.data.content);
      setTotalRows(response.data.totalElements);
    } catch (error) {
      toast.error('Error al cargar ventas');
    } finally {
      setLoading(false);
    }
  };

  const fetchVentasPorFecha = async () => {
    try {
      setLoading(true);
      const response = await ventaService.ventasPorFecha(
        fechaInicio.format('YYYY-MM-DD'),
        fechaFin.format('YYYY-MM-DD')
      );
      setVentas(response.data);
      setTotalRows(response.data.length);
    } catch (error) {
      toast.error('Error al filtrar ventas');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchVentas();
  }, [paginationModel]);

  const handleVerDetalle = async (id) => {
    try {
      const response = await ventaService.obtener(id);
      setVentaDetalle(response.data);
    } catch (error) {
      toast.error('Error al cargar detalle');
    }
  };

  const handleCancelarVenta = async (id) => {
    if (window.confirm('¿Está seguro de cancelar esta venta? Se devolverá el stock.')) {
      try {
        await ventaService.cancelar(id);
        toast.success('Venta cancelada correctamente');
        fetchVentas();
        setVentaDetalle(null);
      } catch (error) {
        toast.error(error.response?.data?.message || 'Error al cancelar venta');
      }
    }
  };

  const handleImprimirTicket = async (id) => {
    try {
      const response = await ventaService.generarTicket(id);
      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      window.open(url, '_blank');
    } catch (error) {
      toast.error('Error al generar ticket');
    }
  };

  const columns = [
    { field: 'id', headerName: 'ID', width: 80 },
    {
      field: 'fechaHora',
      headerName: 'Fecha y Hora',
      width: 180,
      renderCell: (params) => new Date(params.value).toLocaleString(),
    },
    {
      field: 'total',
      headerName: 'Total',
      width: 120,
      renderCell: (params) => (
        <Typography fontWeight="bold">${params.value?.toFixed(2)}</Typography>
      ),
    },
    {
      field: 'estado',
      headerName: 'Estado',
      width: 120,
      renderCell: (params) => (
        <Chip
          label={params.value}
          size="small"
          color={params.value === 'COMPLETADA' ? 'success' : params.value === 'CANCELADA' ? 'error' : 'warning'}
        />
      ),
    },
    {
      field: 'detalles',
      headerName: 'Productos',
      width: 100,
      renderCell: (params) => params.value?.length || 0,
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
          <IconButton size="small" onClick={() => handleImprimirTicket(params.row.id)} title="Imprimir ticket">
            <PrintIcon />
          </IconButton>
          {params.row.estado === 'COMPLETADA' && (
            <IconButton
              size="small"
              color="error"
              onClick={() => handleCancelarVenta(params.row.id)}
              title="Cancelar venta"
            >
              <CancelIcon />
            </IconButton>
          )}
        </Box>
      ),
    },
  ];

  return (
    <LocalizationProvider dateAdapter={AdapterDayjs}>
      <Box>
        <Typography variant="h4" sx={{ fontWeight: 'bold', mb: 3 }}>
          Historial de Ventas
        </Typography>

        {/* Filtros de fecha */}
        <Paper sx={{ p: 2, mb: 2 }}>
          <Grid container spacing={2} alignItems="center">
            <Grid item>
              <FilterIcon />
            </Grid>
            <Grid item xs={12} sm={3}>
              <DatePicker
                label="Fecha Inicio"
                value={fechaInicio}
                onChange={(newValue) => setFechaInicio(newValue)}
                slotProps={{ textField: { size: 'small', fullWidth: true } }}
              />
            </Grid>
            <Grid item xs={12} sm={3}>
              <DatePicker
                label="Fecha Fin"
                value={fechaFin}
                onChange={(newValue) => setFechaFin(newValue)}
                slotProps={{ textField: { size: 'small', fullWidth: true } }}
              />
            </Grid>
            <Grid item>
              <Button variant="contained" onClick={fetchVentasPorFecha}>
                Filtrar
              </Button>
            </Grid>
            <Grid item>
              <Button variant="outlined" onClick={fetchVentas}>
                Ver Todas
              </Button>
            </Grid>
          </Grid>
        </Paper>

        {/* Tabla de ventas */}
        <Paper sx={{ height: 600 }}>
          <DataGrid
            rows={ventas}
            columns={columns}
            loading={loading}
            paginationModel={paginationModel}
            onPaginationModelChange={setPaginationModel}
            pageSizeOptions={[10, 20, 50]}
            rowCount={totalRows}
            paginationMode="server"
            disableRowSelectionOnClick
            localeText={{
              noRowsLabel: 'No hay ventas',
              MuiTablePagination: { labelRowsPerPage: 'Filas por página:' },
            }}
          />
        </Paper>

        {/* Diálogo de detalle */}
        <Dialog open={Boolean(ventaDetalle)} onClose={() => setVentaDetalle(null)} maxWidth="md" fullWidth>
          <DialogTitle>
            Detalle de Venta #{ventaDetalle?.id}
            <Chip
              label={ventaDetalle?.estado}
              size="small"
              color={ventaDetalle?.estado === 'COMPLETADA' ? 'success' : 'error'}
              sx={{ ml: 2 }}
            />
          </DialogTitle>
          <DialogContent>
            <Box sx={{ mb: 2 }}>
              <Typography>Fecha: {new Date(ventaDetalle?.fechaHora).toLocaleString()}</Typography>
              {ventaDetalle?.observaciones && (
                <Typography>Observaciones: {ventaDetalle.observaciones}</Typography>
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
                  </TableRow>
                </TableHead>
                <TableBody>
                  {ventaDetalle?.detalles?.map((item) => (
                    <TableRow key={item.id}>
                      <TableCell>{item.productoCodigoBarras}</TableCell>
                      <TableCell>{item.productoNombre}</TableCell>
                      <TableCell align="right">${item.precioUnitario?.toFixed(2)}</TableCell>
                      <TableCell align="center">{item.cantidad}</TableCell>
                      <TableCell align="right">${item.subtotal?.toFixed(2)}</TableCell>
                    </TableRow>
                  ))}
                  <TableRow>
                    <TableCell colSpan={4} align="right">
                      <Typography variant="h6">TOTAL:</Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Typography variant="h6" color="primary">
                        ${ventaDetalle?.total?.toFixed(2)}
                      </Typography>
                    </TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </TableContainer>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setVentaDetalle(null)}>Cerrar</Button>
            <Button onClick={() => handleImprimirTicket(ventaDetalle?.id)} startIcon={<PrintIcon />}>
              Imprimir
            </Button>
            {ventaDetalle?.estado === 'COMPLETADA' && (
              <Button
                color="error"
                onClick={() => handleCancelarVenta(ventaDetalle?.id)}
                startIcon={<CancelIcon />}
              >
                Cancelar Venta
              </Button>
            )}
          </DialogActions>
        </Dialog>
      </Box>
    </LocalizationProvider>
  );
};

export default Ventas;

