import { Link } from "react-router-dom";

function Header() {
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
        <ul className="navbar-nav ms-auto">
        <li className="nav-item">
            <Link className="nav-link" to="/tools">Herramientas</Link>
        </li>
        <li className="nav-item">
            <Link className="nav-link" to="/loans">Préstamos</Link>
        </li>
        <li className="nav-item">
            <Link className="nav-link" to="/clients">Clientes</Link>
        </li>
        <li className="nav-item">
            <Link className="nav-link" to="/tariffs">Tarifas</Link> 
        </li>
        <li className="nav-item">
            <Link className="nav-link" to="/login">Login</Link>
        </li>
        </ul>
    </div>
    </nav>
);
}

export default Header;