import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import AuthService from "../services/auth.service";

const Login = () => {
const [username, setUsername] = useState("");
const [password, setPassword] = useState("");
const navigate = useNavigate();

const handleLogin = async (e) => {
    e.preventDefault();
    try {
      // 1. Llamar al Microservicio M7
    const response = await AuthService.login({ username, password });
    
      // 2. Guardar el token recibido
    AuthService.setToken(response.data.token);

      // 3. Redirigir al Home
    alert("Login exitoso");
    navigate("/home"); // O la ruta que uses
    window.location.reload(); // Para recargar el interceptor con el nuevo token
    } catch (error) {
    console.error("Error:", error);
    alert("Credenciales incorrectas");
    }
};

return (
<div className="container">
    <h2>Iniciar Sesión (Microservicio M7)</h2>
    <form onSubmit={handleLogin}>
        <input 
            type="text" 
            placeholder="Usuario" 
            value={username}
            onChange={(e) => setUsername(e.target.value)} 
        />
        <input 
            type="password" 
            placeholder="Contraseña" 
            value={password}
            onChange={(e) => setPassword(e.target.value)} 
        />
        <button type="submit">Entrar</button>
    </form>
</div>
);
};

export default Login;