import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap-icons/font/bootstrap-icons.css';
import './App.css';

// Componentes
import Header from "./components/Header";
import ToolList from "./components/ToolList";
import AddTool from "./components/AddTool";
import ClientList from "./components/ClientList";
import AddClient from "./components/AddClient";
import CreateLoan from "./components/CreateLoan";
import ActiveLoans from "./components/ActiveLoans";
import KardexList from "./components/KardexList";
import ReportViewer from "./components/ReportViewer";
import TariffManager from "./components/TariffManager";
import PrivateRoute from "./components/PrivateRoute";

function App({ keycloak }) {
  
  if (!keycloak) return <div>Cargando...</div>;

  return (
    <BrowserRouter>
      {/* Keycloak al Header para el botón de Logout */}
      <Header keycloak={keycloak} /> 
      
      <div className="container mt-3">
        <Routes>
          {/* Ruta Pública (o redirigir a /tools) */}
          <Route path="/" element={<Navigate to="/tools" />} />

          {/* --- Rutas de Herramientas --- */}
          <Route path="/tools" element={<PrivateRoute keycloak={keycloak}><ToolList /></PrivateRoute>} />
          <Route path="/tools/add" element={<PrivateRoute keycloak={keycloak} roles={['ADMIN']}><AddTool /></PrivateRoute>} />

          {/* --- Rutas de Préstamos --- */}
          <Route path="/loans" element={<PrivateRoute keycloak={keycloak} roles={['ADMIN', 'EMPLOYEE']}><ActiveLoans /></PrivateRoute>} />
          <Route path="/loans/add" element={<PrivateRoute keycloak={keycloak} roles={['ADMIN', 'EMPLOYEE']}><CreateLoan /></PrivateRoute>} />

          {/* --- Rutas de Clientes --- */}
          <Route path="/clients" element={<PrivateRoute keycloak={keycloak} roles={['ADMIN', 'EMPLOYEE']}><ClientList /></PrivateRoute>} />
          <Route path="/clients/add" element={<PrivateRoute keycloak={keycloak} roles={['ADMIN']}><AddClient /></PrivateRoute>} />

          {/* --- Rutas de Tarifas, Reportes y Kardex --- */}
          <Route path="/tariffs" element={<PrivateRoute keycloak={keycloak} roles={['ADMIN']}><TariffManager /></PrivateRoute>} />
          <Route path="/reports" element={<PrivateRoute keycloak={keycloak} roles={['ADMIN', 'EMPLOYEE']}><ReportViewer /></PrivateRoute>} />
          <Route path="/kardex" element={<PrivateRoute keycloak={keycloak} roles={['ADMIN', 'EMPLOYEE']}><KardexList /></PrivateRoute>} />

        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;