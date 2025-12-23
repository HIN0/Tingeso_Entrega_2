import { BrowserRouter as Router, Routes, Route, Navigate, Link } from "react-router-dom";
import Login from "./components/Login";
import Header from "./components/Header";
import ToolList from "./components/ToolList";
import AddTool from "./components/AddTool";
import LoanList from "./components/LoanList";
import AddLoan from "./components/AddLoan";
import ReturnLoan from "./components/ReturnLoan";
import ClientList from "./components/ClientList";
import TariffManager from "./components/TariffManager";
import ReportViewer from "./components/ReportViewer";
import EditTool from "./components/EditTool";
import AddClient from "./components/AddClient"; 
import EditClient from "./components/EditClient";
import KardexViewer from "./components/KardexViewer";


export default function App() {
  return (
    <Router>
      <Header />
      <Routes>
        <Route path="/" element={
          <div style={{ padding: 16 }}>
            <h2>Bienvenido a ToolRent</h2>
          </div>
        } />
        {/* --- Rutas de Herramientas --- */}
        <Route path="/tools" element={<RequireAuth><ToolList /></RequireAuth>} />
        <Route path="/tools/add" element={<RequireAuth roles={["ADMIN"]}><AddTool /></RequireAuth>} />
        <Route path="/tools/edit/:id" element={<RequireAuth roles={["ADMIN"]}><EditTool /></RequireAuth>} />

        {/* --- Rutas de Préstamos --- */}
        <Route path="/loans" element={<RequireAuth><LoanList /></RequireAuth>} />
        <Route path="/loans/add" element={<RequireAuth roles={["ADMIN","USER"]}><AddLoan /></RequireAuth>} />
        <Route path="/loans/return/:id" element={<RequireAuth roles={["ADMIN", "USER"]}><ReturnLoan /></RequireAuth>} />

        {/* --- Rutas de Clientes --- */}
        <Route path="/clients" element={<RequireAuth roles={["ADMIN"]}><ClientList /></RequireAuth>} />
        <Route path="/clients/add" element={<RequireAuth roles={["ADMIN"]}><AddClient /></RequireAuth>} />
        <Route path="/clients/edit/:id" element={<RequireAuth roles={["ADMIN"]}><EditClient /></RequireAuth>} />
        
        {/* --- Rutas de Tarifas, Reportes y Kardex--- */}
        <Route path="/tariffs" element={<RequireAuth roles={["ADMIN"]}><TariffManager /></RequireAuth>} />
        <Route path="/reports" element={<RequireAuth roles={["USER","ADMIN"]}><ReportViewer /></RequireAuth>} />
        <Route path="/kardex" element={<RequireAuth roles={["ADMIN", "USER"]}><KardexViewer /></RequireAuth>} />

        {/* fallback */}
        <Route path="*" element={
            <div style={{ padding: 16 }}>
                <h2>Página no encontrada</h2>
                <Link to="/">Volver al inicio</Link>
            </div>
        } />
      </Routes>
    </Router>
  );
}
