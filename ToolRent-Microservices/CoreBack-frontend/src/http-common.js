import axios from "axios";

const instance = axios.create({
  baseURL: "http://localhost:8080/",
  headers: {
    "Content-Type": "application/json"
  }
});

instance.interceptors.request.use(
  (config) => {
    const user = JSON.parse(localStorage.getItem("user"));
    if (user && user.token) {
      config.headers["Authorization"] = 'Bearer ' + user.token;
      config.headers["X-User-Name"] = "admin"; 
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default instance;