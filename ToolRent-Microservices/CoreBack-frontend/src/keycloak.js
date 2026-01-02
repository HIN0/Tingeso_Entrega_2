import Keycloak from "keycloak-js";

const keycloakConfig = {
  url: "http://localhost:8090", // La URL de tu Docker Keycloak
  realm: "toolrent-realm",       // El nombre exacto que pusiste al Realm
  clientId: "toolrent-frontend", // El nombre exacto de tu Cliente
};

const keycloak = new Keycloak(keycloakConfig);

export default keycloak;