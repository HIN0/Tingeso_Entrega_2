import { useEffect, useState } from "react";
import LoanService from "../services/loan.service";
import { Link } from "react-router-dom";

function LoanList() {
  const [loans, setLoans] = useState([]);
  
  // --- AUTENTICACIÓN JWT ---
  const token = localStorage.getItem("token");
  const isAuth = !!token;
  let isAdmin = false;
  let isUser = false; // Empleado

  if (isAuth) {
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const role = payload.role;
        isAdmin = role === "ADMIN";
        isUser = role === "USER" || role === "EMPLEADO";
    } catch (e) { console.error(e); }
  }
  // -------------------------

  const loadLoans = () => {
    LoanService.getAll()
      .then(response => {
        if (Array.isArray(response.data)) {
          setLoans(response.data);
        } else {
          setLoans([]); 
        }
      })
      .catch(e => {
          console.error("Error fetching loans:", e);
          setLoans([]);
      });
  };

  useEffect(() => {
    if(isAuth) {
      loadLoans();
    }
  }, [isAuth]);

  return (
    <div style={{ padding: 16 }}>
      <h2>Loans</h2>
      {(isAdmin || isUser) && <Link to="/loans/add" style={{ marginBottom: '15px', display: 'inline-block' }}>➕ Add Loan</Link>}
      
      <table border="1" style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr>
            <th>ID</th><th>Client</th><th>Tool</th><th>Start</th><th>Due</th><th>Status</th><th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {Array.isArray(loans) && loans.map(loan => (
            <tr key={loan.id}>
              <td>{loan.id}</td>
              <td>{loan.client?.name}</td>
              <td>{loan.tool?.name}</td>
              <td>{loan.startDate}</td>
              <td>{loan.dueDate}</td>
              <td style={{ fontWeight: 'bold', color: loan.status === 'LATE' ? 'red' : 'inherit' }}>
                {loan.status}
              </td>
              <td>
                {(isAdmin || isUser) && (loan.status === "ACTIVE" || loan.status === "LATE") && (
                  <Link to={`/loans/return/${loan.id}`}>Return</Link>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {(!Array.isArray(loans) || loans.length === 0) && (
          <p style={{ marginTop: '15px' }}>No loans found.</p>
      )}
    </div>
  );
}

export default LoanList;