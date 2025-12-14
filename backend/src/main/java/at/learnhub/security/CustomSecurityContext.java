package at.learnhub.security;

import jakarta.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.List;

public class CustomSecurityContext implements SecurityContext {
    private final String username;
    private final List<String> roles;
    private final String fullName;
    private final String keycloakSub;

    public CustomSecurityContext(String username, List<String> roles, String fullName, String keycloakSub) {
        this.username = username;
        this.roles = roles;
        this.fullName = fullName;
        this.keycloakSub = keycloakSub;
    }

    @Override
    public Principal getUserPrincipal() {
        // Gib den Keycloak Sub zurück statt Username
        return () -> keycloakSub;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getKeycloakSub() {
        return keycloakSub;
    }

    @Override
    public boolean isUserInRole(String role) {
        return roles.contains(role);
    }

    @Override
    public boolean isSecure() {
        return true;
    }

    @Override
    public String getAuthenticationScheme() {
        return "Bearer";
    }
}