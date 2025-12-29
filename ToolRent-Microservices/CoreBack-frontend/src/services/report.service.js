import httpClient from "../http-common";

const getSummary = () => {
    return httpClient.get("/api/reports/summary");
};

const getActiveLoans = () => {
    return httpClient.get("/api/reports/active-loans");
};

const getDelinquentClients = () => {
    return httpClient.get("/api/reports/delinquent-clients"); 
};

const getToolRanking = () => {
    return httpClient.get("/api/reports/tool-ranking");
};

export default { 
    getSummary, 
    getActiveLoans, 
    getDelinquentClients, 
    getToolRanking 
};