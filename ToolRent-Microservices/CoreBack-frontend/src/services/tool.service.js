import http from "../http-common";

class ToolService {
  // --- METODO GET ALL ---
  getAll() {
    return http.get("api/tools");
  }

  // --- MÉTODO GET ONE ---
  get(id) {
    return http.get(`api/tools/${id}`);
  }

  // --- MÉTODO CREATE ---
  create(data) {
    return http.post("api/tools", data);
  }

  // --- MÉTODO UPDATE ---
  update(id, data) {
    // data debe contener name, category, replacementValue
    return http.put(`api/tools/${id}`, data);
  }

  // --- MÉTODO ADJUST STOCK ---
  adjustStock(id, data) {
      // data debe ser { quantityChange: number }
      return http.patch(`api/tools/${id}/stock`, data);
  }

  // --- MÉTODO DECOMMISSION ---
  decommission(id) {
    return http.put(`api/tools/${id}/decommission`);
  }
}

export default new ToolService();