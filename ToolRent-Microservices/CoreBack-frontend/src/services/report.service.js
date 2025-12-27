import httpClient from "../http-common";

const getSummary = () => {
    return httpClient.get("/reports/summary");
}

const getActiveLoans = () => {
    return httpClient.get("/reports/active-loans");
}

const getDelinquentClients = () => {
    return httpClient.get("/reports/delinquent-clients");
}

const getToolRanking = () => {
    return httpClient.get("/reports/tool-ranking");
}

export default { getSummary, getActiveLoans, getDelinquentClients, getToolRanking };