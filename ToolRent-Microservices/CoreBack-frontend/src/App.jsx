import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Header from "./components/Header";
import Login from "./components/Login";
import ToolList from "./components/ToolList";
import "bootstrap/dist/css/bootstrap.min.css";

function App() {
  return (
    <Router>
      <div className="container-fluid p-0">
        <Header />
        <div className="container mt-4">
          <Routes>
            {/* Ruta por defecto: Login */}
            <Route path="/" element={<Login />} />
            <Route path="/login" element={<Login />} />

            <Route path="/tools" element={<ToolList />} />
          </Routes>
        </div>
      </div>
    </Router>
  );
}

export default App;