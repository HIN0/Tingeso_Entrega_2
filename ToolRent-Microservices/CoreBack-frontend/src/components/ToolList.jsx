import { useState, useEffect } from "react";
import ToolService from "../services/tool.service";
import { Link } from "react-router-dom";
import keycloak from "../keycloak"; // 1. Importamos Keycloak para verificar el rol

const ToolList = () => {
  const [tools, setTools] = useState([]);
  const [error, setError] = useState(false);
  
  // Estados para el Modal de Edición
  const [editingTool, setEditingTool] = useState(null);
  const [editValue, setEditValue] = useState(0);

  // 2. Verificar si es ADMIN (Revisa si el rol 'ADMIN' está en el token)
  const isAdmin = keycloak.tokenParsed?.realm_access?.roles?.includes('ADMIN');

  useEffect(() => {
    retrieveTools();
  }, []);

  const retrieveTools = () => {
    ToolService.getAll()
      .then((response) => {
        setTools(response.data);
      })
      .catch((e) => {
        console.error("Error al cargar herramientas:", e);
        setError(true);
      });
  };

  // --- Lógica Dar de Baja ---
  const handleDecommission = (id) => {
    const confirm = window.confirm("¿Está seguro de DAR DE BAJA esta herramienta? Stock y Reparación pasarán a 0.");
    if (confirm) {
        ToolService.updateStatus(id, "DECOMMISSIONED")
            .then(() => {
                alert("Herramienta dada de baja correctamente.");
                retrieveTools(); // Recargar tablas
            })
            .catch(e => {
                console.error(e);
                alert("Error al dar de baja.");
            });
    }
  };

  // --- Lógica Edición (Valor Reposición) ---
  const openEditModal = (tool) => {
    setEditingTool(tool);
    setEditValue(tool.replacementValue);
  };

  const saveEdit = () => {
    if (!editingTool) return;

    // Creamos el objeto actualizado manteniendo los otros datos
    const updatedTool = { ...editingTool, replacementValue: editValue };

    ToolService.update(editingTool.id, updatedTool)
        .then(() => {
            setEditingTool(null); // Cerrar modal
            retrieveTools(); // Recargar tabla
        })
        .catch(e => {
            console.error(e);
            alert("Error al actualizar valor.");
        });
  };

  // Filtros
  const activeTools = tools
    .filter(t => t.status === "AVAILABLE" || t.status === "REPAIRING" || t.status === "LOANED")
    .sort((a, b) => a.id - b.id);

  const decommissionedTools = tools
    .filter(t => t.status === "DECOMMISSIONED")
    .sort((a, b) => a.id - b.id);

  return (
    <div className="list row container">
      <div className="col-md-12">
        <div className="d-flex justify-content-between align-items-center mb-4 mt-4">
            <h2>Inventario de Herramientas</h2>
            
            {/* 3. SOLO ADMIN puede ver el botón de Añadir */}
            {isAdmin && (
                <Link to="/tools/add" className="btn btn-success">
                    + Añadir Nueva Herramienta
                </Link>
            )}
        </div>

        {error && <div className="alert alert-danger">Error de conexión con Inventario.</div>}

        {/* TABLA ACTIVA */}
        <h3 className="mb-3 text-primary">Inventario Activo</h3>
        <table className="table table-striped table-bordered shadow-sm mb-5">
          <thead className="table-dark">
            <tr>
              <th>ID</th>
              <th>Nombre</th>
              <th>Categoría</th>
              <th>Estado</th>
              <th>Stock</th>
              <th>En Reparación</th>
              <th>Valor Reposición</th>
              {/* 4. Ocultar columnas de acciones si no es ADMIN */}
              {isAdmin && <th>Acciones</th>}
              {isAdmin && <th>Dar de Baja</th>}
            </tr>
          </thead>
          <tbody>
            {activeTools.length > 0 ? (
              activeTools.map((tool) => (
                <tr key={tool.id}>
                  <td>{tool.id}</td>
                  <td>{tool.name}</td>
                  <td>{tool.category}</td>
                  <td>
                    <span className={`badge ${tool.status === 'AVAILABLE' ? 'bg-success' : 'bg-warning text-dark'}`}>
                      {tool.status}
                    </span>
                  </td>
                  <td>{tool.stock}</td>
                  <td>{tool.inRepair}</td>
                  <td>${tool.replacementValue}</td>
                  
                  {/* Botón EDITAR (Solo Admin) */}
                  {isAdmin && (
                    <td>
                        <button className="btn btn-outline-primary btn-sm" onClick={() => openEditModal(tool)}>
                            Edit
                        </button>
                    </td>
                  )}

                  {/* Botón DECOMMISSIONAR (Solo Admin) */}
                  {isAdmin && (
                    <td>
                        <button 
                            className="btn btn-danger btn-sm fw-bold" 
                            onClick={() => handleDecommission(tool.id)}
                        >
                            DECOMMISSIONAR
                        </button>
                    </td>
                  )}
                </tr>
              ))
            ) : (
              // Ajustar el colspan dependiendo de si es admin o no (9 columnas con admin, 7 sin admin)
              <tr><td colSpan={isAdmin ? "9" : "7"} className="text-center">No hay herramientas activas</td></tr>
            )}
          </tbody>
        </table>

        {/* TABLA BAJAS */}
        <h3 className="mb-3 text-danger">Historial de Bajas (Decommissioned)</h3>
        <table className="table table-striped table-bordered shadow-sm">
          <thead className="table-secondary">
            <tr>
              <th>ID</th>
              <th>Nombre</th>
              <th>Categoría</th>
              <th>Valor Reposición</th>
            </tr>
          </thead>
          <tbody>
            {decommissionedTools.length > 0 ? (
              decommissionedTools.map((tool) => (
                <tr key={tool.id}>
                  <td>{tool.id}</td>
                  <td>{tool.name}</td>
                  <td>{tool.category}</td>
                  <td>${tool.replacementValue}</td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="4" className="text-center">No hay herramientas dadas de baja</td>
              </tr>
            )}
          </tbody>
        </table>

        {/* MODAL SIMPLE DE EDICIÓN (Solo se renderiza si hay editingTool, que solo admin puede activar) */}
        {editingTool && (
            <div className="modal show d-block" style={{backgroundColor: 'rgba(0,0,0,0.5)'}}>
                <div className="modal-dialog">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h5 className="modal-title">Editar {editingTool.name}</h5>
                            <button className="btn-close" onClick={() => setEditingTool(null)}></button>
                        </div>
                        <div className="modal-body">
                            <div className="form-group">
                                <label>Nuevo Valor de Reposición ($)</label>
                                <input 
                                    type="number" 
                                    className="form-control" 
                                    value={editValue}
                                    onChange={(e) => setEditValue(e.target.value)}
                                />
                            </div>
                        </div>
                        <div className="modal-footer">
                            <button className="btn btn-secondary" onClick={() => setEditingTool(null)}>Cancelar</button>
                            <button className="btn btn-primary" onClick={saveEdit}>Guardar Cambios</button>
                        </div>
                    </div>
                </div>
            </div>
        )}

      </div>
    </div>
  );
};

export default ToolList;