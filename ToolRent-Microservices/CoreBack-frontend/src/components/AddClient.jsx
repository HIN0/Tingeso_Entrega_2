import { useState } from "react";
import ClientService from "../services/client.service";
import { useNavigate, Link } from "react-router-dom";

const AddClient = () => {
const navigate = useNavigate();

const [client, setClient] = useState({
    rut: "",
    name: "",
    email: "",
    phone: ""
});

const [error, setError] = useState("");

const handleInputChange = (event) => {
    const { name, value } = event.target;
    setClient({ ...client, [name]: value });
};

const saveClient = (e) => {
    e.preventDefault();
    
    // 1. Validación: Asegurarse que tengan datos si o si
    if (!client.rut || !client.name || !client.email || !client.phone) {
        setError("Todos los campos son obligatorios.");
        return;
    }

    // 2. Preparar datos con valores por defecto
    const data = {
        rut: client.rut,
        name: client.name,
        email: client.email,
        phone: client.phone,
        status: "ACTIVE", // Por defecto 
        balance: 0.0      // Por defecto 
    };

    // 3. Enviar al Backend
    ClientService.create(data)
    .then(() => {
        alert("Cliente registrado exitosamente.");
        navigate("/clients"); // Volver a la lista
    })
    .catch((e) => {
        console.error("Error:", e);
        // Manejo básico de error (ej: RUT duplicado)
        const errorMsg = e.response?.data?.message || "Error al guardar cliente. Verifique el RUT.";
        setError(errorMsg);
    });
};

return (
    <div className="submit-form container mt-4">
    <div className="card shadow p-4" style={{ maxWidth: "600px", margin: "0 auto" }}>
        <h2 className="mb-4 text-primary">Registrar Nuevo Cliente</h2>
        
        {error && (
            <div className="alert alert-danger" role="alert">
                {error}
            </div>
        )}

        <form onSubmit={saveClient}>
        <div className="form-group mb-3">
            <label className="fw-bold">RUT</label>
            <input
            type="text"
            className="form-control"
            id="rut"
            required
            value={client.rut}
            onChange={handleInputChange}
            name="rut"
            placeholder="Ej: 12.345.678-9"
            />
        </div>

        <div className="form-group mb-3">
            <label className="fw-bold">Nombre Completo</label>
            <input
            type="text"
            className="form-control"
            id="name"
            required
            value={client.name}
            onChange={handleInputChange}
            name="name"
            placeholder="Ej: Juan Pérez"
            />
        </div>

        <div className="form-group mb-3">
            <label className="fw-bold">Email</label>
            <input
            type="email"
            className="form-control"
            id="email"
            required
            value={client.email}
            onChange={handleInputChange}
            name="email"
            placeholder="juan@ejemplo.com"
            />
        </div>

        <div className="form-group mb-3">
            <label className="fw-bold">Teléfono</label>
            <input
            type="text"
            className="form-control"
            id="phone"
            required
            value={client.phone}
            onChange={handleInputChange}
            name="phone"
            placeholder="+56 9 ..."
            />
        </div>

        <div className="d-flex justify-content-between mt-4">
            <Link to="/clients" className="btn btn-secondary">
            Cancelar
            </Link>
            <button type="submit" className="btn btn-success">
            Guardar Cliente
            </button>
        </div>
        </form>
    </div>
    </div>
);
};

export default AddClient;