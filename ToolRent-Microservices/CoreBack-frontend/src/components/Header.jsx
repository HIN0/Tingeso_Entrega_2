import { Link } from "react-router-dom";

function Header({ keycloak }) {

const handleLogout = () => {
    keycloak.logout({ redirectUri: 'http://localhost:5173/' });
};

const handleLogin = () => {
    keycloak.login();
};

const username = keycloak?.tokenParsed?.preferred_username || "Usuario";

// VERIFICACIÓN DE ROL
const isAdmin = keycloak?.tokenParsed?.realm_access?.roles?.includes('ADMIN');

return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-primary px-4">
    <Link className="navbar-brand" to="/">
        ToolRent Microservices
    </Link>
    <button
        className="navbar-toggler"
        type="button"
        data-bs-toggle="collapse"
        data-bs-target="#navbarNav"
    >
        <span className="navbar-toggler-icon"></span>
    </button>
    
    <div className="collapse navbar-collapse" id="navbarNav">
        <ul className="navbar-nav ms-auto align-items-center">
        <li className="nav-item">
            <Link className="nav-link fw-bold" to="/tools">Herramientas</Link>
        </li>
        <li className="nav-item">
            <Link className="nav-link fw-bold" to="/loans">Préstamos</Link>
        </li>
        <li className="nav-item">
            <Link className="nav-link fw-bold" to="/clients">Clientes</Link>
        </li>

        {/*  TARIFAS Y KARDEX SOLO PARA ADMIN */}
        {isAdmin && (
            <>
                <li className="nav-item">
                    <Link className="nav-link fw-bold" to="/tariffs">Tarifas</Link> 
                </li>
                <li className="nav-item">
                    <Link className="nav-link fw-bold" to="/kardex">Kardex</Link>
                </li>
            </>
        )}

        <li className="nav-item">
            <Link className="nav-link fw-bold" to="/reports">Reportes</Link> 
        </li>
        
        <li className="nav-item ms-3">
            {keycloak && keycloak.authenticated ? (
            <div className="d-flex align-items-center gap-2">
                <span className="text-white fw-light small">
                Hola, <strong>{username}</strong> <strong>({isAdmin ? 'ADMIN' : 'EMPLOYEE'})</strong>
                </span>
                <button
                className="btn btn-danger btn-sm"
                onClick={handleLogout}
                >
                Salir
                </button>
            </div>
            ) : (
            <button
                className="btn btn-light btn-sm text-primary fw-bold"
                onClick={handleLogin}
            >
                Login
            </button>
            )}
        </li>
        </ul>
    </div>
    </nav>
);
}

export default Header;