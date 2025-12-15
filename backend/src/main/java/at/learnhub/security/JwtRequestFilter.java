package at.learnhub.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Priority;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;


@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtRequestFilter implements ContainerRequestFilter {

    @ConfigProperty(name = "keycloak.realm.public.key")
    String REALM_PUBLIC_KEY;

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        String method = requestContext.getMethod();

        System.out.println("\n[JwtRequestFilter] ========================================");
        System.out.println("[JwtRequestFilter] Request: " + method + " " + path);

        if ("OPTIONS".equalsIgnoreCase(method)) {
            System.out.println("[JwtRequestFilter] ✓ Skipping OPTIONS (CORS preflight)");
            return;
        }

        if (isPermitAll()) {
            System.out.println("[JwtRequestFilter] ✓ Endpoint has @PermitAll - skipping authentication");

            // Even for @PermitAll, we can still extract user info if token is provided
            String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring("Bearer ".length()).trim();
                    System.out.println("[JwtRequestFilter] Token found for @PermitAll endpoint");

                    // Verify token but don't require it
                    Algorithm algorithm = Algorithm.RSA256(getPublicKey(REALM_PUBLIC_KEY), null);
                    JWTVerifier verifier = JWT.require(algorithm).build();
                    DecodedJWT jwt = verifier.verify(token);

                    // Extract user information
                    String keycloakSub = jwt.getSubject();
                    String username = jwt.getClaim("preferred_username").asString();
                    String fullName = jwt.getClaim("name").asString();
                    String email = jwt.getClaim("email").asString();
                    String givenName = jwt.getClaim("given_name").asString();
                    String familyName = jwt.getClaim("family_name").asString();
                    String distinguishedName = jwt.getClaim("distinguishedName").asString();
                    List<String> userRoles = extractRoles(jwt);

                    // Create and set security context
                    CustomSecurityContext securityContext = new CustomSecurityContext(
                            username, userRoles, fullName, keycloakSub,
                            email, givenName, familyName, distinguishedName
                    );
                    requestContext.setSecurityContext(securityContext);

                    System.out.println("[JwtRequestFilter] ✓ Security context created for @PermitAll endpoint");
                } catch (Exception e) {
                    System.out.println("[JwtRequestFilter] Token verification failed for @PermitAll: " + e.getMessage());
                    // Don't abort for @PermitAll, just continue without security context
                }
            }
            return;
        }

        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null) {
            System.err.println("[JwtRequestFilter] ✗ No Authorization header found");
            System.err.println("[JwtRequestFilter] All headers: " + requestContext.getHeaders());
            abortWithUnauthorized(requestContext, "No authorization header provided");
            return;
        }

        System.out.println("[JwtRequestFilter] Authorization header found");

        if (!authHeader.startsWith("Bearer ")) {
            System.err.println("[JwtRequestFilter] ✗ Authorization header doesn't start with 'Bearer '");
            abortWithUnauthorized(requestContext, "Invalid authorization header format");
            return;
        }

        String token = authHeader.substring("Bearer ".length()).trim();
        System.out.println("[JwtRequestFilter] Token extracted, length: " + token.length());

        if (token.isEmpty()) {
            System.err.println("[JwtRequestFilter] ✗ Token is empty after extraction");
            abortWithUnauthorized(requestContext, "Empty token provided");
            return;
        }

        try {
            System.out.println("[JwtRequestFilter] Verifying token with Keycloak public key...");
            Algorithm algorithm = Algorithm.RSA256(getPublicKey(REALM_PUBLIC_KEY), null);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);

            System.out.println("[JwtRequestFilter] ✓ Token verified successfully");

            String keycloakSub = jwt.getSubject();
            String username = jwt.getClaim("preferred_username").asString();
            String fullName = jwt.getClaim("name").asString();
            String email = jwt.getClaim("email").asString();
            String givenName = jwt.getClaim("given_name").asString();
            String familyName = jwt.getClaim("family_name").asString();
            String distinguishedName = jwt.getClaim("distinguishedName").asString();
            List<String> userRoles = extractRoles(jwt);

            System.out.println("[JwtRequestFilter] User Details:");
            System.out.println("  - Username: " + username);
            System.out.println("  - Keycloak Sub: " + keycloakSub);
            System.out.println("  - Full Name: " + fullName);
            System.out.println("  - Email: " + email);
            System.out.println("  - Roles: " + userRoles);

            Set<String> requiredRoles = getRolesAllowed();
            if (!requiredRoles.isEmpty()) {
                System.out.println("[JwtRequestFilter] Required roles: " + requiredRoles);
                if (Collections.disjoint(userRoles, requiredRoles)) {
                    System.err.println("[JwtRequestFilter] ✗ User doesn't have required roles");
                    System.err.println("[JwtRequestFilter] User has: " + userRoles);
                    System.err.println("[JwtRequestFilter] Needs one of: " + requiredRoles);
                    abortWithUnauthorized(requestContext, "Insufficient permissions");
                    return;
                }
                System.out.println("[JwtRequestFilter] ✓ Role check passed");
            }

            CustomSecurityContext securityContext = new CustomSecurityContext(
                    username, userRoles, fullName, keycloakSub,
                    email, givenName, familyName, distinguishedName
            );

            requestContext.setSecurityContext(securityContext);

            System.out.println("[JwtRequestFilter] ✓ Security context set successfully");
            System.out.println("[JwtRequestFilter] ========================================\n");

        } catch (JWTVerificationException e) {
            System.err.println("[JwtRequestFilter] ✗ Token verification failed");
            System.err.println("[JwtRequestFilter] Error: " + e.getMessage());
            abortWithUnauthorized(requestContext, "Invalid token: " + e.getMessage());
        } catch (GeneralSecurityException e) {
            System.err.println("[JwtRequestFilter] ✗ Public key error");
            System.err.println("[JwtRequestFilter] Error: " + e.getMessage());
            abortWithUnauthorized(requestContext, "Server configuration error");
        } catch (Exception e) {
            System.err.println("[JwtRequestFilter] ✗ Unexpected error");
            System.err.println("[JwtRequestFilter] Error: " + e.getClass().getSimpleName());
            System.err.println("[JwtRequestFilter] Message: " + e.getMessage());
            abortWithUnauthorized(requestContext, "Authentication error: " + e.getMessage());
        }
    }

    private boolean isPermitAll() {
        boolean methodPermitAll = resourceInfo.getResourceMethod().isAnnotationPresent(PermitAll.class);
        boolean classPermitAll = resourceInfo.getResourceClass().isAnnotationPresent(PermitAll.class);

        System.out.println("[JwtRequestFilter] Method has @PermitAll: " + methodPermitAll);
        System.out.println("[JwtRequestFilter] Class has @PermitAll: " + classPermitAll);

        return methodPermitAll || classPermitAll;
    }

    private Set<String> getRolesAllowed() {
        RolesAllowed rolesAnnotation = resourceInfo.getResourceMethod().getAnnotation(RolesAllowed.class);
        if (rolesAnnotation == null) {
            rolesAnnotation = resourceInfo.getResourceClass().getAnnotation(RolesAllowed.class);
        }
        return rolesAnnotation != null
                ? new HashSet<>(Arrays.asList(rolesAnnotation.value()))
                : Collections.emptySet();
    }

    private List<String> extractRoles(DecodedJWT jwt) {
        try {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access").asMap();
            if (realmAccess != null && realmAccess.get("roles") instanceof List<?> rawRoles) {
                return rawRoles.stream()
                        .filter(Objects::nonNull)
                        .map(String::valueOf)
                        .toList();
            }
        } catch (Exception e) {
            System.err.println("[JwtRequestFilter] Error extracting roles: " + e.getMessage());
        }
        return List.of();
    }

    private void abortWithUnauthorized(ContainerRequestContext context, String message) {
        System.err.println("[JwtRequestFilter] Aborting with 401: " + message);
        context.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of(
                                "error", "Unauthorized",
                                "message", message,
                                "timestamp", new Date().toString()
                        ))
                        .build()
        );
    }

    private RSAPublicKey getPublicKey(String base64PublicKey) throws GeneralSecurityException {
        byte[] keyBytes = Base64.getDecoder().decode(base64PublicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) factory.generatePublic(spec);
    }
}