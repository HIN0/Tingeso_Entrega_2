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
import KardexList from "./components/KardexList";
import ReportViewer from "./components/ReportViewer";
import TariffManager from "./components/TariffManager";
import PrivateRoute from "./components/PrivateRoute"; // Importamos el portero

function App({ keycloak }) {
  
  // Si keycloak no está listo (caso raro por el main.jsx), mostramos cargando
  if (!keycloak) return <div>Cargando...</div>;

  return (
    <BrowserRouter>
      {/* Pasamos keycloak al Header para el botón de Logout */}
      <Header keycloak={keycloak} /> 
      
      <div className="container mt-3">
        <Routes>
          {/* Ruta Pública (o redirigir a /tools) */}
          <Route path="/" element={<Navigate to="/tools" />} />

          {/* --- RUTAS PROTEGIDAS --- */}
          
          {/* Ejemplo: Ver herramientas (Acceso para TODOS los logueados) */}
          <Route path="/tools" element={
            <PrivateRoute keycloak={keycloak}>
              <ToolList />
            </PrivateRoute>
          } />

          {/* Ejemplo: Agregar Herramienta (SOLO ADMIN) */}
          <Route path="/tool/add" element={
            <PrivateRoute keycloak={keycloak} roles={['ADMIN']}>
              <AddTool />
            </PrivateRoute>
          } />

          {/* Ejemplo: Ver Kardex (SOLO ADMIN) */}
          <Route path="/kardex" element={
            <PrivateRoute keycloak={keycloak} roles={['ADMIN']}>
              <KardexList />
            </PrivateRoute>
          } />

          {/* Ejemplo: Préstamos (EMPLEADOS Y ADMIN) */}
          <Route path="/loans/create" element={
            <PrivateRoute keycloak={keycloak} roles={['ADMIN', 'EMPLOYEE']}>
              <CreateLoan />
            </PrivateRoute>
          } />

           {/* Rutas restantes... protégelas según tu lógica */}
          <Route path="/clients" element={<PrivateRoute keycloak={keycloak}><ClientList /></PrivateRoute>} />
          <Route path="/clients/add" element={<PrivateRoute keycloak={keycloak}><AddClient /></PrivateRoute>} />
          <Route path="/reports" element={<PrivateRoute keycloak={keycloak}><ReportViewer /></PrivateRoute>} />
          <Route path="/tariffs" element={<PrivateRoute keycloak={keycloak}><TariffManager /></PrivateRoute>} />

        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;