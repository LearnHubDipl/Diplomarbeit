package at.learnhub.boundary;

import at.learnhub.dto.NotficationDto;
import at.learnhub.security.CustomSecurityContext;
import at.learnhub.security.SecurityContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.io.File;
import java.security.Principal;
import java.util.*;

@Path("/api/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotificationResource {

    @Context
    SecurityContext securityContext;

    private final ObjectMapper mapper = new ObjectMapper();

    private CustomSecurityContext currentCscOrNull() {
        if (securityContext instanceof CustomSecurityContext csc) return csc;
        SecurityContext sc = SecurityContextHolder.getContext();
        if (sc instanceof CustomSecurityContext csc2) return csc2;
        return null;
    }

    private String currentSubOrNull() {
        CustomSecurityContext csc = currentCscOrNull();
        if (csc != null && csc.keycloakSub() != null && !csc.keycloakSub().isBlank()) return csc.keycloakSub();
        Principal p = (securityContext != null) ? securityContext.getUserPrincipal() : null;
        if (p == null) return null;
        return p.getName();
    }

    private String uploadBaseDir() {
        return System.getProperty("app.upload.dir", "/app/uploads");
    }

    private File notificationsDir() {
        File dir = new File(uploadBaseDir(), "notifications");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    private File notificationFile(String userSub) {
        return new File(notificationsDir(), userSub + ".json");
    }

    private List<NotficationDto> readNotifications(String userSub) {
        try {
            File f = notificationFile(userSub);
            if (!f.exists()) return new ArrayList<>();
            NotficationDto[] arr = mapper.readValue(f, NotficationDto[].class);
            return new ArrayList<>(Arrays.asList(arr));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void writeNotifications(String userSub, List<NotficationDto> list) throws Exception {
        File f = notificationFile(userSub);
        mapper.writerWithDefaultPrettyPrinter().writeValue(f, list);
    }

    @GET
    @Path("/me")
    public Response listMe() {
        String sub = currentSubOrNull();
        if (sub == null) return Response.status(Response.Status.UNAUTHORIZED).build();

        List<NotficationDto> list = readNotifications(sub);
        list.sort((a, b) -> Long.compare(b.createdAt, a.createdAt));
        return Response.ok(list).build();
    }

    @POST
    @Path("/{id}/read")
    public Response markRead(@PathParam("id") String id) {
        String sub = currentSubOrNull();
        if (sub == null) return Response.status(Response.Status.UNAUTHORIZED).build();

        try {
            List<NotficationDto> list = readNotifications(sub);
            for (NotficationDto n : list) {
                if (n != null && id.equals(n.id)) {
                    n.read = true;
                }
            }
            writeNotifications(sub, list);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.serverError().entity(Map.of("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/read-all")
    public Response markAllRead() {
        String sub = currentSubOrNull();
        if (sub == null) return Response.status(Response.Status.UNAUTHORIZED).build();

        try {
            List<NotficationDto> list = readNotifications(sub);
            for (NotficationDto n : list) {
                if (n != null) n.read = true;
            }
            writeNotifications(sub, list);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.serverError().entity(Map.of("error", e.getMessage())).build();
        }
    }
}
