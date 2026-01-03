package at.learnhub.boundary;

import at.learnhub.model.Teacher;
import at.learnhub.repository.TeacherRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Path("/api/notes")
@Produces(MediaType.APPLICATION_JSON)
public class PendingNotesResource {

    @Inject
    TeacherRepository teacherRepo;

    @Context
    SecurityContext securityContext;

    public static class PendingNoteDto {
        public Long topicPoolId;
        public String fileName;
        public String title;
        public String uploaderName;
        public String uploaderSub;
        public Long teacherId;
        public long createdAt;
        public String status;
        public boolean approved;
        public String publicUrl;
    }

    @GET
    @Path("/pending")
    public Response myPendingNotes() {

        if (!isTeacherOrAdmin()) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(java.util.Map.of("error", "Only teacher/admin can access pending notes"))
                    .build();
        }

        String email = currentEmailOrNull();
        if (email == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(java.util.Map.of("error", "No email in token / security context"))
                    .build();
        }

        Teacher me = findTeacherByEmail(email);
        if (me == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(java.util.Map.of("error", "No teacher found for email: " + email))
                    .build();
        }

        try {
            List<PendingNoteDto> out = new ArrayList<>();

            java.nio.file.Path base = java.nio.file.Paths.get(System.getProperty("app.upload.dir", "/app/uploads"));
            java.nio.file.Path topicNotesRoot = base.resolve(java.nio.file.Paths.get("topic-notes"));

            if (!java.nio.file.Files.exists(topicNotesRoot)) {
                return Response.ok(out).build();
            }

            final String baseUrl = System.getProperty("app.public.base", "https://vm91.htl-leonding.ac.at");

            try (java.util.stream.Stream<java.nio.file.Path> pools = java.nio.file.Files.list(topicNotesRoot)) {
                pools.filter(java.nio.file.Files::isDirectory).forEach(poolDir -> {
                    Long poolId = parseLongOrNull(poolDir.getFileName().toString());
                    if (poolId == null) return;

                    try (java.util.stream.Stream<java.nio.file.Path> files = java.nio.file.Files.list(poolDir)) {
                        files.filter(p -> java.nio.file.Files.isRegularFile(p)
                                        && p.getFileName().toString().toLowerCase().endsWith(".meta.json"))
                                .forEach(metaPath -> {
                                    try {
                                        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                        var node = mapper.readTree(metaPath.toFile());

                                        Long teacherId = node.hasNonNull("teacherId") ? node.get("teacherId").asLong() : null;
                                        String status = node.hasNonNull("status") ? node.get("status").asText() : null;
                                        boolean approved = node.hasNonNull("approved") && node.get("approved").asBoolean();

                                        if (teacherId == null || !teacherId.equals(me.getId())) return;

                                        boolean isPending = "PENDING".equalsIgnoreCase(status) || (!approved);
                                        if (!isPending) return;

                                        String fileName = node.hasNonNull("fileName") ? node.get("fileName").asText() : null;
                                        if (fileName == null || fileName.isBlank()) {
                                            // meta file name -> baseName + .pdf
                                            String metaFile = metaPath.getFileName().toString(); // xyz.meta.json
                                            String baseName = metaFile.replaceFirst("\\.meta\\.json$", "");
                                            fileName = baseName + ".pdf";
                                        }

                                        PendingNoteDto dto = new PendingNoteDto();
                                        dto.topicPoolId = poolId;
                                        dto.fileName = fileName;
                                        dto.title = node.hasNonNull("title") ? node.get("title").asText() : baseNameOf(fileName);
                                        dto.uploaderName = node.hasNonNull("uploaderName") ? node.get("uploaderName").asText() : null;
                                        dto.uploaderSub = node.hasNonNull("uploaderSub") ? node.get("uploaderSub").asText() : null;
                                        dto.teacherId = teacherId;
                                        dto.createdAt = node.hasNonNull("createdAt") ? node.get("createdAt").asLong() : 0L;
                                        dto.status = (status != null) ? status : (approved ? "APPROVED" : "PENDING");
                                        dto.approved = approved;

                                        dto.publicUrl = baseUrl + "/uploads/topic-notes/" + poolId + "/" + fileName;

                                        out.add(dto);
                                    } catch (Exception ignore) {
                                    }
                                });
                    } catch (Exception ignore) {
                    }
                });
            }

            // neueste zuerst
            out.sort((a, b) -> Long.compare(b.createdAt, a.createdAt));
            return Response.ok(out).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(java.util.Map.of("error", e.getMessage())).build();
        }
    }

    private boolean isTeacherOrAdmin() {
        try {
            if (securityContext == null) return false;
            return securityContext.isUserInRole("admin") || securityContext.isUserInRole("teacher");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Email aus Token holen:
     * - Wenn dein SecurityContext nur sub liefert, ist "email" evtl. nicht da.
     * - Dann musst du später wieder auf CustomSecurityContext umstellen.
     */
    private String currentEmailOrNull() {
        try {
            Principal p = securityContext != null ? securityContext.getUserPrincipal() : null;
            if (p == null) return null;
        } catch (Exception ignore) {}


        try {
            if (securityContext instanceof at.learnhub.security.CustomSecurityContext csc) {
                String mail = csc.email();
                return (mail != null && !mail.isBlank()) ? mail : null;
            }
        } catch (Exception ignore) {}

        return null;
    }

    private Teacher findTeacherByEmail(String email) {
        if (email == null) return null;
        return teacherRepo.listAll().stream()
                .filter(t -> t.getEmail() != null && t.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    private static Long parseLongOrNull(String s) {
        try { return Long.parseLong(s); } catch (Exception e) { return null; }
    }

    private static String baseNameOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return (dot > 0) ? fileName.substring(0, dot) : fileName;
    }
}
