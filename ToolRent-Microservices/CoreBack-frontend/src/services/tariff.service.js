import http from "../http-common";

class TariffService {
  getTariff() {
    return http.get("api/tariffs");
  }

  // Actualiza TODAS las tarifas con el objeto de entrada
  updateTariff(data) {
    return http.put("api/tariffs", data);
  }
}

export default new TariffService();