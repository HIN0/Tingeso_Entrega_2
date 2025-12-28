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

// Endpoint específico para sumar/restar stock (si lo tienes implementado)
const updateStock = (id, quantity) => {
return httpClient.put(`/api/tools/${id}/stock`, null, { params: { quantity } });
};

export default {
getAll,
get,
create,
update,
remove,
updateStock
};