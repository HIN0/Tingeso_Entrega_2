import httpClient from "../http-common";

const getAll = () => {
return httpClient.get("api/tools");
};

const get = (id) => {
return httpClient.get(`/api/tools/${id}`);
};

const create = (data) => {
return httpClient.post("/api/tools", data);
};

const update = (id, data) => {
return httpClient.put(`/api/tools/${id}`, data);
};

const remove = (id) => {
return httpClient.delete(`/api/tools/${id}`);
};

// Para actualizar el estado de la herramienta (AVAILABLE, IN_REPAIR, DECOMMISSIONED)
const updateStatus = (id, newStatus) => {
    return httpClient.put(`/api/tools/${id}/status`, null, { params: { newStatus } });
};

// Para sumar/restar stock
const updateStock = (id, quantity) => {
return httpClient.put(`/api/tools/${id}/stock`, null, { params: { quantity } });
};

export default {
getAll,
get,
create,
update,
remove,
updateStock,
updateStatus
};