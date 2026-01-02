import React from 'react';
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import App from './App.jsx';
import './index.css';
import keycloak from './keycloak';

const root = createRoot(document.getElementById('root'));

// Inicializamos Keycloak
keycloak.init({
  onLoad: 'login-required', // CAMBIO: Fuerza el login si no hay sesión
  pkceMethod: 'S256'
}).then((authenticated) => {
  
  if (authenticated) {
    console.log("Usuario autenticado. Token:", keycloak.token);
  } else {
    console.error("Fallo la autenticación");
  }

  root.render(
    <StrictMode>
      <App keycloak={keycloak} />
    </StrictMode>
  );

}).catch((error) => {
  console.error("Error al inicializar Keycloak:", error);
  root.render(<div className="container mt-5 text-danger">Error fatal: No se pudo conectar con Keycloak (Puerto 8090).</div>);
});