package at.learnhub.boundary;

import at.learnhub.dto.simple.UserSlimDto;
import at.learnhub.repository.UserRepository;
import at.learnhub.security.CustomSecurityContext;
import at.learnhub.service.UserService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.security.Principal;
import java.util.Map;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@SecurityRequirement(name = "BearerAuth")
@SecurityScheme(
        securitySchemeName = "BearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class UserResource {

    @Inject
    UserRepository userRepository;

    @Inject
    UserService userService;

    /**
     * Helper method to extract CustomSecurityContext from proxy
     */
    private CustomSecurityContext extractCustomSecurityContext(SecurityContext securityContext) {
        if (securityContext == null) {
            return null;
        }

        if (securityContext instanceof CustomSecurityContext) {
            return (CustomSecurityContext) securityContext;
        }

        if (Proxy.isProxyClass(securityContext.getClass())) {
            try {
                InvocationHandler handler = Proxy.getInvocationHandler(securityContext);
                if (handler instanceof jakarta.ws.rs.core.SecurityContext) {
                    return extractCustomSecurityContext((SecurityContext) handler);
                }
            } catch (Exception e) {
                System.err.println("[UserResource] Error extracting from proxy: " + e.getMessage());
            }
        }

        return null;
    }

    /**
     * Get current logged-in user
     * THIS is what your frontend calls!
     */
    @GET
    @Path("/me")
    @Operation(
            summary = "Get current user",
            description = "Get the currently authenticated user's information"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Current user",
                    content = @Content(schema = @Schema(implementation = UserSlimDto.class))
            ),
            @APIResponse(responseCode = "401", description = "Unauthorized"),
            @APIResponse(responseCode = "404", description = "User not found in database")
    })
    public Response getCurrentUser(@Context SecurityContext securityContext) {
        //System.out.println("\n[UserResource /me] ===================");
        //System.out.println("[UserResource /me] Processing request");

        if (securityContext == null) {
            System.err.println("[UserResource /me] SecurityContext is NULL!");
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "No security context"))
                    .build();
        }

        /**
         System.out.println("[UserResource /me] SecurityContext type: " +
         securityContext.getClass().getName());

         System.out.println("[UserResource /me] User principal: " +
         (securityContext.getUserPrincipal() != null ?
         securityContext.getUserPrincipal().getName() : "null"));
         **/
        CustomSecurityContext customContext = extractCustomSecurityContext(securityContext);

        if (customContext == null) {
            System.err.println("[UserResource /me] Could not extract CustomSecurityContext");
            Principal principal = securityContext.getUserPrincipal();
            if (principal != null) {
                String keycloakSub = principal.getName();
                //System.out.println("[UserResource /me] Using principal name as keycloakSub: " + keycloakSub);

                return userService.getUserByKeycloakSub(keycloakSub)
                        .map(user -> {
                            //System.out.println("[UserResource /me]  User found via principal: ID=" + user.id());
                            return Response.ok(user).build();
                        })
                        .orElseGet(() -> {
                            System.err.println("[UserResource /me] User not in database!");
                            return Response.status(Response.Status.NOT_FOUND)
                                    .entity(Map.of(
                                            "error", "User not found",
                                            "message", "Please call POST /api/users/register first",
                                            "keycloakSub", keycloakSub
                                    ))
                                    .build();
                        });
            } else {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "No user principal found"))
                        .build();
            }
        }

        String keycloakSub = customContext.getKeycloakSub();
        //System.out.println("[UserResource /me] Keycloak Sub: " + keycloakSub);
        //System.out.println("[UserResource /me] Username: " + customContext.getUsername());

        return userService.getUserByKeycloakSub(keycloakSub)
                .map(user -> {
                    System.out.println("[UserResource /me] User found: ID=" + user.id());
                    return Response.ok(user).build();
                })
                .orElseGet(() -> {
                    //System.err.println("[UserResource /me] User not in database!");
                    //System.err.println("[UserResource /me] User needs to call /register first");
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity(Map.of(
                                    "error", "User not found",
                                    "message", "Please call POST /api/users/register first",
                                    "keycloakSub", keycloakSub
                            ))
                            .build();
                });
    }

    /**
     * Register or get current user
     * Call this ONCE after login to create user in database
     */
    @POST
    @Path("/register")
    @PermitAll
    @Operation(
            summary = "Register or retrieve user",
            description = "Automatically registers a new user or retrieves existing user based on Keycloak token"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "User retrieved or created",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = UserSlimDto.class)
                    )
            ),
            @APIResponse(responseCode = "401", description = "Unauthorized")
    })
    public Response registerUser(@Context SecurityContext securityContext) {
        //System.out.println("[UserResource /register] Processing request");

        if (securityContext == null) {
            System.err.println("[UserResource /register] SecurityContext is NULL!");
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "No security context"))
                    .build();
        }

        /**
         System.out.println("[UserResource /register] SecurityContext type: " +
         securityContext.getClass().getName());

         System.out.println("[UserResource /register] User principal: " +
         (securityContext.getUserPrincipal() != null ?
         securityContext.getUserPrincipal().getName() : "null"));
         **/
        CustomSecurityContext customContext = extractCustomSecurityContext(securityContext);

        if (customContext == null) {
            System.err.println("[UserResource /register] Could not extract CustomSecurityContext");
            Principal principal = securityContext.getUserPrincipal();
            if (principal != null) {
                String keycloakSub = principal.getName();
                // System.out.println("[UserResource /register] Using principal name as keycloakSub: " + keycloakSub);

                customContext = new CustomSecurityContext(
                        keycloakSub,
                        java.util.Collections.emptyList(),
                        keycloakSub,
                        keycloakSub,
                        "",
                        "",
                        "",
                        ""
                );
            } else {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "No user principal found"))
                        .build();
            }
        }

        String keycloakSub = customContext.getKeycloakSub();
        /**
         System.out.println("[UserResource /register] Keycloak Sub: " + keycloakSub);
         System.out.println("[UserResource /register] Username: " + customContext.getUsername());
         System.out.println("[UserResource /register] Email: " + customContext.getEmail());
         System.out.println("[UserResource /register] Roles: " + customContext.getRoles());
         **/
        try {
            UserSlimDto user = userService.findOrCreateUserFromContext(customContext);
            //System.out.println("[UserResource /register] Success: ID=" + user.id());
            return Response.ok(user).build();
        } catch (Exception e) {
            System.err.println("[UserResource /register] Error: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Registration failed", "message", e.getMessage()))
                    .build();
        }
    }

    /**
     * Get user by ID
     */
    @GET
    @Path("/{id}")
    @Operation(
            summary = "Get a user by ID",
            description = "Get a user based on the provided ID"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = UserSlimDto.class)
                    )
            ),
            @APIResponse(responseCode = "401", description = "Unauthorized"),
            @APIResponse(responseCode = "404", description = "User not found")
    })
    public Response getUserById(@PathParam("id") Long id) {
        try {
            UserSlimDto user = userService.getUserById(id);
            return Response.ok(user).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "User not found", "id", id))
                    .build();
        }
    }

    /**
     * Get all users
     */
    @GET
    @Operation(
            summary = "Get all users",
            description = "Retrieve a list of all registered users"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "List of users",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = UserSlimDto.class)
                    )
            ),
            @APIResponse(responseCode = "401", description = "Unauthorized")
    })
    public Response getAllUsers() {
        //System.out.println("[UserResource /users] Getting all users");
        return Response.ok(userRepository.findAll()).build();
    }

    /**
     * Get all teachers
     */
    @GET
    @Path("/teachers")
    @Operation(
            summary = "Get all teachers",
            description = "Retrieve a list of all users who are teachers"
    )
    @APIResponses({
            @APIResponse(responseCode = "200", description = "List of teachers"),
            @APIResponse(responseCode = "401", description = "Unauthorized")
    })
    public Response getAllTeachers() {
        return Response.ok(userRepository.findAllTeachers()).build();
    }

    /**
     * Get user by Keycloak sub
     */
    @GET
    @Path("/keycloak/{keycloakSub}")
    @Operation(
            summary = "Get a user by Keycloak sub",
            description = "Get a user based on their Keycloak sub"
    )
    @APIResponses({
            @APIResponse(responseCode = "200", description = "User found"),
            @APIResponse(responseCode = "401", description = "Unauthorized"),
            @APIResponse(responseCode = "404", description = "User not found")
    })
    public Response getUserByKeycloakSub(@PathParam("keycloakSub") String keycloakSub) {
        return userService.getUserByKeycloakSub(keycloakSub)
                .map(user -> Response.ok(user).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "User not found", "keycloakSub", keycloakSub))
                        .build());
    }

    /**
     * Admin-only: Delete user
     */
    @DELETE
    @Path("/{id}")
    @RolesAllowed({"admin"})
    @Operation(
            summary = "Delete a user",
            description = "Delete a user by ID. Admin only."
    )
    @APIResponses({
            @APIResponse(responseCode = "204", description = "User deleted"),
            @APIResponse(responseCode = "401", description = "Unauthorized"),
            @APIResponse(responseCode = "403", description = "Forbidden"),
            @APIResponse(responseCode = "404", description = "User not found")
    })
    public Response deleteUser(@PathParam("id") Long id) {
        return Response.noContent().build();
    }
}