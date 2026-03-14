package at.learnhub.boundary;

import at.learnhub.dto.NotficationDto;
import at.learnhub.model.User;
import at.learnhub.repository.UserRepository;
import at.learnhub.security.CustomSecurityContext;
import at.learnhub.security.SecurityContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import java.io.*;
import java.security.Principal;
import java.util.*;

@Path("/api/topic-pools/{topicPoolId}/notes")
@Produces(MediaType.APPLICATION_JSON)
public class TopicNotesResource {

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
        try {
            CustomSecurityContext csc = currentCscOrNull();
            if (csc != null) {
                String sub = csc.keycloakSub();
                return (sub != null && !sub.isBlank()) ? sub : null;
            }
            Principal p = (securityContext != null) ? securityContext.getUserPrincipal() : null;
            if (p == null) return null;
            String n = p.getName();
            return (n != null && !n.isBlank()) ? n : null;
        } catch (Exception e) {
            return null;
        }
    }

    private User currentUserOrNull() {
        String sub = currentSubOrNull();
        if (sub == null) return null;
        return userRepository.findUserEntityByKeycloakSub(sub).orElse(null);
    }

    private boolean isTeacherOrAdminDb() {
        User me = currentUserOrNull();
        if (me == null) return false;
        return Boolean.TRUE.equals(me.getTeacher()) || Boolean.TRUE.equals(me.getAdmin());
    }

    private boolean isAdminDb() {
        User me = currentUserOrNull();
        if (me == null) return false;
        return Boolean.TRUE.equals(me.getAdmin());
    }

    private void requireTeacherOrAdminDb() {
        if (!isTeacherOrAdminDb()) {
            throw new WebApplicationException(
                    "Du hast keine Berechtigung, diese Aktion auszuführen.",
                    Response.Status.FORBIDDEN
            );
        }
    }

    private String currentFullNameOrFallback() {
        try {
            User me = currentUserOrNull();
            if (me != null && me.getName() != null && !me.getName().isBlank()) {
                return me.getName();
            }
            CustomSecurityContext csc = currentCscOrNull();
            if (csc != null) {
                String n = csc.fullName();
                if (n != null && !n.isBlank()) return n;
                if (csc.givenName() != null && !csc.givenName().isBlank()) return csc.givenName();
            }
        } catch (Exception ignore) {}
        return "Lehrkraft";
    }

    private String uploadBaseDir() {
        return System.getProperty("app.upload.dir", "/app/uploads");
    }

    private String publicBase() {
        return System.getProperty("app.public.base", "https://vm91.htl-leonding.ac.at");
    }

    private File poolDir(long topicPoolId) {
        File dir = new File(uploadBaseDir(), "topic-notes" + File.separator + topicPoolId);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    private static String safeFileName(String name) {
        if (name == null) return "upload.pdf";
        name = name.replace("\\", "/");
        name = name.substring(name.lastIndexOf('/') + 1);
        return name;
    }

    private static String baseNameOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return (dot > 0) ? fileName.substring(0, dot) : fileName;
    }

    private static boolean endsWithPdf(String fileName) {
        return fileName != null && fileName.toLowerCase().endsWith(".pdf");
    }

    private static long lastModified(File f) {
        return (f != null) ? f.lastModified() : 0L;
    }

    private static long size(File f) {
        return (f != null) ? f.length() : 0L;
    }

    private Map<String, Object> readMeta(File dir, String pdfFileName, long fallbackCreatedAt) {
        Map<String, Object> meta = new LinkedHashMap<>();
        String base = baseNameOf(pdfFileName);
        File metaFile = new File(dir, base + ".meta.json");

        if (metaFile.exists()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> read = mapper.readValue(metaFile, Map.class);
                meta.putAll(read);
            } catch (Exception ignore) {}
        }

        meta.putIfAbsent("fileName", pdfFileName);
        meta.putIfAbsent("createdAt", fallbackCreatedAt);
        meta.putIfAbsent("approved", Boolean.FALSE);
        meta.putIfAbsent("status", Boolean.TRUE.equals(meta.get("approved")) ? "APPROVED" : "PENDING");

        return meta;
    }

    private void writeMeta(File dir, String pdfFileName, Map<String, Object> meta) throws Exception {
        String base = baseNameOf(pdfFileName);
        File metaFile = new File(dir, base + ".meta.json");
        mapper.writerWithDefaultPrettyPrinter().writeValue(metaFile, meta);
    }

    private boolean isApproved(Map<String, Object> meta) {
        Object a = meta.get("approved");
        if (a instanceof Boolean b) return b;
        if (a instanceof String s) return Boolean.parseBoolean(s);
        return false;
    }

    private boolean isOwner(Map<String, Object> meta) {
        String me = currentSubOrNull();
        Object os = meta.get("uploaderSub");
        String owner = (os != null) ? String.valueOf(os) : null;
        return me != null && owner != null && me.equals(owner);
    }

    private void requireCanEdit(Map<String, Object> meta) {
        if (isTeacherOrAdminDb()) return;
        if (!(isOwner(meta) && !isApproved(meta))) {
            throw new WebApplicationException(
                    "Du hast keine Berechtigung, diese Aktion auszuführen.",
                    Response.Status.FORBIDDEN
            );
        }
    }

    private void requireCanDelete(Map<String, Object> meta) {
        if (isTeacherOrAdminDb()) return;
        if (!isOwner(meta)) {
            throw new WebApplicationException(
                    "Du hast keine Berechtigung, diese Aktion auszuführen.",
                    Response.Status.FORBIDDEN
            );
        }
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

    private void pushNotificationToUserSub(String userSub, String type, String title, String message, Map<String, Object> meta) {
        if (userSub == null || userSub.isBlank()) return;
        try {
            List<NotficationDto> list = readNotifications(userSub);
            NotficationDto n = new NotficationDto();
            n.id = UUID.randomUUID().toString();
            n.type = type;
            n.title = title;
            n.message = message;
            n.createdAt = System.currentTimeMillis();
            n.read = false;
            n.meta = meta;
            list.add(n);
            writeNotifications(userSub, list);
        } catch (Exception e) {
            System.err.println("[TopicNotesResource] Could not write notification: " + e.getMessage());
        }
    }

    @GET
    public Response list(@PathParam("topicPoolId") Long topicPoolId) {
        try {
            File dir = poolDir(topicPoolId);
            File[] files = dir.listFiles();
            if (files == null) return Response.ok(Collections.emptyList()).build();

            boolean teacherOrAdmin = isTeacherOrAdminDb();
            List<Map<String, Object>> out = new ArrayList<>();

            for (File f : files) {
                if (!f.isFile()) continue;
                if (!endsWithPdf(f.getName())) continue;

                String fileName = f.getName();
                long lm = lastModified(f);
                Map<String, Object> meta = readMeta(dir, fileName, lm);
                boolean approved = isApproved(meta);

                if (!approved && !(teacherOrAdmin || isOwner(meta))) continue;

                String url = publicBase() + "/uploads/topic-notes/" + topicPoolId + "/" + fileName;

                Map<String, Object> dto = new LinkedHashMap<>();
                dto.put("topicPoolId", topicPoolId);
                dto.put("fileName", fileName);
                dto.put("pdfUrl", url);
                dto.put("publicUrl", url);
                dto.put("size", size(f));
                dto.put("lastModified", lm);
                dto.putAll(meta);
                dto.put("approved", approved);
                dto.put("status", meta.getOrDefault("status", approved ? "APPROVED" : "PENDING"));
                dto.put("canEdit", teacherOrAdmin || (isOwner(meta) && !approved));
                dto.put("canDelete", teacherOrAdmin || isOwner(meta));
                out.add(dto);
            }

            out.sort((a, b) -> Long.compare(
                    ((Number) b.getOrDefault("lastModified", 0)).longValue(),
                    ((Number) a.getOrDefault("lastModified", 0)).longValue()
            ));

            return Response.ok(out).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(Map.of("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upload(@PathParam("topicPoolId") long topicPoolId, MultipartFormDataInput input) {
        try {
            if (input == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Missing multipart body")).build();
            }

            Map<String, List<InputPart>> map = input.getFormDataMap();
            String replaceFileName = trimOrNull(field(map, "replaceFileName"));
            boolean isReplace = replaceFileName != null;

            InputPart filePart = first(map, "file");

            if (filePart == null && !isReplace) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Missing file")).build();
            }

            File dir = poolDir(topicPoolId);
            String targetFileName;

            if (isReplace) {
                targetFileName = safeFileName(replaceFileName);
                Map<String, Object> oldMeta = readMeta(dir, targetFileName, System.currentTimeMillis());
                requireCanEdit(oldMeta);
            } else {
                targetFileName = safeFileName(filename(filePart));
                File target = new File(dir, targetFileName);
                if (target.exists()) {
                    String base = baseNameOf(targetFileName);
                    String ext = targetFileName.contains(".")
                            ? targetFileName.substring(targetFileName.lastIndexOf('.'))
                            : "";
                    targetFileName = base + "-" + System.currentTimeMillis() + ext;
                }
            }

            if (filePart != null) {
                File targetFile = new File(dir, targetFileName);
                try (InputStream in = filePart.getBody(InputStream.class, null);
                     OutputStream out = new FileOutputStream(targetFile)) {
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                }
            }

            String title = trimOrNull(field(map, "title"));
            String description = trimOrNull(field(map, "description"));
            String uploaderName = trimOrNull(field(map, "uploaderName"));
            String teacherEmail = trimOrNull(field(map, "teacherEmail"));

            boolean teacherOrAdmin = isTeacherOrAdminDb();
            String sub = currentSubOrNull();

            Map<String, Object> meta;
            if (isReplace) {
                meta = readMeta(dir, targetFileName, System.currentTimeMillis());
            } else {
                meta = new LinkedHashMap<>();
            }

            boolean approved;
            String status;

            if (teacherOrAdmin) {
                approved = true;
                status = "APPROVED";
                // Lehrer-Upload → approvedByName sofort setzen
                meta.put("approvedByName", currentFullNameOrFallback());
            } else {
                if (teacherEmail == null && !isReplace) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(Map.of("error", "teacherEmail is required for student uploads")).build();
                }
                approved = isReplace ? isApproved(meta) : false;
                status = isReplace ? (String) meta.getOrDefault("status", "PENDING") : "PENDING";
            }

            long createdAt = isReplace
                    ? ((Number) meta.getOrDefault("createdAt", System.currentTimeMillis())).longValue()
                    : System.currentTimeMillis();

            meta.put("fileName", targetFileName);
            meta.put("title", (title != null) ? title : baseNameOf(targetFileName));
            meta.put("description", description);
            meta.put("uploaderName", uploaderName);
            meta.put("uploaderSub", sub);
            meta.put("createdAt", createdAt);
            meta.put("approved", approved);
            meta.put("status", status);

            if (!teacherOrAdmin && teacherEmail != null) {
                meta.put("teacherEmail", teacherEmail);
                try {
                    userRepository.findByEmail(teacherEmail).ifPresent(teacherDto -> {});
                    User teacher = userRepository.findUserEntityByEmail(teacherEmail);
                    if (teacher != null && teacher.getKeycloakSub() != null) {
                        meta.put("teacherSub", teacher.getKeycloakSub());
                        if (!isReplace) {
                            String noteTitle = (String) meta.getOrDefault("title", targetFileName);
                            pushNotificationToUserSub(
                                    teacher.getKeycloakSub(),
                                    "NOTE_PENDING",
                                    "Neue Mitschrift wartet",
                                    "Eine Mitschrift '" + noteTitle + "' wartet auf deine Freigabe.",
                                    Map.of("topicPoolId", topicPoolId, "fileName", targetFileName, "status", "PENDING")
                            );
                        }
                    }
                } catch (Exception ignore) {}
            }

            writeMeta(dir, targetFileName, meta);

            String url = publicBase() + "/uploads/topic-notes/" + topicPoolId + "/" + targetFileName;

            return Response.ok(Map.of(
                    "topicPoolId", topicPoolId,
                    "fileName", targetFileName,
                    "publicUrl", url,
                    "approved", approved,
                    "status", status
            )).build();

        } catch (WebApplicationException wex) {
            throw wex;
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(Map.of("error", e.getMessage())).build();
        }
    }

    @DELETE
    @Path("/{fileName}")
    public Response delete(@PathParam("topicPoolId") Long topicPoolId,
                           @PathParam("fileName") String fileName) {
        try {
            File dir = poolDir(topicPoolId);
            String safe = safeFileName(fileName);
            Map<String, Object> meta = readMeta(dir, safe, System.currentTimeMillis());
            requireCanDelete(meta);

            File pdf = new File(dir, safe);
            File metaFile = new File(dir, baseNameOf(safe) + ".meta.json");

            boolean existed = false;
            if (pdf.exists()) { existed = pdf.delete(); }
            if (metaFile.exists()) { metaFile.delete(); }

            return existed ? Response.noContent().build()
                    : Response.status(Response.Status.NOT_FOUND).build();

        } catch (WebApplicationException wex) {
            throw wex;
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(Map.of("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/{fileName}/approve")
    public Response approve(@PathParam("topicPoolId") Long topicPoolId,
                            @PathParam("fileName") String fileName) {
        requireTeacherOrAdminDb();
        try {
            File dir = poolDir(topicPoolId);
            String safe = safeFileName(fileName);
            Map<String, Object> meta = readMeta(dir, safe, System.currentTimeMillis());

            String mySub = currentSubOrNull();
            if (mySub == null) return Response.status(Response.Status.UNAUTHORIZED).build();

            if (!isAdminDb()) {
                Object ts = meta.get("teacherSub");
                if (ts != null && !String.valueOf(ts).isBlank()) {
                    if (!mySub.equals(String.valueOf(ts))) {
                        throw new WebApplicationException("Nicht für dich bestimmt.", Response.Status.FORBIDDEN);
                    }
                }
            }

            String uploaderSub = meta.get("uploaderSub") != null ? String.valueOf(meta.get("uploaderSub")) : null;
            String noteTitle = meta.get("title") != null ? String.valueOf(meta.get("title")) : safe;
            String teacherName = currentFullNameOrFallback();

            meta.put("approved", true);
            meta.put("status", "APPROVED");
            meta.put("approvedByName", teacherName); // ← NEU: Name des Lehrers speichern
            writeMeta(dir, safe, meta);

            pushNotificationToUserSub(
                    uploaderSub,
                    "NOTE_APPROVED",
                    "Mitschrift freigegeben",
                    "Deine Mitschrift '" + noteTitle + "' wurde von " + teacherName + " freigegeben.",
                    Map.of("topicPoolId", topicPoolId, "fileName", safe, "status", "APPROVED")
            );

            return Response.ok(meta).build();

        } catch (WebApplicationException wex) {
            throw wex;
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(Map.of("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/{fileName}/reject")
    public Response reject(@PathParam("topicPoolId") Long topicPoolId,
                           @PathParam("fileName") String fileName) {
        requireTeacherOrAdminDb();
        try {
            File dir = poolDir(topicPoolId);
            String safe = safeFileName(fileName);
            Map<String, Object> meta = readMeta(dir, safe, System.currentTimeMillis());

            String mySub = currentSubOrNull();
            if (mySub == null) return Response.status(Response.Status.UNAUTHORIZED).build();

            if (!isAdminDb()) {
                Object ts = meta.get("teacherSub");
                if (ts != null && !String.valueOf(ts).isBlank()) {
                    if (!mySub.equals(String.valueOf(ts))) {
                        throw new WebApplicationException("Nicht für dich bestimmt.", Response.Status.FORBIDDEN);
                    }
                }
            }

            String uploaderSub = meta.get("uploaderSub") != null ? String.valueOf(meta.get("uploaderSub")) : null;
            String noteTitle = meta.get("title") != null ? String.valueOf(meta.get("title")) : safe;
            String teacherName = currentFullNameOrFallback();

            pushNotificationToUserSub(
                    uploaderSub,
                    "NOTE_REJECTED",
                    "Mitschrift abgelehnt",
                    "Deine Mitschrift '" + noteTitle + "' wurde von " + teacherName + " abgelehnt und entfernt.",
                    Map.of("topicPoolId", topicPoolId, "fileName", safe, "status", "REJECTED")
            );

        } catch (WebApplicationException wex) {
            throw wex;
        } catch (Exception ignore) {}

        return delete(topicPoolId, fileName);
    }

    private static InputPart first(Map<String, List<InputPart>> map, String key) {
        List<InputPart> list = map.get(key);
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    private static String field(Map<String, List<InputPart>> map, String key) throws Exception {
        InputPart p = first(map, key);
        return p == null ? null : p.getBodyAsString();
    }

    private static String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static Long fieldLong(Map<String, List<InputPart>> map, String key) throws Exception {
        String s = field(map, key);
        if (s == null || s.isBlank()) return null;
        return Long.parseLong(s.trim());
    }

    private static String filename(InputPart part) {
        String hdr = part.getHeaders().getFirst("Content-Disposition");
        if (hdr == null) return "upload.pdf";
        for (String cd : hdr.split(";")) {
            cd = cd.trim();
            if (cd.startsWith("filename=")) {
                String fn = cd.substring("filename=".length()).trim().replace("\"", "");
                return fn.isEmpty() ? "upload.pdf" : fn;
            }
        }
        return "upload.pdf";
    }
}