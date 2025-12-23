import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import AuthService from "../services/auth.service";

const Login = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      // 1. Llamar al backend (Gateway -> Auth Service)
      const response = await AuthService.login({ username, password });
      
      // 2. Guardar token
      if (response.data.token) {
        AuthService.setToken(response.data.token);
        // 3. Redirigir al Home
        navigate("/home");
      }
    } catch (err) {
      console.error(err);
      setError("Credenciales incorrectas o error de conexión.");
    }
  };

  return (
    <div className="login-container" style={{ maxWidth: "400px", margin: "100px auto", padding: "20px", border: "1px solid #ddd", borderRadius: "8px" }}>
      <h2 className="text-center">Iniciar Sesión</h2>
      <p className="text-center text-muted">ToolRent v2.0</p>
      
      {error && <div className="alert alert-danger">{error}</div>}
      
      <form onSubmit={handleLogin}>
        <div className="form-group mb-3">
          <label>Usuario</label>
          <input 
            type="text" 
            className="form-control"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required 
          />
        </div>
        <div className="form-group mb-3">
          <label>Contraseña</label>
          <input 
            type="password" 
            className="form-control"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required 
          />
        </div>
        <button type="submit" className="btn btn-primary w-100">Entrar</button>
      </form>
    </div>
  );
};

export default Login;