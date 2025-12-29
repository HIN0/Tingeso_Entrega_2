import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Header from "./components/Header";
import Login from "./components/Login";
import ToolList from "./components/ToolList";
import CreateLoan from "./components/CreateLoan";
import ActiveLoans from "./components/ActiveLoans"; 
import AddTool from "./components/AddTool";
import ClientList from "./components/ClientList";
import AddClient from "./components/AddClient";
import TariffManager from "./components/TariffManager";
import ReportViewer from "./components/ReportViewer";

import "bootstrap/dist/css/bootstrap.min.css";

function App() {
  return (
    <Router>
      <div className="container-fluid p-0">
        <Header />
        <div className="container mt-4">
          <Routes>
            <Route path="/" element={<Login />} />
            <Route path="/login" element={<Login />} />

            <Route path="/tools" element={<ToolList />} />
            <Route path="/tools/add" element={<AddTool />} />

            <Route path="/loans" element={<ActiveLoans />} />
            <Route path="/loans/add" element={<CreateLoan />} />

            <Route path="/clients" element={<ClientList />} />
            <Route path="/clients/add" element={<AddClient />} />

            <Route path="/tariffs" element={<TariffManager />} />

            <Route path="/reports" element={<ReportViewer />} />
          </Routes>
        </div>
      </div>
    </Router>
  );
}

export default App;