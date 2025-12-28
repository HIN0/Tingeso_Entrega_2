import httpClient from "../http-common";

const login = (username, password) => {
return httpClient.post("/auth/login", { username, password })
    .then(response => {
    if (response.data.token) {
        // Guardamos el usuario y token en el navegador
        localStorage.setItem("user", JSON.stringify(response.data));
    }
    return response.data;
    });
};

const logout = () => {
localStorage.removeItem("user");
};

const getCurrentUser = () => {
return JSON.parse(localStorage.getItem("user"));
};

export default {
login,
logout,
getCurrentUser
};