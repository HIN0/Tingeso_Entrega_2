import httpClient from "../http-common";

const getAll = () => {
    return httpClient.get("/api/clients");
};

export default { getAll };