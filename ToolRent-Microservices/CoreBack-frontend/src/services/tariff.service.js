import httpClient from "../http-common";

const getTariffs = () => {
    return httpClient.get("/api/tariffs");
};

const saveTariffs = (data) => {
    return httpClient.post("/api/tariffs", data);
};

export default { getTariffs, saveTariffs };