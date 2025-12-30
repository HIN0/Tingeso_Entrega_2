    import { useState, useEffect } from "react";
    import kardexService from "../services/kardex.service";
    import toolService from "../services/tool.service";

    const KardexList = () => {
    const [movements, setMovements] = useState([]);
    const [toolsMap, setToolsMap] = useState({}); // Para mostrar nombres en vez de IDs
    
    // Estados para los filtros
    const [filterToolId, setFilterToolId] = useState(""); // RF5.2
    const [startDate, setStartDate] = useState("");       // RF5.3
    const [endDate, setEndDate] = useState("");           // RF5.3

    useEffect(() => {
        // 1. Cargamos las herramientas primero para tener sus nombres
        loadTools();
        // 2. Cargamos el kardex inicial (todo)
        loadKardex();
    }, []);

    const loadTools = () => {
        toolService.getAll()
        .then(response => {
            const map = {};
            response.data.forEach(tool => {
            map[tool.id] = tool.name; // Creamos un mapa ID -> Nombre
            });
            setToolsMap(map);
        })
        .catch(e => console.error("Error cargando herramientas", e));
    };

    const loadKardex = () => {
        // Preparamos los parámetros. Si están vacíos, enviamos undefined para que axios los ignore
        const params = {
        toolId: filterToolId || undefined,
        startDate: startDate || undefined,
        endDate: endDate || undefined
        };

        kardexService.getAll(params)
        .then((response) => {
            setMovements(response.data || []);
        })
        .catch((error) => {
            console.log("No se encontraron movimientos o hubo error", error);
            setMovements([]); // Limpiamos la tabla si no hay datos
        });
    };

    const handleFilter = (e) => {
        e.preventDefault();
        loadKardex();
    };

    // Función auxiliar para colores de badges
    const getBadgeClass = (type) => {
        switch (type) {
            case 'INCOME': return 'bg-success';      // Verde
            case 'DECOMMISSIONED': return 'bg-danger';      // Rojo
            case 'LOANED': return 'bg-warning text-dark'; // Amarillo
            case 'RETURNED': return 'bg-info text-dark';  // Azul claro
            case 'REPAIRING': return 'bg-primary';   // Azul
            default: return 'bg-secondary';
        }
    };

    return (
        <div className="container mt-4">
        <h2 className="mb-4">Kardex - Historial de Movimientos</h2>

        {/* --- ZONA DE FILTROS (RF5.2 y RF5.3) --- */}
        <div className="card p-3 mb-4 bg-light">
            <form onSubmit={handleFilter} className="row g-3 align-items-end">
            
            {/* RF5.2: Filtro por ID de Herramienta */}
            <div className="col-md-3">
                <label className="form-label fw-bold">ID Herramienta</label>
                <input
                type="number"
                className="form-control"
                placeholder="Ej: 1"
                value={filterToolId}
                onChange={(e) => setFilterToolId(e.target.value)}
                />
            </div>

            {/* RF5.3: Filtro por Rango de Fechas */}
            <div className="col-md-3">
                <label className="form-label fw-bold">Fecha Desde</label>
                <input
                type="date"
                className="form-control"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                />
            </div>
            <div className="col-md-3">
                <label className="form-label fw-bold">Fecha Hasta</label>
                <input
                type="date"
                className="form-control"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                />
            </div>

            <div className="col-md-3">
                <button type="submit" className="btn btn-primary w-100">
                <i className="bi bi-search"></i> Filtrar Movimientos
                </button>
            </div>
            </form>
        </div>

        {/* --- TABLA DE RESULTADOS --- */}
        <table className="table table-striped table-hover border shadow-sm">
            <thead className="table-dark">
            <tr>
                <th>Fecha</th>
                <th>Herramienta (ID)</th>
                <th>Tipo Movimiento</th>
                <th>Cantidad</th>
                <th>Usuario</th>
            </tr>
            </thead>
            <tbody>
            {movements.length > 0 ? (
                movements.map((mov) => (
                <tr key={mov.id}>
                    <td>{mov.date}</td>
                    <td>
                        {/* Mostramos el nombre si existe, sino el ID */}
                        {toolsMap[mov.toolId] ? `${toolsMap[mov.toolId]} (ID: ${mov.toolId})` : `ID: ${mov.toolId}`}
                    </td>
                    <td>
                    <span className={`badge ${getBadgeClass(mov.movementType)}`}>
                        {mov.movementType}
                    </span>
                    </td>
                    <td className="fw-bold">{mov.quantity}</td>
                    <td>{mov.username || "Sistema"}</td>
                </tr>
                ))
            ) : (
                <tr>
                <td colSpan="5" className="text-center text-muted py-4">
                    No se encontraron movimientos con los filtros seleccionados.
                </td>
                </tr>
            )}
            </tbody>
        </table>
        </div>
    );
    };

    export default KardexList;