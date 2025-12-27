package at.learnhub.service;

import at.learnhub.dto.request.UserCreateDto;
import at.learnhub.dto.simple.UserSlimDto;
import at.learnhub.mapper.UserMapper;
import at.learnhub.model.User;
import at.learnhub.repository.UserRepository;
import at.learnhub.security.CustomSecurityContext;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Optional;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    /**
     * Finds or creates a user based on Keycloak token.
     * This is called automatically when a user logs in.
     *
     * @param token the JWT token from Keycloak
     * @return the UserSlimDto
     */
    @Transactional
    public UserSlimDto findOrCreateUser(String token) {
        DecodedJWT jwt = JWT.decode(token);
        String keycloakSub = jwt.getSubject();

        // Check if user already exists
        Optional<UserSlimDto> existingUser = userRepository.findByKeycloakSub(keycloakSub);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        // Extract user data from token
        UserCreateDto dto = extractUserDataFromToken(jwt);

        // Create new user
        User newUser = UserMapper.toEntity(dto);
        User savedUser = userRepository.createUser(newUser);

        return UserMapper.toSlimDto(savedUser);
    }

    /**
     * Finds or creates a user based on CustomSecurityContext.
     * This is the recommended method as the token is already validated.
     */
    @Transactional
    public UserSlimDto findOrCreateUserFromContext(CustomSecurityContext context) {
        String keycloakSub = context.getKeycloakSub();

        // System.out.println("[UserService] Looking for user with keycloakSub: " + keycloakSub);

        // Check if user already exists
        Optional<UserSlimDto> existingUser = userRepository.findByKeycloakSub(keycloakSub);
        if (existingUser.isPresent()) {
            //System.out.println("[UserService] User exists: " + keycloakSub);
            return existingUser.get();
        }

        //System.out.println("[UserService] Creating new user: " + keycloakSub);

        // Create DTO from context
        UserCreateDto dto = new UserCreateDto(
                keycloakSub,
                context.getFullName(),
                context.getEmail(),
                context.getUsername(),
                context.getGivenName(),
                context.getFamilyName(),
                context.getClassName(),
                context.isTeacher()
        );

        // Create new user
        User newUser = UserMapper.toEntity(dto);
        User savedUser = userRepository.createUser(newUser);

        //System.out.println("[UserService] User created with ID: " + savedUser.getId());

        return UserMapper.toSlimDto(savedUser);
    }

    /**
     * Extracts user data from JWT token.
     */
    private UserCreateDto extractUserDataFromToken(DecodedJWT jwt) {
        String keycloakSub = jwt.getSubject();
        String name = jwt.getClaim("name").asString();
        String email = jwt.getClaim("email").asString();
        String username = jwt.getClaim("preferred_username").asString();
        String givenName = jwt.getClaim("given_name").asString();
        String familyName = jwt.getClaim("family_name").asString();
        String distinguishedName = jwt.getClaim("distinguishedName").asString();

        // Extract class from DN
        String className = extractClassFromDN(distinguishedName);

        // Determine if user is student
        Boolean isStudent = isStudentFromDN(distinguishedName);
        Boolean isTeacher = !isStudent;

        return new UserCreateDto(
                keycloakSub,
                name,
                email,
                username,
                givenName,
                familyName,
                className,
                isTeacher
        );
    }

    /**
     * Extracts class name from distinguished name.
     * Example: CN=it210181,OU=5AHITM,... -> "5AHITM"
     */
    private String extractClassFromDN(String dn) {
        if (dn == null || dn.isEmpty()) {
            return "";
        }

        // Split by comma and find OU entries
        String[] parts = dn.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("OU=")) {
                String value = part.substring(3);
                // Check if it contains numbers or class identifiers
                if (value.matches(".*\\d.*") ||
                        value.matches(".*(HIF|HITM|HEL|HBG|FELA|CIF|BIFT|CIFT|ABIF|ACIF).*")) {
                    return value;
                }
            }
        }

        return "";
    }

    /**
     * Checks if user is a student based on DN.
     */
    private Boolean isStudentFromDN(String dn) {
        if (dn == null || dn.isEmpty()) {
            return false;
        }
        return dn.toUpperCase().contains("OU=STUDENTS");
    }

    /**
     * Gets a user by ID.
     */
    public UserSlimDto getUserById(Long id) {
        return userRepository.getUserSlimDtoById(id);
    }

    /**
     * Gets a user by Keycloak sub.
     */
    public Optional<UserSlimDto> getUserByKeycloakSub(String keycloakSub) {
        return userRepository.findByKeycloakSub(keycloakSub);
    }
}