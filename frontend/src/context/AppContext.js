import React, { createContext, useContext, useState, useCallback } from 'react';
import { reporteService, productoService } from '../services/api';

const AppContext = createContext();

export const useApp = () => {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useApp debe usarse dentro de AppProvider');
  }
  return context;
};

export const AppProvider = ({ children }) => {
  const [dashboard, setDashboard] = useState(null);
  const [productosStockBajo, setProductosStockBajo] = useState([]);
  const [loading, setLoading] = useState(false);
  const [alertas, setAlertas] = useState([]);

  const cargarDashboard = useCallback(async () => {
    try {
      setLoading(true);
      const response = await reporteService.dashboard();
      setDashboard(response.data);

      // Actualizar alertas de stock bajo
      if (response.data.productosStockCritico) {
        setProductosStockBajo(response.data.productosStockCritico);
        setAlertas(response.data.productosStockCritico.map(p => ({
          id: p.codigoBarras,
          tipo: p.stockActual === 0 ? 'error' : 'warning',
          mensaje: p.stockActual === 0
            ? `${p.nombre} está AGOTADO`
            : `${p.nombre} tiene stock bajo (${p.stockActual} unidades)`,
          producto: p
        })));
      }
    } catch (error) {
      console.error('Error al cargar dashboard:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  const cargarProductosStockBajo = useCallback(async () => {
    try {
      const response = await productoService.stockBajo();
      setProductosStockBajo(response.data);
      return response.data;
    } catch (error) {
      console.error('Error al cargar productos con stock bajo:', error);
      return [];
    }
  }, []);

  const dismissAlerta = useCallback((id) => {
    setAlertas(prev => prev.filter(a => a.id !== id));
  }, []);

  const value = {
    dashboard,
    productosStockBajo,
    loading,
    alertas,
    cargarDashboard,
    cargarProductosStockBajo,
    dismissAlerta,
  };

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
};

export default AppContext;

