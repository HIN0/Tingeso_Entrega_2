import { useState } from "react";
import ToolService from "../services/tool.service";
import { useNavigate, Link } from "react-router-dom";

const AddTool = () => {
const navigate = useNavigate();

// Estado inicial del formulario
const [tool, setTool] = useState({
    name: "",
    category: "",
    status: "AVAILABLE", // Valor por defecto lógico
    stock: 1,
    inRepair: 0,
    replacementValue: 1000 // El valor base
});

const [error, setError] = useState(false);

const handleInputChange = (event) => {
    const { name, value } = event.target;
    setTool({ ...tool, [name]: value });
};

const saveTool = (e) => {
    e.preventDefault();
    
    // Validación básica
    if (!tool.name || !tool.category) {
        alert("Por favor complete nombre y categoría");
        return;
    }

    ToolService.create(tool)
    .then(() => {
        // Al guardar exitosamente, el backend ya registra el INCOME en Kardex
        // Redirigimos al listado
        navigate("/tools");
    })
    .catch((e) => {
        console.error("Error al crear herramienta", e);
        setError(true);
    });
};

return (
    <div className="submit-form container mt-4">
    <div className="card shadow p-4">
        <h2 className="mb-4 text-primary">Añadir Nueva Herramienta</h2>
        
        {error && (
            <div className="alert alert-danger">
                Ocurrió un error al intentar guardar. Verifique los datos o la conexión.
            </div>
        )}

        <form onSubmit={saveTool}>
        {/* Fila 1: Nombre y Categoría */}
        <div className="row">
            <div className="col-md-6 mb-3">
                <label className="form-label">Nombre</label>
                <input
                    type="text"
                    className="form-control"
                    id="name"
                    required
                    value={tool.name}
                    onChange={handleInputChange}
                    name="name"
                />
            </div>
            <div className="col-md-6 mb-3">
                <label className="form-label">Categoría</label>
                <input
                    type="text"
                    className="form-control"
                    id="category"
                    required
                    value={tool.category}
                    onChange={handleInputChange}
                    name="category"
                />
            </div>
        </div>

        {/* Fila 2: Stock y Estado */}
        <div className="row">
            <div className="col-md-6 mb-3">
                <label className="form-label">Stock Inicial</label>
                <input
                    type="number"
                    className="form-control"
                    id="stock"
                    required
                    min="0"
                    value={tool.stock}
                    onChange={handleInputChange}
                    name="stock"
                />
            </div>
        </div>
        
        {/*
            <div className="col-md-6 mb-3">
                <label className="form-label">Estado</label>
                <select 
                    className="form-control form-select"
                    id="status"
                    value={tool.status}
                    onChange={handleInputChange}
                    name="status"
                >
                    <option value="AVAILABLE">AVAILABLE (Disponible)</option>
                    <option value="REPAIRING">REPAIRING (En Reparación)</option>
                    <option value="DECOMMISSIONED">DECOMMISSIONED (De Baja)</option>
                </select>
            </div>
        */}
        
        {/* Fila 3: En Reparación y Valor Reposición */}
        <div className="row">
            <div className="col-md-6 mb-3">
                <label className="form-label">En Reparación (Cantidad)</label>
                <input
                    type="number"
                    className="form-control"
                    id="inRepair"
                    required
                    min="0"
                    value={tool.inRepair}
                    onChange={handleInputChange}
                    name="inRepair"
                />
            </div>
            <div className="col-md-6 mb-3">
                <label className="form-label">Valor de Reposición ($)</label>
                <input
                    type="number"
                    className="form-control"
                    id="replacementValue"
                    required
                    min="0"
                    value={tool.replacementValue}
                    onChange={handleInputChange}
                    name="replacementValue"
                />
            </div>
        </div>

        {/* Botones de Acción */}
        <div className="mt-3 d-flex gap-2">
            <button type="submit" className="btn btn-success">
                Guardar
            </button>
            <Link to="/tools" className="btn btn-secondary">
                Cancelar
            </Link>
        </div>
        </form>
    </div>
    </div>
);
};

export default AddTool;