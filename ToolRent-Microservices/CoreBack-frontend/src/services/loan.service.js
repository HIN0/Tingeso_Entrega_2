import httpClient from "../http-common";

const getAll = () => {
    return httpClient.get("/api/loans");
};

const getActive = () => {
    return httpClient.get("/api/loans/active"); // Usamos el endpoint optimizado que creamos
};

const create = (clientId, toolId, deadline) => {
    // Enviamos como x-www-form-urlencoded o params según tu Controller
    const params = new URLSearchParams();
    params.append('clientId', clientId);
    params.append('toolId', toolId);
    if (deadline) params.append('deadline', deadline);

    return httpClient.post("/api/loans", null, { params });
};

const returnLoan = (id, damaged, irreparable) => {
    const data = {
        damaged: damaged,
        irreparable: irreparable
    };
    return httpClient.put(`/api/loans/${id}/return`, data);
};

export default { getAll, getActive, create, returnLoan };