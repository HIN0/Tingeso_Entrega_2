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

  return (
    <div className="list row">
      <div className="col-md-12">
        <h2 className="mb-4">Inventario de Herramientas</h2>
        
        {/* Botón para crear (descomentar cuando tengas AddTool) 
        <div className="mb-3">
            <Link to="/tools/add" className="btn btn-success">
                Agregar Nueva Herramienta
            </Link>
        </div>
        */}

        {error ? (
          <div className="alert alert-danger">
            Error al conectar con el servicio de Inventario. Verifica que el microservicio esté activo.
          </div>
        ) : (
          <table className="table table-striped table-bordered shadow-sm">
            <thead className="table-dark">
              <tr>
                <th>ID</th>
                <th>Nombre</th>
                <th>Precio Día</th>
                <th>Stock</th>
                <th>Estado</th>
              </tr>
            </thead>
            <tbody>
              {tools.length > 0 ? (
                tools.map((tool) => (
                  <tr key={tool.id}>
                    <td>{tool.id}</td>
                    <td>{tool.name}</td>
                    <td>${tool.price}</td> 
                    {/* Nota: Asegúrate si en tu backend es 'price', 'dailyPrice' o 'rentalPrice' */}
                    
                    <td>{tool.stock}</td>
                    
                    <td>
                      <span className={`badge ${tool.status === 'AVAILABLE' ? 'bg-success' : 'bg-warning'}`}>
                        {tool.status}
                      </span>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="5" className="text-center">No hay herramientas registradas</td>
                </tr>
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export default ToolList;