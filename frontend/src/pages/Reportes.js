                    <Typography variant="h4">{reporteVentas.cantidadVentas}</Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={6} sm={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography color="textSecondary">Productos Vendidos</Typography>
                    <Typography variant="h4">{reporteVentas.cantidadProductosVendidos}</Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={6} sm={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography color="textSecondary">Promedio/Venta</Typography>
                    <Typography variant="h4">${reporteVentas.promedioVenta?.toFixed(2)}</Typography>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>

            {ventasChartData && (
              <Box sx={{ mt: 3, height: 300 }}>
                <Typography variant="h6" gutterBottom>Ventas Diarias</Typography>
                <Bar
                  data={ventasChartData}
                  options={{
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { display: false } },
                  }}
                />
              </Box>
            )}

            {reporteVentas.productosMasVendidos?.length > 0 && (
              <Box sx={{ mt: 3 }}>
                <Typography variant="h6" gutterBottom>Productos Más Vendidos</Typography>
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>#</TableCell>
                        <TableCell>Código</TableCell>
                        <TableCell>Producto</TableCell>
                        <TableCell align="right">Cantidad Vendida</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {reporteVentas.productosMasVendidos.map((prod, index) => (
                        <TableRow key={prod.codigoBarras}>
                          <TableCell>{index + 1}</TableCell>
                          <TableCell>{prod.codigoBarras}</TableCell>
                          <TableCell>{prod.nombre}</TableCell>
                          <TableCell align="right">{prod.cantidadVendida}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Box>
            )}
          </Paper>
        )}
      </Box>
    </LocalizationProvider>
  );
};

export default Reportes;
import React, { useState } from 'react';
import {
  Box,
  Paper,
  Typography,
  Button,
  Grid,
  Card,
  CardContent,
  CardActions,
  CircularProgress,
  Alert,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from '@mui/material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import {
  Assessment as ReportIcon,
  Inventory as InventoryIcon,
  ShoppingCart as SalesIcon,
  PictureAsPdf as PdfIcon,
} from '@mui/icons-material';
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend } from 'chart.js';
import { Bar } from 'react-chartjs-2';
import dayjs from 'dayjs';
import toast from 'react-hot-toast';
import { reporteService } from '../services/api';

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

