import { useState, useEffect } from "react";
import TariffService from "../services/tariff.service";

const TariffManager = () => {
    // Estado para los valores actuales del formulario
    const [tariffs, setTariffs] = useState({
        dailyRentFee: 0,
        dailyLateFee: 0,
        repairFee: 0
    });

    // Estado para guardar la copia original (para el botón Cancelar)
    const [originalTariffs, setOriginalTariffs] = useState({});
    
    // Estado para notificaciones
    const [notification, setNotification] = useState({ message: "", type: "" });

    useEffect(() => {
        loadTariffs();
    }, []);

    const loadTariffs = () => {
        TariffService.getTariffs()
            .then(res => {
                // Si el backend devuelve algo, lo usamos. Si no, ceros.
                const data = res.data || { dailyRentFee: 0, dailyLateFee: 0, repairFee: 0 };
                setTariffs(data);
                setOriginalTariffs(data); // Guardamos la copia de seguridad
            })
            .catch(e => {
                console.error(e);
                setNotification({ message: "Error cargando tarifas.", type: "danger" });
            });
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setTariffs({ ...tariffs, [name]: parseFloat(value) });
    };

    const handleSave = () => {
        TariffService.saveTariffs(tariffs)
            .then(res => {
                setTariffs(res.data);
                setOriginalTariffs(res.data); // Actualizamos la copia de seguridad
                showMessage("Tarifas actualizadas correctamente.", "success");
            })
            .catch(e => {
                console.error(e);
                showMessage("Error al guardar cambios.", "danger");
            });
    };

    const handleCancel = () => {
        // Revertir a los valores originales
        setTariffs(originalTariffs);
        showMessage("Cambios deshechos. Valores restaurados.", "warning");
    };

    const showMessage = (msg, type) => {
        setNotification({ message: msg, type: type });
        setTimeout(() => setNotification({ message: "", type: "" }), 3000);
    };

    return (
        <div className="container mt-4">
            <h2 className="mb-4">Configuración de Tarifas</h2>

            {/* NOTIFICACIÓN INTEGRADA */}
            {notification.message && (
                <div className={`alert alert-${notification.type}`} role="alert">
                    {notification.message}
                </div>
            )}

            <div className="card shadow">
                <div className="card-body">
                    <table className="table table-bordered">
                        <thead className="table-dark">
                            <tr>
                                <th>Concepto</th>
                                <th>Valor Actual ($)</th>
                                <th>Descripción</th>
                            </tr>
                        </thead>
                        <tbody>
                            {/* Tarifa 1: Arriendo Diario */}
                            <tr>
                                <td className="align-middle fw-bold">Arriendo Diario</td>
                                <td>
                                    <input 
                                        type="number" 
                                        className="form-control"
                                        name="dailyRentFee"
                                        value={tariffs.dailyRentFee}
                                        onChange={handleInputChange}
                                    />
                                </td>
                                <td className="align-middle text-muted">Costo por día de uso normal.</td>
                            </tr>

                            {/* Tarifa 2: Multa por Atraso */}
                            <tr>
                                <td className="align-middle fw-bold text-danger">Multa por Atraso</td>
                                <td>
                                    <input 
                                        type="number" 
                                        className="form-control"
                                        name="dailyLateFee"
                                        value={tariffs.dailyLateFee}
                                        onChange={handleInputChange}
                                    />
                                </td>
                                <td className="align-middle text-muted">Costo extra por cada día de retraso.</td>
                            </tr>

                            {/* Tarifa 3: Reparación */}
                            <tr>
                                <td className="align-middle fw-bold text-warning">Cargo por Reparación</td>
                                <td>
                                    <input 
                                        type="number" 
                                        className="form-control"
                                        name="repairFee"
                                        value={tariffs.repairFee}
                                        onChange={handleInputChange}
                                    />
                                </td>
                                <td className="align-middle text-muted">Monto fijo si la herramienta vuelve dañada.</td>
                            </tr>
                        </tbody>
                    </table>

                    <div className="d-flex justify-content-end gap-2 mt-3">
                        <button className="btn btn-secondary" onClick={handleCancel}>
                            Cancelar / Deshacer
                        </button>
                        <button className="btn btn-primary" onClick={handleSave}>
                            Guardar Cambios
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default TariffManager;