import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthService from '../services/auth.service';

const Header = () => {
    const navigate = useNavigate();

    const handleLogout = () => {
        AuthService.logout();
        navigate("/");
    };

    return (
        <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
            <div className="container-fluid">
                <Link className="navbar-brand" to="/home">ToolRent</Link>
                <button className="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                    <span className="navbar-toggler-icon"></span>
                </button>
                <div className="collapse navbar-collapse" id="navbarNav">
                    <ul className="navbar-nav me-auto">
                        <li className="nav-item"><Link className="nav-link" to="/home">Herramientas</Link></li>
                        <li className="nav-item"><Link className="nav-link" to="/clients">Clientes</Link></li>
                        <li className="nav-item"><Link className="nav-link" to="/loans">Arriendos</Link></li>
                        <li className="nav-item"><Link className="nav-link" to="/kardex">Kardex</Link></li>
                        <li className="nav-item"><Link className="nav-link" to="/reports">Reportes</Link></li>
                        <li className="nav-item"><Link className="nav-link" to="/tariffs">Tarifas</Link></li>
                    </ul>
                    <div className="d-flex">
                        <button className="btn btn-outline-danger btn-sm" onClick={handleLogout}>Cerrar Sesión</button>
                    </div>
                </div>
            </div>
        </nav>
    );
};

export default Header;