import React, { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  Button,
  TextField,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Grid,
  Card,
  CardContent,
  CardActions,
  CircularProgress,
  Chip,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Phone as PhoneIcon,
  Email as EmailIcon,
  Business as BusinessIcon,
} from '@mui/icons-material';
import toast from 'react-hot-toast';
import { proveedorService } from '../services/api';

const Proveedores = () => {
  const [proveedores, setProveedores] = useState([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editando, setEditando] = useState(null);
  const [formData, setFormData] = useState({
    nombre: '',
    ruc: '',
    direccion: '',
    telefono: '',
    email: '',
    contactoPrincipal: '',
    productosSuministra: '',
  });
  const [saving, setSaving] = useState(false);

  const fetchProveedores = async () => {
    try {
      setLoading(true);
      const response = await proveedorService.listar();
      setProveedores(response.data);
    } catch (error) {
      toast.error('Error al cargar proveedores');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProveedores();
  }, []);

  const handleOpenDialog = (proveedor = null) => {
    if (proveedor) {
      setEditando(proveedor);
      setFormData({
        nombre: proveedor.nombre,
        ruc: proveedor.ruc || '',
        direccion: proveedor.direccion || '',
        telefono: proveedor.telefono || '',
        email: proveedor.email || '',
        contactoPrincipal: proveedor.contactoPrincipal || '',
        productosSuministra: proveedor.productosSuministra || '',
      });
    } else {
      setEditando(null);
      setFormData({
        nombre: '',
        ruc: '',
        direccion: '',
        telefono: '',
        email: '',
        contactoPrincipal: '',
        productosSuministra: '',
      });
    }
    setDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
    setEditando(null);
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleGuardar = async () => {
    if (!formData.nombre.trim()) {
      toast.error('El nombre es obligatorio');
      return;
    }

    try {
      setSaving(true);
      if (editando) {
        await proveedorService.actualizar(editando.id, formData);
        toast.success('Proveedor actualizado');
      } else {
        await proveedorService.crear(formData);
        toast.success('Proveedor creado');
      }
      handleCloseDialog();
      fetchProveedores();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Error al guardar');
    } finally {
      setSaving(false);
    }
  };

  const handleEliminar = async (id, nombre) => {
    if (window.confirm(`¿Está seguro de eliminar el proveedor "${nombre}"?`)) {
      try {
        await proveedorService.eliminar(id);
        toast.success('Proveedor eliminado');
        fetchProveedores();
      } catch (error) {
        toast.error('Error al eliminar proveedor');
      }
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
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" sx={{ fontWeight: 'bold' }}>
          Proveedores
        </Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpenDialog()}>
          Nuevo Proveedor
        </Button>
      </Box>

      <Grid container spacing={3}>
        {proveedores.length === 0 ? (
          <Grid item xs={12}>
            <Paper sx={{ p: 4, textAlign: 'center' }}>
              <Typography color="textSecondary">No hay proveedores registrados</Typography>
            </Paper>
          </Grid>
        ) : (
          proveedores.map((proveedor) => (
            <Grid item xs={12} sm={6} md={4} key={proveedor.id}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    {proveedor.nombre}
                  </Typography>
                  {proveedor.ruc && (
                    <Typography variant="body2" color="textSecondary" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <BusinessIcon fontSize="small" /> RUC: {proveedor.ruc}
                    </Typography>
                  )}
                  {proveedor.telefono && (
                    <Typography variant="body2" color="textSecondary" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <PhoneIcon fontSize="small" /> {proveedor.telefono}
                    </Typography>
                  )}
                  {proveedor.email && (
                    <Typography variant="body2" color="textSecondary" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <EmailIcon fontSize="small" /> {proveedor.email}
                    </Typography>
                  )}
                  {proveedor.contactoPrincipal && (
                    <Typography variant="body2" sx={{ mt: 1 }}>
                      Contacto: {proveedor.contactoPrincipal}
                    </Typography>
                  )}
                  <Box sx={{ mt: 1 }}>
                    <Chip label={`${proveedor.cantidadProductos || 0} productos`} size="small" />
                  </Box>
                </CardContent>
                <CardActions>
                  <Button size="small" startIcon={<EditIcon />} onClick={() => handleOpenDialog(proveedor)}>
                    Editar
                  </Button>
                  <Button size="small" color="error" startIcon={<DeleteIcon />} onClick={() => handleEliminar(proveedor.id, proveedor.nombre)}>
                    Eliminar
                  </Button>
                </CardActions>
              </Card>
            </Grid>
          ))
        )}
      </Grid>

      {/* Diálogo de creación/edición */}
      <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>{editando ? 'Editar Proveedor' : 'Nuevo Proveedor'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                fullWidth
                required
                label="Nombre / Razón Social"
                name="nombre"
                value={formData.nombre}
                onChange={handleChange}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="RUC / NIT"
                name="ruc"
                value={formData.ruc}
                onChange={handleChange}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Teléfono"
                name="telefono"
                value={formData.telefono}
                onChange={handleChange}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Email"
                name="email"
                type="email"
                value={formData.email}
                onChange={handleChange}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Dirección"
                name="direccion"
                value={formData.direccion}
                onChange={handleChange}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Contacto Principal"
                name="contactoPrincipal"
                value={formData.contactoPrincipal}
                onChange={handleChange}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={2}
                label="Productos que Suministra"
                name="productosSuministra"
                value={formData.productosSuministra}
                onChange={handleChange}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>Cancelar</Button>
          <Button onClick={handleGuardar} variant="contained" disabled={saving}>
            {saving ? <CircularProgress size={20} /> : 'Guardar'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Proveedores;
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Paper,
  Typography,
  TextField,
  Button,
  Grid,
  Autocomplete,
  FormControlLabel,
  Checkbox,
  Chip,
  InputAdornment,
  Slider,
  Collapse,
  IconButton,
} from '@mui/material';
import { DataGrid } from '@mui/x-data-grid';
import {
  Search as SearchIcon,
  FilterList as FilterIcon,
  Clear as ClearIcon,
  Warning as WarningIcon,
  ExpandMore as ExpandMoreIcon,
  ExpandLess as ExpandLessIcon,
} from '@mui/icons-material';
import toast from 'react-hot-toast';
import { productoService, categoriaService, proveedorService } from '../services/api';

const BusquedaAvanzada = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [productos, setProductos] = useState([]);
  const [totalRows, setTotalRows] = useState(0);
  const [categorias, setCategorias] = useState([]);
  const [proveedores, setProveedores] = useState([]);
  const [showFilters, setShowFilters] = useState(true);
  const [paginationModel, setPaginationModel] = useState({ page: 0, pageSize: 20 });

  const [filtros, setFiltros] = useState({
    texto: '',
    nombre: '',
    codigoBarras: '',
    codigoInterno: '',
    marca: '',
    modeloCompatible: '',
    categoriaId: null,
    proveedorId: null,
    stockMin: null,
    stockMax: null,
    soloAgotados: false,
    soloStockBajo: false,
    sortBy: 'nombre',
    sortDirection: 'ASC',
  });

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [catRes, provRes] = await Promise.all([
          categoriaService.listar(),
          proveedorService.listar(),
        ]);
        setCategorias(catRes.data);
        setProveedores(provRes.data);
      } catch (error) {
        console.error(error);
      }
    };
    fetchData();
  }, []);

  const handleBuscar = async () => {
    try {
      setLoading(true);
      const params = {
        ...filtros,
        page: paginationModel.page,
        size: paginationModel.pageSize,
      };

      // Limpiar valores vacíos
      Object.keys(params).forEach(key => {
        if (params[key] === '' || params[key] === null) {
          delete params[key];
        }
      });

      const response = await productoService.busquedaAvanzada(params);
      setProductos(response.data.content);
      setTotalRows(response.data.totalElements);
    } catch (error) {
      toast.error('Error en la búsqueda');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    handleBuscar();
  }, [paginationModel]);

  const handleLimpiarFiltros = () => {
    setFiltros({
      texto: '',
      nombre: '',
      codigoBarras: '',
      codigoInterno: '',
      marca: '',
      modeloCompatible: '',
      categoriaId: null,
      proveedorId: null,
      stockMin: null,
      stockMax: null,
      soloAgotados: false,
      soloStockBajo: false,
      sortBy: 'nombre',
      sortDirection: 'ASC',
    });
  };

  const handleFiltroChange = (name, value) => {
    setFiltros(prev => ({ ...prev, [name]: value }));
  };

  const columns = [
    {
      field: 'codigoBarras',
      headerName: 'Código',
      width: 140,
      renderCell: (params) => (
        <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
          {params.value}
        </Typography>
      )
    },
    {
      field: 'nombre',
      headerName: 'Nombre',
      flex: 1,
      minWidth: 200,
    },
    {
      field: 'marca',
      headerName: 'Marca',
      width: 120,
    },
    {
      field: 'categoriaNombre',
      headerName: 'Categoría',
      width: 130,
    },
    {
      field: 'precioVenta',
      headerName: 'Precio',
      width: 100,
      renderCell: (params) => `$${params.value?.toFixed(2)}`
    },
    {
      field: 'stockActual',
      headerName: 'Stock',
      width: 120,
      renderCell: (params) => {
        const row = params.row;
        let color = 'success';
        let label = params.value;
        if (row.stockActual === 0) {
          color = 'error';
          label = 'AGOTADO';
        } else if (row.stockActual <= row.stockMinimo) {
          color = 'warning';
        }

        return (
          <Chip
            label={label}
            size="small"
            color={color}
            icon={row.stockActual <= row.stockMinimo && row.stockActual > 0 ? <WarningIcon /> : null}
          />
        );
      }
    },
    {
      field: 'stockMinimo',
      headerName: 'Mín.',
      width: 70,
    },
    {
      field: 'proveedorNombre',
      headerName: 'Proveedor',
      width: 140,
    },
    {
      field: 'modeloCompatible',
      headerName: 'Modelo Compatible',
      width: 180,
    },
  ];

  return (
    <Box>
      <Typography variant="h4" sx={{ fontWeight: 'bold', mb: 3 }}>
        Búsqueda Avanzada de Productos
      </Typography>

      {/* Panel de filtros */}
      <Paper sx={{ p: 2, mb: 2 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h6" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <FilterIcon /> Filtros de Búsqueda
          </Typography>
          <IconButton onClick={() => setShowFilters(!showFilters)}>
            {showFilters ? <ExpandLessIcon /> : <ExpandMoreIcon />}
          </IconButton>
        </Box>

        <Collapse in={showFilters}>
          <Grid container spacing={2}>
            {/* Búsqueda general */}
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Búsqueda General"
                placeholder="Buscar por nombre, código, marca..."
                value={filtros.texto}
                onChange={(e) => handleFiltroChange('texto', e.target.value)}
                InputProps={{
                  startAdornment: <InputAdornment position="start"><SearchIcon /></InputAdornment>,
                }}
              />
            </Grid>

            {/* Filtros específicos */}
            <Grid item xs={12} md={3}>
              <TextField
                fullWidth
                size="small"
                label="Código de Barras"
                value={filtros.codigoBarras}
                onChange={(e) => handleFiltroChange('codigoBarras', e.target.value)}
              />
            </Grid>
            <Grid item xs={12} md={3}>
              <TextField
                fullWidth
                size="small"
                label="Código Interno"
                value={filtros.codigoInterno}
                onChange={(e) => handleFiltroChange('codigoInterno', e.target.value)}
              />
            </Grid>
            <Grid item xs={12} md={3}>
              <TextField
                fullWidth
                size="small"
                label="Marca"
                value={filtros.marca}
                onChange={(e) => handleFiltroChange('marca', e.target.value)}
              />
            </Grid>
            <Grid item xs={12} md={3}>
              <TextField
                fullWidth
                size="small"
                label="Modelo Compatible"
                value={filtros.modeloCompatible}
                onChange={(e) => handleFiltroChange('modeloCompatible', e.target.value)}
              />
            </Grid>

            {/* Selectores */}
            <Grid item xs={12} md={4}>
              <Autocomplete
                size="small"
                options={categorias}
                getOptionLabel={(option) => option.nombre || ''}
                value={categorias.find(c => c.id === filtros.categoriaId) || null}
                onChange={(e, value) => handleFiltroChange('categoriaId', value?.id || null)}
                renderInput={(params) => <TextField {...params} label="Categoría" />}
              />
            </Grid>
            <Grid item xs={12} md={4}>
              <Autocomplete
                size="small"
                options={proveedores}
                getOptionLabel={(option) => option.nombre || ''}
                value={proveedores.find(p => p.id === filtros.proveedorId) || null}
                onChange={(e, value) => handleFiltroChange('proveedorId', value?.id || null)}
                renderInput={(params) => <TextField {...params} label="Proveedor" />}
              />
            </Grid>
            <Grid item xs={12} md={4}>
              <Autocomplete
                size="small"
                options={[
                  { value: 'nombre', label: 'Nombre' },
                  { value: 'precioVenta', label: 'Precio' },
                  { value: 'stockActual', label: 'Stock' },
                  { value: 'fechaIngreso', label: 'Fecha de Ingreso' },
                ]}
                getOptionLabel={(option) => option.label}
                value={{ value: filtros.sortBy, label: filtros.sortBy === 'nombre' ? 'Nombre' :
                         filtros.sortBy === 'precioVenta' ? 'Precio' :
                         filtros.sortBy === 'stockActual' ? 'Stock' : 'Fecha de Ingreso' }}
                onChange={(e, value) => handleFiltroChange('sortBy', value?.value || 'nombre')}
                renderInput={(params) => <TextField {...params} label="Ordenar por" />}
              />
            </Grid>

            {/* Filtros de stock */}
            <Grid item xs={12} md={3}>
              <TextField
                fullWidth
                size="small"
                type="number"
                label="Stock Mínimo"
                value={filtros.stockMin || ''}
                onChange={(e) => handleFiltroChange('stockMin', e.target.value ? parseInt(e.target.value) : null)}
              />
            </Grid>
            <Grid item xs={12} md={3}>
              <TextField
                fullWidth
                size="small"
                type="number"
                label="Stock Máximo"
                value={filtros.stockMax || ''}
                onChange={(e) => handleFiltroChange('stockMax', e.target.value ? parseInt(e.target.value) : null)}
              />
            </Grid>
            <Grid item xs={12} md={3}>
              <FormControlLabel
                control={
                  <Checkbox
                    checked={filtros.soloAgotados}
                    onChange={(e) => handleFiltroChange('soloAgotados', e.target.checked)}
                  />
                }
                label="Solo Agotados"
              />
            </Grid>
            <Grid item xs={12} md={3}>
              <FormControlLabel
                control={
                  <Checkbox
                    checked={filtros.soloStockBajo}
                    onChange={(e) => handleFiltroChange('soloStockBajo', e.target.checked)}
                  />
                }
                label="Solo Stock Bajo"
              />
            </Grid>

            {/* Botones */}
            <Grid item xs={12}>
              <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
                <Button
                  variant="outlined"
                  startIcon={<ClearIcon />}
                  onClick={handleLimpiarFiltros}
                >
                  Limpiar Filtros
                </Button>
                <Button
                  variant="contained"
                  startIcon={<SearchIcon />}
                  onClick={handleBuscar}
                >
                  Buscar
                </Button>
              </Box>
            </Grid>
          </Grid>
        </Collapse>
      </Paper>

      {/* Resultados */}
      <Paper sx={{ p: 2 }}>
        <Typography variant="subtitle1" sx={{ mb: 2 }}>
          {totalRows} producto(s) encontrado(s)
        </Typography>
        <Box sx={{ height: 500 }}>
          <DataGrid
            rows={productos}
            columns={columns}
            getRowId={(row) => row.codigoBarras}
            loading={loading}
            paginationModel={paginationModel}
            onPaginationModelChange={setPaginationModel}
            pageSizeOptions={[10, 20, 50, 100]}
            rowCount={totalRows}
            paginationMode="server"
            disableRowSelectionOnClick
            onRowClick={(params) => navigate(`/productos/editar/${params.row.codigoBarras}`)}
            sx={{
              '& .MuiDataGrid-row:hover': {
                cursor: 'pointer',
              },
            }}
            localeText={{
              noRowsLabel: 'No se encontraron productos',
              MuiTablePagination: {
                labelRowsPerPage: 'Filas por página:',
              },
            }}
          />
        </Box>
      </Paper>
    </Box>
  );
};

export default BusquedaAvanzada;

