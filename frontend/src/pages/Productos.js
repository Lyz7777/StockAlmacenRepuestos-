import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Paper,
  Typography,
  Button,
  TextField,
  InputAdornment,
  IconButton,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  CircularProgress,
  Alert,
} from '@mui/material';
import { DataGrid } from '@mui/x-data-grid';
import {
  Add as AddIcon,
  Search as SearchIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Visibility as ViewIcon,
  Warning as WarningIcon,
} from '@mui/icons-material';
import toast from 'react-hot-toast';
import { productoService } from '../services/api';

const Productos = () => {
  const navigate = useNavigate();
  const [productos, setProductos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchText, setSearchText] = useState('');
  const [paginationModel, setPaginationModel] = useState({ page: 0, pageSize: 20 });
  const [totalRows, setTotalRows] = useState(0);
  const [deleteDialog, setDeleteDialog] = useState({ open: false, producto: null });

  const fetchProductos = async () => {
    try {
      setLoading(true);
      const response = await productoService.listar(
        paginationModel.page,
        paginationModel.pageSize,
        'nombre',
        'ASC'
      );
      setProductos(response.data.content);
      setTotalRows(response.data.totalElements);
    } catch (error) {
      toast.error('Error al cargar productos');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProductos();
  }, [paginationModel]);

  const handleSearch = async () => {
    if (!searchText.trim()) {
      fetchProductos();
      return;
    }
    try {
      setLoading(true);
      const response = await productoService.busquedaGeneral(searchText);
      setProductos(response.data);
      setTotalRows(response.data.length);
    } catch (error) {
      toast.error('Error en la búsqueda');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    try {
      await productoService.eliminar(deleteDialog.producto.codigoBarras);
      toast.success('Producto eliminado correctamente');
      setDeleteDialog({ open: false, producto: null });
      fetchProductos();
    } catch (error) {
      toast.error('Error al eliminar producto');
    }
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
      field: 'categoriaNombre',
      headerName: 'Categoría',
      width: 130,
    },
    {
      field: 'marca',
      headerName: 'Marca',
      width: 120,
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
      width: 100,
      renderCell: (params) => {
        const row = params.row;
        let color = 'success';
        if (row.stockActual === 0) color = 'error';
        else if (row.stockActual <= row.stockMinimo) color = 'warning';

        return (
          <Chip
            label={params.value}
            size="small"
            color={color}
            icon={row.stockActual <= row.stockMinimo ? <WarningIcon /> : null}
          />
        );
      }
    },
    {
      field: 'stockMinimo',
      headerName: 'Mínimo',
      width: 80,
    },
    {
      field: 'proveedorNombre',
      headerName: 'Proveedor',
      width: 150,
    },
    {
      field: 'acciones',
      headerName: 'Acciones',
      width: 150,
      sortable: false,
      renderCell: (params) => (
        <Box>
          <IconButton
            size="small"
            color="primary"
            onClick={() => navigate(`/productos/editar/${params.row.codigoBarras}`)}
          >
            <EditIcon />
          </IconButton>
          <IconButton
            size="small"
            color="error"
            onClick={() => setDeleteDialog({ open: true, producto: params.row })}
          >
            <DeleteIcon />
          </IconButton>
        </Box>
      ),
    },
  ];

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" sx={{ fontWeight: 'bold' }}>
          Productos
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => navigate('/productos/nuevo')}
        >
          Nuevo Producto
        </Button>
      </Box>

      <Paper sx={{ p: 2, mb: 2 }}>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <TextField
            size="small"
            placeholder="Buscar por nombre, código, marca..."
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
            sx={{ flexGrow: 1 }}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              ),
            }}
          />
          <Button variant="contained" onClick={handleSearch}>
            Buscar
          </Button>
          <Button
            variant="outlined"
            onClick={() => navigate('/productos/busqueda')}
          >
            Búsqueda Avanzada
          </Button>
        </Box>
      </Paper>

      <Paper sx={{ height: 600 }}>
        <DataGrid
          rows={productos}
          columns={columns}
          getRowId={(row) => row.codigoBarras}
          loading={loading}
          paginationModel={paginationModel}
          onPaginationModelChange={setPaginationModel}
          pageSizeOptions={[10, 20, 50]}
          rowCount={totalRows}
          paginationMode="server"
          disableRowSelectionOnClick
          localeText={{
            noRowsLabel: 'No hay productos',
            MuiTablePagination: {
              labelRowsPerPage: 'Filas por página:',
            },
          }}
        />
      </Paper>

      {/* Diálogo de confirmación de eliminación */}
      <Dialog open={deleteDialog.open} onClose={() => setDeleteDialog({ open: false, producto: null })}>
        <DialogTitle>Confirmar Eliminación</DialogTitle>
        <DialogContent>
          <Typography>
            ¿Está seguro de eliminar el producto "{deleteDialog.producto?.nombre}"?
          </Typography>
          <Typography variant="body2" color="textSecondary" sx={{ mt: 1 }}>
            Esta acción no se puede deshacer.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialog({ open: false, producto: null })}>
            Cancelar
          </Button>
          <Button onClick={handleDelete} color="error" variant="contained">
            Eliminar
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Productos;

