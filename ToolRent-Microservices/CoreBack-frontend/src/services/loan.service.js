import http from "../http-common";

class LoanService {
  getAll() {
    // Ajustado para coincidir con el backend (devuelve activos + atrasados)
    return http.get("api/loans");
  }

  get(id) {
    return http.get(`api/loans/${id}`);
  }

  create(data) {
    return http.post("api/loans", data);
  }

  returnLoan(id, data) {
    return http.put(`api/loans/${id}/return`, data);
  }

  markAsPaid(loanId) {
    return http.patch(`api/loans/${loanId}/pay`);
  }

  getUnpaidLoansByClient(clientId) {
    return http.get(`api/loans/client/${clientId}/unpaid`);
  }

  getActiveLoans() {
    return http.get("api/loans/active");
  }

  getLateLoans() {
      return http.get("api/loans/late");
  }

  getUnpaidClosedLoans() {
      return http.get("api/loans/closed/unpaid");
  }
}

export default new LoanService();