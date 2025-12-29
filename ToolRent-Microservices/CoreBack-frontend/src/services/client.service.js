import httpClient from "../http-common";

const getAll = () => {
    return httpClient.get("/api/clients");
};

const create = (data) => {
    return httpClient.post("/api/clients", data);
};

const update = (id, data) => {
    return httpClient.put(`/api/clients/${id}`, data);
};

// NUEVO: Pagar deuda
const payDebt = (id, amount) => {
    return httpClient.put(`/api/clients/${id}/pay`, null, { params: { amount } });
};

// NUEVO: Cambiar estado manual
const changeStatus = (id, newStatus) => {
    return httpClient.put(`/api/clients/${id}/status`, null, { params: { newStatus } });
};

export default { getAll, create, update, payDebt, changeStatus };