import { useEffect } from "react";

const PrivateRoute = ({ children, keycloak, roles = [] }) => {

// 1. Si no está logueado, mándalo al Login de Keycloak
if (!keycloak.authenticated) {
    // Usamos useEffect para evitar efectos secundarios durante el render
    useEffect(() => {
        keycloak.login();
    }, [keycloak]);
    return <div className="text-center mt-5">Redirigiendo al Login...</div>;
}

// 2. Extraer roles del usuario desde el token
// Keycloak guarda los roles del Realm en: keycloak.tokenParsed.realm_access.roles
const userRoles = keycloak.tokenParsed?.realm_access?.roles || [];

// 3. Verificar si tiene el rol necesario (si se pidieron roles)
const hasRole = roles.length === 0 || roles.some(role => userRoles.includes(role));

if (!hasRole) {
    return (
    <div className="container mt-5 text-center">
        <h3 className="text-danger">Acceso Denegado</h3>
        <p>No tienes permisos para ver esta página.</p>
        <button className="btn btn-secondary" onClick={() => keycloak.logout()}>
            Cerrar Sesión
        </button>
    </div>
    );
}

// 4. Si pasa todo, muestra el contenido
return children;
};

export default PrivateRoute;