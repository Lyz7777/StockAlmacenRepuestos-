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
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  Divider,
  CircularProgress,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
} from '@mui/icons-material';
import toast from 'react-hot-toast';
import { categoriaService } from '../services/api';

const Categorias = () => {
  const [categorias, setCategorias] = useState([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editando, setEditando] = useState(null);
  const [formData, setFormData] = useState({ nombre: '', descripcion: '' });
  const [saving, setSaving] = useState(false);

  const fetchCategorias = async () => {
    try {
      setLoading(true);
      const response = await categoriaService.listar();
      setCategorias(response.data);
    } catch (error) {
      toast.error('Error al cargar categorías');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCategorias();
  }, []);

  const handleOpenDialog = (categoria = null) => {
    if (categoria) {
      setEditando(categoria);
      setFormData({ nombre: categoria.nombre, descripcion: categoria.descripcion || '' });
    } else {
      setEditando(null);
      setFormData({ nombre: '', descripcion: '' });
    }
    setDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
    setEditando(null);
    setFormData({ nombre: '', descripcion: '' });
  };

  const handleGuardar = async () => {
    if (!formData.nombre.trim()) {
      toast.error('El nombre es obligatorio');
      return;
    }

    try {
      setSaving(true);
      if (editando) {
        await categoriaService.actualizar(editando.id, formData);
        toast.success('Categoría actualizada');
      } else {
        await categoriaService.crear(formData);
        toast.success('Categoría creada');
      }
      handleCloseDialog();
      fetchCategorias();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Error al guardar');
    } finally {
      setSaving(false);
    }
  };

  const handleEliminar = async (id, nombre) => {
    if (window.confirm(`¿Está seguro de eliminar la categoría "${nombre}"?`)) {
      try {
        await categoriaService.eliminar(id);
        toast.success('Categoría eliminada');
        fetchCategorias();
      } catch (error) {
        toast.error('Error al eliminar categoría');
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
          Categorías
        </Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpenDialog()}>
          Nueva Categoría
        </Button>
      </Box>

      <Paper>
        <List>
          {categorias.length === 0 ? (
            <ListItem>
              <ListItemText primary="No hay categorías registradas" />
            </ListItem>
          ) : (
            categorias.map((categoria, index) => (
              <React.Fragment key={categoria.id}>
                {index > 0 && <Divider />}
                <ListItem>
                  <ListItemText
                    primary={categoria.nombre}
                    secondary={
                      <>
                        {categoria.descripcion && <span>{categoria.descripcion}<br /></span>}
                        <span>{categoria.cantidadProductos || 0} productos</span>
                      </>
                    }
                  />
                  <ListItemSecondaryAction>
                    <IconButton onClick={() => handleOpenDialog(categoria)}>
                      <EditIcon />
                    </IconButton>
                    <IconButton color="error" onClick={() => handleEliminar(categoria.id, categoria.nombre)}>
                      <DeleteIcon />
                    </IconButton>
                  </ListItemSecondaryAction>
                </ListItem>
              </React.Fragment>
            ))
          )}
        </List>
      </Paper>

      {/* Diálogo de creación/edición */}
      <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>{editando ? 'Editar Categoría' : 'Nueva Categoría'}</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            fullWidth
            label="Nombre"
            value={formData.nombre}
            onChange={(e) => setFormData({ ...formData, nombre: e.target.value })}
            margin="normal"
            required
          />
          <TextField
            fullWidth
            multiline
            rows={3}
            label="Descripción"
            value={formData.descripcion}
            onChange={(e) => setFormData({ ...formData, descripcion: e.target.value })}
            margin="normal"
          />
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

export default Categorias;

