package at.learnhub.boundary;

import at.learnhub.model.User;
import at.learnhub.repository.UserRepository;
import at.learnhub.security.CustomSecurityContext;
import at.learnhub.security.SecurityContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.io.File;
import java.security.Principal;
import java.util.*;

@Path("/api/notes")
@Produces(MediaType.APPLICATION_JSON)
public class NotesPendingResource {

    @Context
    SecurityContext securityContext;

    @Inject
    UserRepository userRepository;

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
        String n = p.getName();
        return (n == null || n.isBlank()) ? null : n;
    }

    private User currentUserOrNull() {
        String sub = currentSubOrNull();
        if (sub == null) return null;
        return userRepository.findUserEntityByKeycloakSub(sub).orElse(null);
    }

    private String uploadBaseDir() {
        return System.getProperty("app.upload.dir", "/app/uploads");
    }

    private File topicNotesRoot() {
        return new File(uploadBaseDir(), "topic-notes");
    }

    private static String baseNameOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return (dot > 0) ? fileName.substring(0, dot) : fileName;
    }

    private static Long asLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(o)); } catch (Exception e) { return null; }
    }

    private static boolean asBool(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean b) return b;
        return Boolean.parseBoolean(String.valueOf(o));
    }

    @GET
    @Path("/pending")
    public Response listPendingNotes() {

        User me = currentUserOrNull();
        if (me == null) return Response.status(Response.Status.UNAUTHORIZED).build();
        if (Boolean.TRUE.equals(me.getAdmin())) {
            return Response.ok(Collections.emptyList()).build();
        }

        if (!Boolean.TRUE.equals(me.getTeacher())) {
            throw new WebApplicationException("Forbidden", Response.Status.FORBIDDEN);
        }

        Long myTeacherId = me.getId();

        File root = topicNotesRoot();
        if (!root.exists() || !root.isDirectory()) {
            return Response.ok(Collections.emptyList()).build();
        }

        List<Map<String, Object>> out = new ArrayList<>();

        File[] poolDirs = root.listFiles(File::isDirectory);
        if (poolDirs == null) return Response.ok(Collections.emptyList()).build();

        for (File poolDir : poolDirs) {
            Long poolId = asLong(poolDir.getName());
            if (poolId == null) continue;

            File[] files = poolDir.listFiles();
            if (files == null) continue;

            for (File f : files) {
                if (!f.isFile()) continue;
                String name = f.getName();
                if (!name.toLowerCase().endsWith(".pdf")) continue;

                String base = baseNameOf(name);
                File metaFile = new File(poolDir, base + ".meta.json");
                if (!metaFile.exists()) continue;

                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> meta = mapper.readValue(metaFile, Map.class);

                    boolean approved = asBool(meta.get("approved"));
                    if (approved) continue; // nur pending

                    Long teacherId = asLong(meta.get("teacherId"));
                    if (teacherId == null) continue;
                    
                    if (!teacherId.equals(myTeacherId)) continue;

                    Map<String, Object> dto = new LinkedHashMap<>();
                    dto.put("topicPoolId", poolId);
                    dto.put("fileName", name);
                    dto.putAll(meta);
                    dto.putIfAbsent("status", "PENDING");

                    out.add(dto);

                } catch (Exception ignore) {}
            }
        }

        out.sort((a, b) -> Long.compare(
                asLong(b.getOrDefault("createdAt", 0L)) != null ? asLong(b.getOrDefault("createdAt", 0L)) : 0L,
                asLong(a.getOrDefault("createdAt", 0L)) != null ? asLong(a.getOrDefault("createdAt", 0L)) : 0L
        ));

        return Response.ok(out).build();
    }
}
