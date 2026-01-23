package at.learnhub.security;

import jakarta.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.List;

/**
 * Custom Security Context that holds user information from Keycloak JWT token
 */
public record CustomSecurityContext(String username, List<String> roles, String fullName, String keycloakSub,
                                    String email, String givenName, String familyName,
                                    String distinguishedName) implements SecurityContext {
    public CustomSecurityContext(String username, List<String> roles, String fullName,
                                 String keycloakSub, String email, String givenName,
                                 String familyName, String distinguishedName) {
        this.username = username;
        this.roles = roles != null ? roles : List.of();
        this.fullName = fullName != null ? fullName : "";
        this.keycloakSub = keycloakSub != null ? keycloakSub : "";
        this.email = email != null ? email : "";
        this.givenName = givenName != null ? givenName : "";
        this.familyName = familyName != null ? familyName : "";
        this.distinguishedName = distinguishedName != null ? distinguishedName : "";
    }

    @Override
    public Principal getUserPrincipal() {
        return () -> keycloakSub;
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

    public String getClassName() {
        if (distinguishedName == null || distinguishedName.isEmpty()) {
            return "";
        }

        String[] parts = distinguishedName.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("OU=")) {
                String value = part.substring(3);
                if (value.matches(".*\\d.*") ||
                        value.matches(".*(HIF|HITM|HEL|HBG|FELA|CIF|BIFT|CIFT|ABIF|ACIF).*")) {
                    return value;
                }
            }
        }
        return "";
    }

    public boolean isStudent() {
        if (distinguishedName != null && !distinguishedName.isBlank()) {
            String dnUp = distinguishedName.toUpperCase();
            if (dnUp.contains("OU=STUDENTS") || dnUp.contains("STUDENTS")) {
                return true;
            }
        }
        if (email != null && !email.isBlank()) {
            String mail = email.toLowerCase().trim();
            if (mail.endsWith("@students.htl-leonding.ac.at")) {
                return true;
            }
        }

        return false;
    }


    /**public boolean isStudent() {
     return false;
     }**/


    public boolean isTeacher() {
        return !isStudent();
    }
}