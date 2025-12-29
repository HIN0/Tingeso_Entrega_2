import { useState, useEffect } from "react";
import ReportService from "../services/report.service";

const ReportViewer = () => {
    const [summary, setSummary] = useState({ activeLoansCount: 0, delinquentClientsCount: 0 });
    const [activeLoans, setActiveLoans] = useState([]);
    const [delinquents, setDelinquents] = useState([]);
    const [ranking, setRanking] = useState([]);
    const [loading, setLoading] = useState(true);
    const [activeTab, setActiveTab] = useState("activeLoans"); // 'activeLoans', 'delinquents', 'ranking'

    useEffect(() => {
        loadReports();
    }, []);

    const loadReports = async () => {
        try {
            setLoading(true);
            // Ejecutamos todas las peticiones en paralelo para velocidad
            const [resSummary, resActive, resDelinquent, resRanking] = await Promise.all([
                ReportService.getSummary(),
                ReportService.getActiveLoans(),
                ReportService.getDelinquentClients(),
                ReportService.getToolRanking()
            ]);

            setSummary(resSummary.data);
            setActiveLoans(resActive.data);
            setDelinquents(resDelinquent.data);
            setRanking(resRanking.data);
        } catch (error) {
            console.error("Error cargando reportes:", error);
        } finally {
            setLoading(false);
        }
    };



    if (loading) {
        return <div className="container mt-5 text-center"><h3>Cargando Reportes...</h3></div>;
    }

    return (
        <div className="container mt-4">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2>Panel de Reportes y Métricas</h2>
            </div>

            {/* TARJETAS DE RESUMEN */}
            <div className="row mb-4">
                <div className="col-md-6">
                    <div className="card text-white bg-primary mb-3 shadow-sm">
                        <div className="card-header">Préstamos Activos</div>
                        <div className="card-body">
                            <h1 className="card-title display-4 text-center">{summary.activeLoansCount}</h1>
                            <p className="card-text text-center">Herramientas actualmente prestadas.</p>
                        </div>
                    </div>
                </div>
                <div className="col-md-6">
                    <div className="card text-white bg-danger mb-3 shadow-sm">
                        <div className="card-header">Clientes Morosos</div>
                        <div className="card-body">
                            <h1 className="card-title display-4 text-center">{summary.delinquentClientsCount}</h1>
                            <p className="card-text text-center">Clientes con deuda o bloqueo.</p>
                        </div>
                    </div>
                </div>
            </div>

            {/* NAVEGACIÓN DE PESTAÑAS */}
            <ul className="nav nav-tabs d-print-none mb-3">
                <li className="nav-item">
                    <button 
                        className={`nav-link ${activeTab === 'activeLoans' ? 'active fw-bold' : ''}`}
                        onClick={() => setActiveTab('activeLoans')}
                    >
                        Préstamos en Curso
                    </button>
                </li>
                <li className="nav-item">
                    <button 
                        className={`nav-link ${activeTab === 'delinquents' ? 'active fw-bold' : ''}`}
                        onClick={() => setActiveTab('delinquents')}
                    >
                        Clientes Morosos
                    </button>
                </li>
                <li className="nav-item">
                    <button 
                        className={`nav-link ${activeTab === 'ranking' ? 'active fw-bold' : ''}`}
                        onClick={() => setActiveTab('ranking')}
                    >
                        Ranking Herramientas
                    </button>
                </li>
            </ul>

            {/* CONTENIDO DE TABLAS */}
            <div className="card shadow p-3">
                
                {/* TABLA 1: PRÉSTAMOS ACTIVOS */}
                {activeTab === 'activeLoans' && (
                    <>
                        <h4 className="mb-3 text-primary">Detalle de Préstamos Activos</h4>
                        <table className="table table-striped table-hover">
                            <thead className="table-light">
                                <tr>
                                    <th>ID Préstamo</th>
                                    <th>Cliente ID</th>
                                    <th>Herramienta ID</th>
                                    <th>Fecha Inicio</th>
                                    <th>Vencimiento</th>
                                </tr>
                            </thead>
                            <tbody>
                                {activeLoans.length > 0 ? (
                                    activeLoans.map(loan => (
                                        <tr key={loan.id}>
                                            <td>{loan.id}</td>
                                            <td>{loan.clientId}</td>
                                            <td>{loan.toolId}</td>
                                            <td>{loan.loanDate}</td>
                                            <td>{loan.deadlineDate}</td>
                                        </tr>
                                    ))
                                ) : (
                                    <tr><td colSpan="5" className="text-center">No hay préstamos activos.</td></tr>
                                )}
                            </tbody>
                        </table>
                    </>
                )}

                {/* TABLA 2: MOROSOS */}
                {activeTab === 'delinquents' && (
                    <>
                        <h4 className="mb-3 text-danger">Listado de Clientes con Deuda</h4>
                        <table className="table table-striped table-hover">
                            <thead className="table-light">
                                <tr>
                                    <th>RUT</th>
                                    <th>Nombre</th>
                                    <th>Email</th>
                                    <th>Deuda ($)</th>
                                    <th>Estado</th>
                                </tr>
                            </thead>
                            <tbody>
                                {delinquents.length > 0 ? (
                                    delinquents.map(client => (
                                        <tr key={client.id}>
                                            <td>{client.rut}</td>
                                            <td>{client.name} {client.lastName}</td>
                                            <td>{client.email}</td>
                                            <td className="fw-bold text-danger">${client.balance}</td>
                                            <td>{client.status}</td>
                                        </tr>
                                    ))
                                ) : (
                                    <tr><td colSpan="5" className="text-center text-success">¡Excelente! No hay clientes morosos.</td></tr>
                                )}
                            </tbody>
                        </table>
                    </>
                )}

                {/* TABLA 3: RANKING */}
                {activeTab === 'ranking' && (
                    <>
                        <h4 className="mb-3 text-success">Ranking: Las Más Usadas</h4>
                        <table className="table table-striped table-hover">
                            <thead className="table-light">
                                <tr>
                                    <th># Posición</th>
                                    <th>ID Herramienta</th>
                                    <th>Total Préstamos Históricos</th>
                                </tr>
                            </thead>
                            <tbody>
                                {ranking.length > 0 ? (
                                    ranking.map((item, index) => (
                                        <tr key={index}>
                                            <td className="fw-bold">{index + 1}</td>
                                            <td>{item.toolId}</td>
                                            <td>{item.loansCount} veces</td>
                                        </tr>
                                    ))
                                ) : (
                                    <tr><td colSpan="3" className="text-center">Sin datos históricos.</td></tr>
                                )}
                            </tbody>
                        </table>
                    </>
                )}
            </div>
        </div>
    );
};

export default ReportViewer;