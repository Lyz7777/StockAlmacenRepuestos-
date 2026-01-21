import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import Productos from './pages/Productos';
import ProductoForm from './pages/ProductoForm';
import Ventas from './pages/Ventas';
import NuevaVenta from './pages/NuevaVenta';
import Categorias from './pages/Categorias';
import Proveedores from './pages/Proveedores';
import Movimientos from './pages/Movimientos';
import OrdenesCompra from './pages/OrdenesCompra';
import Reportes from './pages/Reportes';
import BusquedaAvanzada from './pages/BusquedaAvanzada';

function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/productos" element={<Productos />} />
        <Route path="/productos/nuevo" element={<ProductoForm />} />
        <Route path="/productos/editar/:codigoBarras" element={<ProductoForm />} />
        <Route path="/productos/busqueda" element={<BusquedaAvanzada />} />
        <Route path="/ventas" element={<Ventas />} />
        <Route path="/ventas/nueva" element={<NuevaVenta />} />
        <Route path="/categorias" element={<Categorias />} />
        <Route path="/proveedores" element={<Proveedores />} />
        <Route path="/movimientos" element={<Movimientos />} />
        <Route path="/ordenes-compra" element={<OrdenesCompra />} />
        <Route path="/reportes" element={<Reportes />} />
      </Routes>
    </Layout>
  );
}

export default App;

