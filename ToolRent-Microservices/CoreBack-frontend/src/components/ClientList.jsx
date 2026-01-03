import { useState, useEffect } from "react";
import ClientService from "../services/client.service";
import LoanService from "../services/loan.service";
import { Link } from "react-router-dom";
import keycloak from "../keycloak"; // 1. Importamos Keycloak para validar rol

const ClientList = () => {
  const [clients, setClients] = useState([]);
  const [loans, setLoans] = useState([]);
  
  // Estados para Modal y Selección
  const [selectedClient, setSelectedClient] = useState(null);
  const [showModal, setShowModal] = useState(false);
  
  // Estado para mensajes en pantalla 
  const [notification, setNotification] = useState({ message: "", type: "" });

  // 2. Verificar si es ADMIN
  const isAdmin = keycloak.tokenParsed?.realm_access?.roles?.includes('ADMIN');

  useEffect(() => {
    loadData();
  }, []);

const loadData = () => {
    ClientService.getAll()
      .then(res => {
        const data = Array.isArray(res.data) ? res.data : [];
        data.sort((a, b) => a.id - b.id); 
        setClients(data);
      })
      .catch(e => console.error(e));

    LoanService.getAll()
      .then(res => {      
          setLoans(Array.isArray(res.data) ? res.data : []); 
      })
      .catch(e => {
          console.error("Error cargando préstamos:", e);
          setLoans([]);
      });
  };

  // Función auxiliar para mostrar mensajes temporales
  const showMessage = (msg, type = "success") => {
    setNotification({ message: msg, type: type });
    // Ocultar mensaje a los 3 segundos
    setTimeout(() => {
        setNotification({ message: "", type: "" });
    }, 3000);
  };

  // --- LÓGICA DE ACCIONES MODIFICADA ---
  const handleActionClick = (client) => {
    if (client.status === "ACTIVE") {
        ClientService.changeStatus(client.id, "RESTRICTED")
            .then(() => {
                showMessage(`Cliente ${client.name} restringido correctamente.`, "warning");
                loadData(); // Actualiza la tabla visualmente
            })
            .catch(e => showMessage("Error al restringir: " + e.message, "danger"));
        
    } else {
        // Abrir modal de gestión
        setSelectedClient(client);
        setShowModal(true);
        setNotification({ message: "", type: "" }); // Limpiar mensajes previos
    }
  };

  const handlePayDebt = () => {
    if (!selectedClient) return;
    
    const amountToPay = selectedClient.balance;

    ClientService.payDebt(selectedClient.id, amountToPay)
        .then(() => {
            // CERRAR MODAL Y MOSTRAR MENSAJE EN LA TABLA
            setShowModal(false);
            setSelectedClient(null);
            loadData(); // La tabla se actualiza sola (Balance 0 y Estado ACTIVE)
            showMessage("Deuda pagada. Cliente activado automáticamente.", "success");
        })
        .catch(e => {
            alert("Error: " + e.message); 
        });
  };

  const handleManualActivation = () => {
      ClientService.changeStatus(selectedClient.id, "ACTIVE")
        .then(() => {
            setShowModal(false);
            loadData();
            showMessage("Cliente activado manualmente.", "success");
        })
        .catch(e => alert("Error: " + e.response?.data));
  };

// Filtrar préstamos (con validación extra de que 'loans' existe y es array)
  const clientLoans = selectedClient && Array.isArray(loans)
    ? loans.filter(l => l.clientId === selectedClient.id) 
    : [];
    
  return (
      <div className="container mt-4">
        {/* HEADER */}
        <div className="d-flex justify-content-between align-items-center mb-3">
            <h2>Gestión de Clientes</h2>
            
            {/* 3. RESTRICCIÓN: Solo ADMIN ve el botón de añadir */}
            {isAdmin && (
                <Link to="/clients/add" className="btn btn-success">
                    + Añadir Nuevo Cliente
                </Link>
            )}
        </div>

      {/* BARRA DE NOTIFICACIÓN INTEGRADA */}
      {notification.message && (
          <div className={`alert alert-${notification.type} role="alert"`}>
              {notification.message}
          </div>
      )}

      {/* TABLA */}
      <table className="table table-bordered table-hover shadow-sm">
        <thead className="table-dark">
          <tr>
            <th>ID</th>
            <th>RUT</th>
            <th>Nombre Completo</th>
            <th>Email</th>
            <th>Teléfono</th>
            <th>Balance (Deuda)</th>
            <th>Estado</th>
            {/* 4. RESTRICCIÓN: Ocultar columna si no es ADMIN */}
            {isAdmin && <th>Acción</th>}
          </tr>
        </thead>
        <tbody>
          {clients
          .map(client => (
            <tr key={client.id} className={client.status === 'RESTRICTED' ? 'table-danger' : ''}>
              <td>{client.id}</td>
              <td>{client.rut}</td>
              <td>{client.name} {client.lastName}</td>
              <td>{client.email}</td>
              <td>{client.phone}</td>
              <td className="fw-bold">
                {client.balance > 0 ? `$${client.balance}` : '$0'}
              </td>
              <td>
                <span className={`badge ${client.status === 'ACTIVE' ? 'bg-success' : 'bg-danger'}`}>
                    {client.status}
                </span>
              </td>
              
              {/* 5. RESTRICCIÓN: Ocultar botón de gestión si no es ADMIN */}
              {isAdmin && (
                  <td>
                    <button 
                        className={`btn btn-sm ${client.status === 'ACTIVE' ? 'btn-outline-danger' : 'btn-warning'}`}
                        onClick={() => handleActionClick(client)}
                    >
                        {client.status === 'ACTIVE' ? 'Restringir' : 'Gestionar / Activar'}
                    </button>
                  </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>

      {/* MODAL (Solo se abre si showModal es true, activado por handleActionClick que ya está oculto para empleados) */}
      {showModal && selectedClient && (
        <div className="modal show d-block" style={{backgroundColor: 'rgba(0,0,0,0.5)'}}>
            <div className="modal-dialog modal-lg">
                <div className="modal-content">
                    <div className="modal-header bg-warning">
                        <h5 className="modal-title">Gestión: {selectedClient.name}</h5>
                        <button className="btn-close" onClick={() => setShowModal(false)}></button>
                    </div>
                    
                    <div className="modal-body">
                        <div className="row">
                            <div className="col-md-6">
                                <h4>Deuda: <span className="text-danger">${selectedClient.balance}</span></h4>
                                <p className="text-muted">
                                    {selectedClient.balance > 0 
                                        ? "Debe pagar para desbloquear." 
                                        : "Sin deuda, activar manualmente."}
                                </p>
                            </div>
                            <div className="col-md-6 text-end">
                                {selectedClient.balance > 0 ? (
                                    <button 
                                        className="btn btn-lg text-white fw-bold" 
                                        style={{backgroundColor: '#6f42c1'}} 
                                        onClick={handlePayDebt}
                                    >
                                        Pagar Deuda Total
                                    </button>
                                ) : (
                                    <button 
                                        className="btn btn-success btn-lg"
                                        onClick={handleManualActivation}
                                    >
                                        Activar Cliente
                                    </button>
                                )}
                            </div>
                        </div>
                        <hr />
                        <h5>Historial de Préstamos</h5>
                        <div style={{maxHeight: '200px', overflowY: 'auto'}}>
                            <table className="table table-sm table-striped">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Herramienta ID</th>
                                        <th>Fecha</th>
                                        <th>Estado</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {clientLoans.map(loan => (
                                        <tr key={loan.id}>
                                            <td>{loan.id}</td>
                                            <td>{loan.toolId}</td>
                                            <td>{loan.loanDate}</td>
                                            <td>{loan.status}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                    
                    <div className="modal-footer">
                        <button className="btn btn-secondary" onClick={() => setShowModal(false)}>Cerrar</button>
                    </div>
                </div>
            </div>
        </div>
      )}
    </div>
  );
};

export default ClientList;