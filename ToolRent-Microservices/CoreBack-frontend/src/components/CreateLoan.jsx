import { useState, useEffect } from "react";
import ToolService from "../services/tool.service";
import ClientService from "../services/client.service";
import LoanService from "../services/loan.service";
import { useNavigate } from "react-router-dom";

const CreateLoan = () => {
    const [clients, setClients] = useState([]);
    const [tools, setTools] = useState([]);
    const [selectedClient, setSelectedClient] = useState("");
    const [selectedTool, setSelectedTool] = useState("");
    const [deadline, setDeadline] = useState("");
    const [message, setMessage] = useState("");
    
    const navigate = useNavigate();

    useEffect(() => {
        // Cargar Clientes Activos
        ClientService.getAll().then(res => {
            // Filtrar solo los que NO están restringidos
            const activeClients = res.data.filter(c => c.status !== 'RESTRICTED');
            setClients(activeClients);
        });

        // Cargar Herramientas Disponibles
        ToolService.getAll().then(res => {
            const availableTools = res.data.filter(t => t.stock > 0 && t.status === 'AVAILABLE');
            setTools(availableTools);
        });
    }, []);

    const saveLoan = (e) => {
        e.preventDefault();
        setMessage("");

        if(!selectedClient || !selectedTool) {
            setMessage("Seleccione cliente y herramienta");
            return;
        }

        LoanService.create(selectedClient, selectedTool, deadline)
            .then(() => {
                navigate("/loans"); // Redirigir a lista de préstamos
            })
            .catch(e => {
                const errorMsg = e.response && e.response.data && e.response.data.message 
                                ? e.response.data.message 
                                : "Error al crear préstamo";
                setMessage(errorMsg);
            });
    };

    return (
        <div className="submit-form container mt-4">
            <h3>Nuevo Préstamo</h3>
            {message && <div className="alert alert-danger">{message}</div>}
            
            <form onSubmit={saveLoan}>
                <div className="form-group mb-3">
                    <label>Cliente (Solo Activos)</label>
                    <select className="form-control" onChange={(e) => setSelectedClient(e.target.value)}>
                        <option value="">Seleccione...</option>
                        {clients.map(c => (
                            <option key={c.id} value={c.id}>{c.rut} - {c.name}</option>
                        ))}
                    </select>
                </div>

                <div className="form-group mb-3">
                    <label>Herramienta (Disponible)</label>
                    <select className="form-control" onChange={(e) => setSelectedTool(e.target.value)}>
                        <option value="">Seleccione...</option>
                        {tools.map(t => (
                            <option key={t.id} value={t.id}>{t.name} (Stock: {t.stock})</option>
                        ))}
                    </select>
                </div>

                <div className="form-group mb-3">
                    <label>Fecha Devolución (Opcional, defecto 7 días)</label>
                    <input 
                        type="date" 
                        className="form-control"
                        onChange={(e) => setDeadline(e.target.value)}
                    />
                </div>

                <button type="submit" className="btn btn-success">Registrar Préstamo</button>
            </form>
        </div>
    );
};

export default CreateLoan;