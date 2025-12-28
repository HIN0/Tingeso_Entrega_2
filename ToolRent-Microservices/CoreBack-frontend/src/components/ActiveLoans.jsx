import { useState, useEffect } from "react";
import LoanService from "../services/loan.service";
import { Link } from "react-router-dom";

const ActiveLoans = () => {
    const [loans, setLoans] = useState([]);
    
    // Estado para el Modal de Devolución
    const [selectedLoan, setSelectedLoan] = useState(null);
    const [isDamaged, setIsDamaged] = useState(false);
    const [isIrreparable, setIsIrreparable] = useState(false);
    const [msg, setMsg] = useState("");

    useEffect(() => {
        loadLoans();
    }, []);

    const loadLoans = () => {
        LoanService.getActive()
            .then(res => setLoans(res.data))
            .catch(e => console.error(e));
    };

    const handleReturnClick = (loan) => {
        setSelectedLoan(loan);
        setIsDamaged(false);
        setIsIrreparable(false);
        setMsg("");
    };

    const confirmReturn = () => {
        if(!selectedLoan) return;

        LoanService.returnLoan(selectedLoan.id, isDamaged, isIrreparable)
            .then(() => {
                setMsg("Devolución exitosa");
                setSelectedLoan(null); // Cerrar modal
                loadLoans(); // Recargar lista
            })
            .catch(e => {
                setMsg("Error al devolver: " + e.message);
            });
    };

    return (
        <div className="container mt-4">
            <h2>Préstamos Activos</h2>
            <Link to="/loans/add" className="btn btn-primary mb-3">Nuevo Préstamo</Link>

            <table className="table table-bordered table-striped">
                <thead className="table-dark">
                    <tr>
                        <th>ID</th>
                        <th>Fecha Préstamo</th>
                        <th>Vence</th>
                        <th>Cliente</th>
                        <th>Herramienta</th>
                        <th>Acción</th>
                    </tr>
                </thead>
                <tbody>
                    {loans.map(loan => (
                        <tr key={loan.id}>
                            <td>{loan.id}</td>
                            <td>{loan.loanDate}</td>
                            <td>{loan.deadlineDate}</td>
                            {/* Nota: Si el backend no devuelve nombres en /active, solo verás IDs */}
                            <td>{loan.clientId}</td> 
                            <td>{loan.toolId}</td>
                            <td>
                                <button 
                                    className="btn btn-warning btn-sm"
                                    onClick={() => handleReturnClick(loan)}
                                >
                                    Devolver
                                </button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>

            {/* Simulación de Modal para Devolución */}
            {selectedLoan && (
                <div className="card p-3 mt-3 border-danger">
                    <h4>Confirmar Devolución (ID: {selectedLoan.id})</h4>
                    <div className="form-check">
                        <input 
                            className="form-check-input" 
                            type="checkbox" 
                            checked={isDamaged}
                            onChange={e => setIsDamaged(e.target.checked)}
                        />
                        <label className="form-check-label">¿Tiene Daños?</label>
                    </div>
                    
                    {isDamaged && (
                        <div className="form-check ms-4">
                            <input 
                                className="form-check-input" 
                                type="checkbox" 
                                checked={isIrreparable}
                                onChange={e => setIsIrreparable(e.target.checked)}
                            />
                            <label className="form-check-label text-danger fw-bold">¿Es Irreparable (Destruida)?</label>
                        </div>
                    )}

                    <div className="mt-3">
                        <button className="btn btn-success me-2" onClick={confirmReturn}>Confirmar</button>
                        <button className="btn btn-secondary" onClick={() => setSelectedLoan(null)}>Cancelar</button>
                    </div>
                    {msg && <div className="text-info mt-2">{msg}</div>}
                </div>
            )}
        </div>
    );
};

export default ActiveLoans;