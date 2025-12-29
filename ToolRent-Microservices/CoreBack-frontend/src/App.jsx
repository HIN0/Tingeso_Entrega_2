import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Header from "./components/Header";
import Login from "./components/Login";
import ToolList from "./components/ToolList";
import CreateLoan from "./components/CreateLoan";
import ActiveLoans from "./components/ActiveLoans"; 
import AddTool from "./components/AddTool";

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

            {/* Nuevas Rutas */}
            <Route path="/loans" element={<ActiveLoans />} />
            <Route path="/loans/add" element={<CreateLoan />} />
          </Routes>
        </div>
      </div>
    </Router>
  );
}

export default App;