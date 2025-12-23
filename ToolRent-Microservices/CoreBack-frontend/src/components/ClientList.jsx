import React, { useEffect, useState } from "react";
import ClientService from "../services/client.service";
import LoanService from "../services/loan.service";
import { Link } from "react-router-dom";

// Componente simple para mostrar los detalles de las deudas
function DebtDetails({ clientId, onPay, messageSetter }) {
  const [debts, setDebts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    setLoading(true);
    setErrorMessage("");
    LoanService.getUnpaidLoansByClient(clientId)
      .then(response => {
        setDebts(response.data);
        if (response.data.length === 0) {
          setErrorMessage("No hay deudas pendientes (estado RECEIVED) para este cliente.");
        }
      })
      .catch(e => {
        console.error("Error fetching unpaid loans:", e);
        setErrorMessage(`Error al cargar deudas: ${e.response?.data?.message || e.message}`);
      })
      .finally(() => setLoading(false));
  }, [clientId]);

  const handlePay = (loanId) => {
    setErrorMessage("");
    if (!window.confirm(`¿Marcar la deuda del préstamo #${loanId} como pagada?`)) {
        return;
    }
    messageSetter(`Procesando pago para préstamo ${loanId}...`);
    LoanService.markAsPaid(loanId)
      .then(() => {
        messageSetter(`Pago registrado para préstamo ${loanId}. Actualizando lista...`);
        setLoading(true);
        LoanService.getUnpaidLoansByClient(clientId)
          .then(response => setDebts(response.data))
          .catch(e => setErrorMessage(`Error al recargar deudas: ${e.response?.data?.message || e.message}`))
          .finally(() => setLoading(false));
      })
      .catch(e => {
        console.error("Error marking loan as paid:", e);
        setErrorMessage(`Error al marcar pago: ${e.response?.data?.message || e.message}`);
        messageSetter("");
      });
  };

  if (loading) {
    return <td colSpan="7" style={{ textAlign: 'center', padding: '10px' }}>Cargando deudas...</td>;
  }

  return (
    <td colSpan="7" style={{ padding: '10px', backgroundColor: '#f0f0f0', borderTop: '2px solid grey' }}>
      <h4 style={{ marginTop: 0 }}>Deudas Pendientes</h4>
      {errorMessage && <p style={{ color: 'red' }}>{errorMessage}</p>}
      {debts.length > 0 ? (
        <table border="1" style={{ width: '95%', margin: 'auto', borderCollapse: 'collapse', backgroundColor: 'white' }}>
          <thead>
            <tr>
              <th>ID</th><th>Herramienta</th><th>Monto ($)</th><th>Vencimiento</th><th>Acción</th>
            </tr>
          </thead>
          <tbody>
            {debts.map(loan => (
              <tr key={loan.id}>
                <td>{loan.id}</td>
                <td>{loan.tool?.name}</td>
                <td>{loan.totalPenalty.toFixed(0)}</td>
                <td>{loan.dueDate}</td>
                <td><button onClick={() => handlePay(loan.id)} style={{ backgroundColor: 'green', color: 'white' }}>Pagar</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      ) : (!errorMessage && <p>No se encontraron deudas.</p>)}
    </td>
  );
}

function ClientList() {
  const [clients, setClients] = useState([]);
  const [message, setMessage] = useState("");
  const [visibleDebtsClientId, setVisibleDebtsClientId] = useState(null);

  // --- AUTENTICACIÓN JWT ---
  const token = localStorage.getItem("token");
  const isAuth = !!token;
  let isAdmin = false;
  let isEmployee = false;

  if (isAuth) {
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const role = payload.role; // Asumiendo que el backend envía "ADMIN" o "USER"
        isAdmin = role === "ADMIN";
        isEmployee = role === "USER" || role === "EMPLEADO";
    } catch (e) { console.error("Error decoding token", e); }
  }
  // -------------------------

  const loadClients = () => {
    setMessage("");
    ClientService.getAll()
      .then(response => setClients(response.data))
      .catch(e => {
        console.error("Error fetching clients:", e);
        setMessage("Error al cargar la lista de clientes.");
      });
  };

  useEffect(() => {
    if (isAdmin || isEmployee) {
      loadClients();
    }
  }, [isAdmin, isEmployee]);

  const handleUpdateStatus = (id, currentStatus, clientName) => {
    setMessage("");
    if (currentStatus === "ACTIVE") {
      if (window.confirm(`¿Restringir al cliente ${clientName}?`)) {
        ClientService.updateStatus(id, "RESTRICTED")
          .then(loadClients)
          .catch(e => setMessage(`Error: ${e.response?.data?.message || e.message}`));
      }
    } else {
      if (window.confirm(`¿Intentar reactivar al cliente ${clientName}?`)) {
        ClientService.attemptReactivation(id)
          .then((response) => {
            setMessage(`Resultado: ${response.data.status}`);
            loadClients();
          })
          .catch(e => setMessage(`Error: ${e.response?.data?.message || e.message}`));
      }
    }
  };

  const handleShowOrHideDebts = (clientId) => {
    setVisibleDebtsClientId(visibleDebtsClientId === clientId ? null : clientId);
  };

  return (
    <div style={{ padding: 16 }}>
      <h2>Gestión de Clientes</h2>
      {isAdmin && (
        <Link to="/clients/add" style={{ marginBottom: '15px', display: 'inline-block' }}>➕ Registrar Nuevo Cliente</Link>
      )}
      {message && <p style={{ color: message.startsWith("Error") ? 'red' : 'green' }}>{message}</p>}

      <table border="1" style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr>
            <th>ID</th><th>RUT</th><th>Nombre</th><th>Email</th><th>Teléfono</th><th>Estado</th><th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          {clients.map(client => (
            <React.Fragment key={client.id}>
              <tr>
                <td>{client.id}</td>
                <td>{client.rut}</td>
                <td>{client.name}</td>
                <td>{client.email}</td>
                <td>{client.phone}</td>
                <td style={{ fontWeight: 'bold', color: client.status === 'RESTRICTED' ? 'red' : 'green' }}>{client.status}</td>
                <td>
                  {isAdmin && <Link to={`/clients/edit/${client.id}`} style={{ marginRight: '10px' }}>Editar</Link>}
                  {isAdmin && (
                    <button
                      onClick={() => handleUpdateStatus(client.id, client.status, client.name)}
                      style={{ backgroundColor: client.status === 'ACTIVE' ? 'darkred' : 'darkgreen', color: 'white', marginRight: '10px' }}
                    >
                      {client.status === 'ACTIVE' ? 'Restringir' : 'Activar'}
                    </button>
                  )}
                  {(isAdmin || isEmployee) && client.status === 'RESTRICTED' && (
                      <button onClick={() => handleShowOrHideDebts(client.id)} style={{ backgroundColor: 'purple', color: 'white' }}>
                          {visibleDebtsClientId === client.id ? 'Ocultar Deudas' : 'Ver Deudas'}
                        </button>
                  )}
                </td>
              </tr>
              {visibleDebtsClientId === client.id && (
                <tr className="debt-details-row">
                  <DebtDetails clientId={client.id} onPay={() => {}} messageSetter={setMessage} />
                </tr>
              )}
            </React.Fragment>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default ClientList;