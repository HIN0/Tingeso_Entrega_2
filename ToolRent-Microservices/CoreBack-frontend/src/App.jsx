import { BrowserRouter, Routes, Route, useLocation } from "react-router-dom";
import Login from "./components/Login";
import Header from "./components/Header"; // Tu barra de navegación
import ToolList from "./components/ToolList";
import AddTool from "./components/AddTool";
import EditTool from "./components/EditTool";
import ClientList from "./components/ClientList";
import AddClient from "./components/AddClient";
import EditClient from "./components/EditClient";
import LoanList from "./components/LoanList";
import AddLoan from "./components/AddLoan";
import ReturnLoan from "./components/ReturnLoan";
import KardexViewer from "./components/KardexViewer";
import ReportViewer from "./components/ReportViewer";
import TariffManager from "./components/TariffManager";

function AppContent() {
  const location = useLocation();
  // Ocultar Header solo en la ruta raíz ("/") o "/login"
  const hideHeader = location.pathname === "/" || location.pathname === "/login";

  return (
    <>
      {!hideHeader && <Header />}
      <div className="container mt-3">
        <Routes>
          {/* Ruta por defecto: Login */}
          <Route path="/" element={<Login />} />
          <Route path="/login" element={<Login />} />
          
          {/* Rutas protegidas (Home y demás) */}
          <Route path="/home" element={<ToolList />} />
          <Route path="/tools/add" element={<AddTool />} />
          <Route path="/tools/edit/:id" element={<EditTool />} />
          
          <Route path="/clients" element={<ClientList />} />
          <Route path="/clients/add" element={<AddClient />} />
          <Route path="/clients/edit/:id" element={<EditClient />} />
          
          <Route path="/loans" element={<LoanList />} />
          <Route path="/loans/add" element={<AddLoan />} />
          <Route path="/loans/return/:id" element={<ReturnLoan />} />
          
          <Route path="/kardex" element={<KardexViewer />} />
          <Route path="/reports" element={<ReportViewer />} />
          <Route path="/tariffs" element={<TariffManager />} />
        </Routes>
      </div>
    </>
  );
}

function App() {
  return (
    <BrowserRouter>
      <AppContent />
    </BrowserRouter>
  );
}

export default App;