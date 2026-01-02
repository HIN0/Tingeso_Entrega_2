import axios from "axios";
import keycloak from "./keycloak"; // IMPORTANTE: Importamos la instancia configurada

const instance = axios.create({
  baseURL: "http://localhost:8080", // Apunta a tu Gateway
  headers: {
    "Content-Type": "application/json"
  }
});

instance.interceptors.request.use(
  (config) => {
    // Si Keycloak tiene un token válido, lo agregamos
    if (keycloak.token) {
      config.headers["Authorization"] = 'Bearer ' + keycloak.token;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default instance;