const Reportes = () => {
  const [fechaInicio, setFechaInicio] = useState(dayjs().subtract(30, 'day'));
  const [fechaFin, setFechaFin] = useState(dayjs());
  const [loading, setLoading] = useState(false);
  const [reporteInventario, setReporteInventario] = useState(null);
  const [reporteVentas, setReporteVentas] = useState(null);

  const generarReporteInventario = async () => {
    try {
      setLoading(true);
      const response = await reporteService.inventario();
      setReporteInventario(response.data);
      setReporteVentas(null);
    } catch (error) {
      toast.error('Error al generar reporte');
    } finally {
      setLoading(false);
    }
  };

  const generarReporteVentas = async () => {
    try {
      setLoading(true);
      const response = await reporteService.ventas(
        fechaInicio.format('YYYY-MM-DD'),
        fechaFin.format('YYYY-MM-DD')
      );
      setReporteVentas(response.data);
      setReporteInventario(null);
    } catch (error) {
      toast.error('Error al generar reporte');
    } finally {
      setLoading(false);
    }
  };

  const descargarPdfInventario = async () => {
    try {
      const response = await reporteService.inventarioPdf();
      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'reporte-inventario.pdf';
      link.click();
      toast.success('PDF descargado');
    } catch (error) {
      toast.error('Error al descargar PDF');
    }
  };

  const descargarPdfVentas = async () => {
    try {
      const response = await reporteService.ventasPdf(
        fechaInicio.format('YYYY-MM-DD'),
        fechaFin.format('YYYY-MM-DD')
      );
      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `reporte-ventas-${fechaInicio.format('YYYY-MM-DD')}-${fechaFin.format('YYYY-MM-DD')}.pdf`;
      link.click();
      toast.success('PDF descargado');
    } catch (error) {
      toast.error('Error al descargar PDF');
    }
  };

  const ventasChartData = reporteVentas?.ventasDiarias ? {
    labels: reporteVentas.ventasDiarias.map(v => dayjs(v.fecha).format('DD/MM')),
    datasets: [{
      label: 'Ventas ($)',
      data: reporteVentas.ventasDiarias.map(v => v.total),
      backgroundColor: 'rgba(25, 118, 210, 0.6)',
      borderColor: 'rgba(25, 118, 210, 1)',
      borderWidth: 1,
    }],
  } : null;

  return (
    <LocalizationProvider dateAdapter={AdapterDayjs}>
      <Box>
        <Typography variant="h4" sx={{ fontWeight: 'bold', mb: 3 }}>
          Reportes y Análisis
        </Typography>

        {/* Selección de reporte */}
        <Grid container spacing={3} sx={{ mb: 3 }}>
          <Grid item xs={12} md={6}>
            <Card>
              <CardContent>
                <Typography variant="h6" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <InventoryIcon color="primary" /> Reporte de Inventario
                </Typography>
                <Typography variant="body2" color="textSecondary" sx={{ mt: 1 }}>
                  Valor del inventario, productos por categoría, stock bajo y agotados.
                </Typography>
              </CardContent>
              <CardActions>
                <Button onClick={generarReporteInventario} disabled={loading}>
                  Generar Reporte
                </Button>
                <Button startIcon={<PdfIcon />} onClick={descargarPdfInventario} disabled={loading}>
                  Descargar PDF
                </Button>
              </CardActions>
            </Card>
          </Grid>

          <Grid item xs={12} md={6}>
            <Card>
              <CardContent>
                <Typography variant="h6" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <SalesIcon color="success" /> Reporte de Ventas
                </Typography>
                <Grid container spacing={2} sx={{ mt: 1 }}>
                  <Grid item xs={6}>
                    <DatePicker
                      label="Desde"
                      value={fechaInicio}
                      onChange={setFechaInicio}
                      slotProps={{ textField: { size: 'small', fullWidth: true } }}
                    />
                  </Grid>
                  <Grid item xs={6}>
                    <DatePicker
                      label="Hasta"
                      value={fechaFin}
                      onChange={setFechaFin}
                      slotProps={{ textField: { size: 'small', fullWidth: true } }}
                    />
                  </Grid>
                </Grid>
              </CardContent>
              <CardActions>
                <Button onClick={generarReporteVentas} disabled={loading}>
                  Generar Reporte
                </Button>
                <Button startIcon={<PdfIcon />} onClick={descargarPdfVentas} disabled={loading}>
                  Descargar PDF
                </Button>
              </CardActions>
            </Card>
          </Grid>
        </Grid>

        {loading && (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
            <CircularProgress />
          </Box>
        )}

        {/* Reporte de Inventario */}
        {reporteInventario && (
          <Paper sx={{ p: 3 }}>
            <Typography variant="h5" gutterBottom>
              <ReportIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
              Reporte de Inventario
            </Typography>

            <Grid container spacing={3} sx={{ mt: 2 }}>
              <Grid item xs={6} sm={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography color="textSecondary">Total Productos</Typography>
                    <Typography variant="h4">{reporteInventario.totalProductos}</Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={6} sm={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography color="textSecondary">Con Stock</Typography>
                    <Typography variant="h4" color="success.main">{reporteInventario.productosConStock}</Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={6} sm={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography color="textSecondary">Stock Bajo</Typography>
                    <Typography variant="h4" color="warning.main">{reporteInventario.productosStockBajo}</Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={6} sm={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography color="textSecondary">Agotados</Typography>
                    <Typography variant="h4" color="error.main">{reporteInventario.productosAgotados}</Typography>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>

            <Alert severity="info" sx={{ mt: 3 }}>
              <Typography variant="h6">
                Valor Total del Inventario: ${reporteInventario.valorTotalInventario?.toFixed(2)}
              </Typography>
            </Alert>

            {reporteInventario.inventarioPorCategoria?.length > 0 && (
              <Box sx={{ mt: 3 }}>
                <Typography variant="h6" gutterBottom>Inventario por Categoría</Typography>
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Categoría</TableCell>
                        <TableCell align="right">Productos</TableCell>
                        <TableCell align="right">Unidades</TableCell>
                        <TableCell align="right">Valor</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {reporteInventario.inventarioPorCategoria.map((cat) => (
                        <TableRow key={cat.categoriaId}>
                          <TableCell>{cat.categoriaNombre}</TableCell>
                          <TableCell align="right">{cat.cantidadProductos}</TableCell>
                          <TableCell align="right">{cat.totalUnidades}</TableCell>
                          <TableCell align="right">${cat.valorTotal?.toFixed(2)}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Box>
            )}
          </Paper>
        )}

        {/* Reporte de Ventas */}
        {reporteVentas && (
          <Paper sx={{ p: 3 }}>
            <Typography variant="h5" gutterBottom>
              <ReportIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
              Reporte de Ventas ({dayjs(reporteVentas.fechaInicio).format('DD/MM/YYYY')} - {dayjs(reporteVentas.fechaFin).format('DD/MM/YYYY')})
            </Typography>

            <Grid container spacing={3} sx={{ mt: 2 }}>
              <Grid item xs={6} sm={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography color="textSecondary">Total Ventas</Typography>
                    <Typography variant="h4" color="success.main">${reporteVentas.totalVentas?.toFixed(2)}</Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={6} sm={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography color="textSecondary">Cantidad Ventas</Typography>
import React, { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  Chip,
  Grid,
  TextField,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Autocomplete,
} from '@mui/material';
import { DataGrid } from '@mui/x-data-grid';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import {
  Add as AddIcon,
  FilterList as FilterIcon,
} from '@mui/icons-material';
import dayjs from 'dayjs';
import toast from 'react-hot-toast';
import { movimientoService, productoService } from '../services/api';

const Movimientos = () => {
  const [movimientos, setMovimientos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [paginationModel, setPaginationModel] = useState({ page: 0, pageSize: 20 });
  const [totalRows, setTotalRows] = useState(0);
  const [fechaInicio, setFechaInicio] = useState(dayjs().subtract(30, 'day'));
  const [fechaFin, setFechaFin] = useState(dayjs());
  const [dialogOpen, setDialogOpen] = useState(false);
  const [productos, setProductos] = useState([]);
  const [formData, setFormData] = useState({
    codigoBarras: '',
    cantidad: 1,
    motivo: '',
    esPositivo: true,
  });

  const fetchMovimientos = async () => {
    try {
      setLoading(true);
      const response = await movimientoService.listar(paginationModel.page, paginationModel.pageSize);
      setMovimientos(response.data.content);
      setTotalRows(response.data.totalElements);
    } catch (error) {
      toast.error('Error al cargar movimientos');
    } finally {
      setLoading(false);
    }
  };

  const fetchMovimientosPorFecha = async () => {
    try {
      setLoading(true);
      const response = await movimientoService.listarPorFecha(
        fechaInicio.format('YYYY-MM-DD'),
        fechaFin.format('YYYY-MM-DD')
      );
      setMovimientos(response.data);
      setTotalRows(response.data.length);
    } catch (error) {
      toast.error('Error al filtrar movimientos');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMovimientos();
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

  const handleRegistrarMovimiento = async () => {
    if (!formData.codigoBarras || !formData.cantidad || !formData.motivo) {
      toast.error('Complete todos los campos');
      return;
    }

    try {
      await movimientoService.registrarAjuste(
        formData.codigoBarras,
        formData.cantidad,
        formData.motivo,
        formData.esPositivo
      );
      toast.success('Movimiento registrado correctamente');
      setDialogOpen(false);
      setFormData({ codigoBarras: '', cantidad: 1, motivo: '', esPositivo: true });
      fetchMovimientos();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Error al registrar movimiento');
    }
  };

  const getTipoColor = (tipo) => {
    switch (tipo) {
      case 'ENTRADA': return 'success';
      case 'SALIDA': return 'error';
      case 'AJUSTE_POSITIVO': return 'info';
      case 'AJUSTE_NEGATIVO': return 'warning';
      case 'DEVOLUCION': return 'secondary';
      default: return 'default';
    }
  };

  const columns = [
    { field: 'id', headerName: 'ID', width: 70 },
    {
      field: 'fechaHora',
      headerName: 'Fecha y Hora',
      width: 170,
      renderCell: (params) => new Date(params.value).toLocaleString(),
    },
    {
      field: 'tipoMovimiento',
      headerName: 'Tipo',
      width: 140,
      renderCell: (params) => (
        <Chip label={params.value} size="small" color={getTipoColor(params.value)} />
      ),
    },
    { field: 'productoCodigoBarras', headerName: 'Código', width: 130 },
    { field: 'productoNombre', headerName: 'Producto', flex: 1, minWidth: 200 },
    { field: 'cantidad', headerName: 'Cantidad', width: 100, align: 'center' },
    { field: 'stockAnterior', headerName: 'Stock Ant.', width: 100, align: 'center' },
    { field: 'stockNuevo', headerName: 'Stock Nuevo', width: 110, align: 'center' },
    { field: 'motivo', headerName: 'Motivo', width: 200 },
    { field: 'referencia', headerName: 'Referencia', width: 120 },
  ];

  return (
    <LocalizationProvider dateAdapter={AdapterDayjs}>
      <Box>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Typography variant="h4" sx={{ fontWeight: 'bold' }}>
            Movimientos de Inventario
          </Typography>
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogOpen(true)}>
            Nuevo Ajuste
          </Button>
        </Box>

        {/* Filtros */}
        <Paper sx={{ p: 2, mb: 2 }}>
          <Grid container spacing={2} alignItems="center">
            <Grid item>
              <FilterIcon />
            </Grid>
            <Grid item xs={12} sm={3}>
              <DatePicker
                label="Fecha Inicio"
                value={fechaInicio}
                onChange={setFechaInicio}
                slotProps={{ textField: { size: 'small', fullWidth: true } }}
              />
            </Grid>
            <Grid item xs={12} sm={3}>
              <DatePicker
                label="Fecha Fin"
                value={fechaFin}
                onChange={setFechaFin}
                slotProps={{ textField: { size: 'small', fullWidth: true } }}
              />
            </Grid>
            <Grid item>
              <Button variant="contained" onClick={fetchMovimientosPorFecha}>
                Filtrar
              </Button>
            </Grid>
            <Grid item>
              <Button variant="outlined" onClick={fetchMovimientos}>
                Ver Todos
              </Button>
            </Grid>
          </Grid>
        </Paper>

        {/* Tabla */}
        <Paper sx={{ height: 600 }}>
          <DataGrid
            rows={movimientos}
            columns={columns}
            loading={loading}
            paginationModel={paginationModel}
            onPaginationModelChange={setPaginationModel}
            pageSizeOptions={[10, 20, 50]}
            rowCount={totalRows}
            paginationMode="server"
            disableRowSelectionOnClick
            localeText={{
              noRowsLabel: 'No hay movimientos',
              MuiTablePagination: { labelRowsPerPage: 'Filas por página:' },
            }}
          />
        </Paper>

        {/* Diálogo de nuevo ajuste */}
        <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
          <DialogTitle>Registrar Ajuste de Inventario</DialogTitle>
          <DialogContent>
            <Grid container spacing={2} sx={{ mt: 1 }}>
              <Grid item xs={12}>
                <Autocomplete
                  options={productos}
                  getOptionLabel={(option) =>
                    typeof option === 'string' ? option : `${option.codigoBarras} - ${option.nombre}`
                  }
                  onInputChange={(e, value) => handleBuscarProductos(value)}
                  onChange={(e, value) => setFormData({ ...formData, codigoBarras: value?.codigoBarras || '' })}
                  renderInput={(params) => (
                    <TextField {...params} label="Buscar Producto" required />
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <FormControl fullWidth>
                  <InputLabel>Tipo de Ajuste</InputLabel>
                  <Select
                    value={formData.esPositivo}
                    label="Tipo de Ajuste"
                    onChange={(e) => setFormData({ ...formData, esPositivo: e.target.value })}
                  >
                    <MenuItem value={true}>Entrada / Agregar Stock</MenuItem>
                    <MenuItem value={false}>Salida / Reducir Stock</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  type="number"
                  label="Cantidad"
                  value={formData.cantidad}
                  onChange={(e) => setFormData({ ...formData, cantidad: parseInt(e.target.value) || 1 })}
                  inputProps={{ min: 1 }}
                  required
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  multiline
                  rows={2}
                  label="Motivo del Ajuste"
                  value={formData.motivo}
                  onChange={(e) => setFormData({ ...formData, motivo: e.target.value })}
                  required
                  placeholder="Ej: Corrección de inventario, merma, etc."
                />
              </Grid>
            </Grid>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDialogOpen(false)}>Cancelar</Button>
            <Button onClick={handleRegistrarMovimiento} variant="contained">
              Registrar
            </Button>
          </DialogActions>
        </Dialog>
      </Box>
    </LocalizationProvider>
  );
};

export default Movimientos;

