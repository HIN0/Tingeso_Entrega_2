import { useState, useEffect } from "react";
import ToolService from "../services/tool.service";
import { Link } from "react-router-dom";

const ToolList = () => {
  const [tools, setTools] = useState([]);
  const [error, setError] = useState(false);

  // Al cargar el componente, pedimos las herramientas
  useEffect(() => {
    retrieveTools();
  }, []);

  const retrieveTools = () => {
    ToolService.getAll()
      .then((response) => {
        setTools(response.data);
        console.log("Herramientas cargadas:", response.data);
      })
      .catch((e) => {
        console.error("Error al cargar herramientas:", e);
        setError(true);
      });
  };

  // --- LÓGICA DE FILTRADO Y ORDENAMIENTO ---

  // 1. Tabla Principal: AVAILABLE y REPAIRING (u otros estados activos)
  const activeTools = tools
    .filter(t => t.status === "AVAILABLE" || t.status === "REPAIRING" || t.status === "LOANED")
    .sort((a, b) => a.id - b.id); // Ordenar por ID

  // 2. Tabla Secundaria: DECOMMISSIONED
  const decommissionedTools = tools
    .filter(t => t.status === "DECOMMISSIONED")
    .sort((a, b) => a.id - b.id); // Ordenar por ID

  return (
    <div className="list row">
      <div className="col-md-12">
      <div className="d-flex justify-content-between align-items-center mb-4">
                  <h2>Inventario de Herramientas</h2>
                  {/* BOTÓN NUEVO AQUÍ */}
                  <Link to="/tools/add" className="btn btn-success">
                      + Añadir Nueva Herramienta
                  </Link>
              </div>
              
        {error && (
          <div className="alert alert-danger mb-4">
            Error al conectar con el servicio de Inventario. Verifica que el microservicio esté activo.
          </div>
        )}

        {/* --- TABLA 1: HERRAMIENTAS ACTIVAS --- */}
        <h3 className="mb-3 text-primary">Inventario Activo (Disponibles y En Reparación)</h3>
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
                    <span className={`badge ${
                      tool.status === 'AVAILABLE' ? 'bg-success' : 
                      tool.status === 'REPAIRING' ? 'bg-warning text-dark' : 
                      'bg-info'
                    }`}>
                      {tool.status}
                    </span>
                  </td>
                  <td>{tool.stock}</td>
                  <td>{tool.inRepair}</td>
                  <td>${tool.replacementValue}</td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="7" className="text-center">No hay herramientas activas</td>
              </tr>
            )}
          </tbody>
        </table>

        {/* --- TABLA 2: HERRAMIENTAS DADAS DE BAJA --- */}
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

      </div>
    </div>
  );
};

export default ToolList;