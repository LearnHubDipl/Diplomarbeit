package at.learnhub.boundary;

import at.learnhub.dto.simple.UserSlimDto;
import at.learnhub.repository.UserRepository;
import at.learnhub.service.UserService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserRepository userRepository;

    @Inject
    UserService userService;

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
            @APIResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    public Response getUserById(
            @Parameter(
                    description = "ID of the user to be fetched",
                    required = true
            )
            @PathParam("id") Long id
    ) {
        UserSlimDto user = userService.getUserById(id);
        return Response.ok(user).build();
    }

    @POST
    @Path("/register")
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
            @APIResponse(
                    responseCode = "401",
                    description = "Unauthorized - no valid token provided"
            )
    })
    public Response registerUser(@Context HttpHeaders headers) {
        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);

        System.out.println("[Backend] Received Authorization header: " +
                (authHeader != null ? authHeader.substring(0, Math.min(50, authHeader.length())) + "..." : "null"));

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("[Backend] No valid authorization token provided");
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("No valid authorization token provided")
                    .build();
        }

        String token = authHeader.substring("Bearer ".length()).trim();
        System.out.println("[Backend] Extracted token length: " + token.length());
        System.out.println("[Backend] Token preview: " + token.substring(0, Math.min(50, token.length())) + "...");

        if (token.isEmpty()) {
            System.out.println("[Backend] Token is empty after extraction");
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Empty token provided")
                    .build();
        }

        try {
            UserSlimDto user = userService.findOrCreateUser(token);
            System.out.println("[Backend] User successfully registered/retrieved: " + user.keycloakSub());
            return Response.ok(user).build();
        } catch (Exception e) {
            System.err.println("[Backend] Error processing token: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error processing token: " + e.getMessage())
                    .build();
        }
    }

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
            )
    })
    public Response getAllUsers() {
        return Response.ok(userRepository.findAll()).build();
    }

    @GET
    @Path("/teachers")
    @Operation(
            summary = "Get all teachers",
            description = "Retrieve a list of all users who are teachers"
    )
    public Response getAllTeachers() {
        return Response.ok(userRepository.findAllTeachers()).build();
    }

    @GET
    @Path("/keycloak/{keycloakSub}")
    @Operation(
            summary = "Get a user by Keycloak sub",
            description = "Get a user based on their Keycloak sub"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "User found"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    public Response getUserByKeycloakSub(
            @Parameter(
                    description = "Keycloak sub of the user",
                    required = true
            )
            @PathParam("keycloakSub") String keycloakSub
    ) {
        return userService.getUserByKeycloakSub(keycloakSub)
                .map(user -> Response.ok(user).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
}