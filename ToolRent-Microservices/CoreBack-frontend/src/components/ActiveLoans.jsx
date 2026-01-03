import { useState, useEffect } from "react";
import LoanService from "../services/loan.service";
import { Link } from "react-router-dom";

const ActiveLoans = () => {
    const [loans, setLoans] = useState([]);
    
    // Estado para el Modal de Devolución
    const [selectedLoan, setSelectedLoan] = useState(null);
    const [isDamaged, setIsDamaged] = useState(false);
    const [isIrreparable, setIsIrreparable] = useState(false);
    
    // Nuevo estado auxiliar para manejar los Radio Buttons visualmente
    // Valores posibles: 'GOOD', 'DAMAGED', 'IRREPARABLE'
    const [returnCondition, setReturnCondition] = useState('GOOD'); 
    
    const [msg, setMsg] = useState("");

    useEffect(() => {
        loadLoans();
    }, []);

    const loadLoans = () => {
        LoanService.getAll()
            .then(res => setLoans(res.data))
            .catch(e => console.error(e));
    };

    const handleReturnClick = (loan) => {
        setSelectedLoan(loan);
        // Se resetea el estado por defecto (Buen estado)
        setReturnCondition('GOOD');
        setIsDamaged(false);
        setIsIrreparable(false);
        setMsg("");
    };

    // Función para manejar el cambio de los Radio Buttons
    const handleConditionChange = (condition) => {
        setReturnCondition(condition);
        
        // Lógica de mapeo para el backend
        if (condition === 'GOOD') {
            setIsDamaged(false);
            setIsIrreparable(false);
        } else if (condition === 'DAMAGED') {
            setIsDamaged(true);
            setIsIrreparable(false);
        } else if (condition === 'IRREPARABLE') {
            setIsDamaged(false); 
            setIsIrreparable(true);
        }
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
            <h2>Registro Préstamos</h2>
            <Link to="/loans/add" className="btn btn-primary mb-3">Nuevo Préstamo</Link>

            <table className="table table-bordered table-striped">
                <thead className="table-dark">
                    <tr>
                        <th>ID</th>
                        <th>Fecha Inicio</th>
                        <th>Fecha Vencimiento</th>
                        <th>Cliente</th>
                        <th>Herramienta</th>
                        <th>Status</th>
                        <th>Acción</th>
                    </tr>
                </thead>
                <tbody>
                    
                    {loans
                    .filter(loan => loan.status === "ACTIVE")
                    .map(loan => (
                        <tr key={loan.id}>
                            <td>{loan.id}</td>
                            <td>{loan.loanDate}</td>
                            <td>{loan.deadlineDate}</td>
                            <td>(ID: {loan.clientId}) {loan.clientName}</td> 
                            <td>(ID: {loan.toolId}) { loan.toolName}</td>
                            <td>{loan.status}</td>
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

            {/* Modal Simulado de Devolución */}
            {selectedLoan && (
                <div className="modal show d-block" style={{backgroundColor: 'rgba(0,0,0,0.5)'}}>
                    <div className="modal-dialog">
                        <div className="modal-content">
                            <div className="modal-header">
                                <h5 className="modal-title">Confirmar Devolución (ID: {selectedLoan.id})</h5>
                                <button className="btn-close" onClick={() => setSelectedLoan(null)}></button>
                            </div>
                            <div className="modal-body">
                                <p>Seleccione el estado de la herramienta:</p>
                                
                                {/* OPCIÓN 1: BUEN ESTADO */}
                                <div className="form-check mb-2">
                                    <input 
                                        className="form-check-input" 
                                        type="radio" 
                                        name="condition"
                                        id="radioGood"
                                        checked={returnCondition === 'GOOD'}
                                        onChange={() => handleConditionChange('GOOD')}
                                    />
                                    <label className="form-check-label text-success fw-bold" htmlFor="radioGood">
                                        En Buen Estado
                                    </label>
                                </div>

                                {/* OPCIÓN 2: CON DAÑOS */}
                                <div className="form-check mb-2">
                                    <input 
                                        className="form-check-input" 
                                        type="radio" 
                                        name="condition"
                                        id="radioDamaged"
                                        checked={returnCondition === 'DAMAGED'}
                                        onChange={() => handleConditionChange('DAMAGED')}
                                    />
                                    <label className="form-check-label text-warning fw-bold" htmlFor="radioDamaged">
                                        Con Daños (Reparable)
                                    </label>
                                </div>

                                {/* OPCIÓN 3: IRREPARABLE */}
                                <div className="form-check mb-3">
                                    <input 
                                        className="form-check-input" 
                                        type="radio" 
                                        name="condition"
                                        id="radioIrreparable"
                                        checked={returnCondition === 'IRREPARABLE'}
                                        onChange={() => handleConditionChange('IRREPARABLE')}
                                    />
                                    <label className="form-check-label text-danger fw-bold" htmlFor="radioIrreparable">
                                        Irreparable (Pérdida Total)
                                    </label>
                                </div>
                                
                                {msg && <div className="alert alert-info">{msg}</div>}
                            </div>
                            <div className="modal-footer">
                                <button className="btn btn-secondary" onClick={() => setSelectedLoan(null)}>Cancelar</button>
                                <button className="btn btn-success" onClick={confirmReturn}>Confirmar Devolución</button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ActiveLoans;