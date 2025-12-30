import httpClient from "../http-common";

const getAll = (params) => {
    return httpClient.get("/api/kardex", { params });
};

export default { getAll };