import httpClient from "../http-common";

const login = (data) => {
    return httpClient.post("/auth/login", data);
}

const register = (data) => {
    return httpClient.post("/auth/register", data);
}

const validate = (token) => {
    return httpClient.get(`/auth/validate?token=${token}`);
}

// Método auxiliar para guardar el token en el navegador
const setToken = (token) => {
    localStorage.setItem("token", token);
}

// Método auxiliar para obtener el token
const getToken = () => {
    return localStorage.getItem("token");
}

// Método para cerrar sesión
const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
}

export default { login, register, validate, setToken, getToken, logout };