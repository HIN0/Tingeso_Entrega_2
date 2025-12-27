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
    // 1. Extraemos el valor del objeto que envía React
      const quantity = data.quantityChange; 
  
      // 2. Usamos 'http' (NO httpClient)
      // 3. Usamos .put (Tu backend es @PutMapping)
      // 4. Enviamos como 'params' para que la URL sea: .../stock?quantity=5
      return http.put(`api/tools/${id}/stock`, null, {
          params: { 
              quantity: quantity 
          }
      });

  }

  // --- MÉTODO DECOMMISSION ---
  decommission(id) {
    return http.put(`api/tools/${id}/decommission`);
  }
}

export default new ToolService();