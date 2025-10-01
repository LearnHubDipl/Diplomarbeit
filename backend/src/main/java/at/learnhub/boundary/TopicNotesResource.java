package at.learnhub.boundary;

import at.learnhub.repository.TopicContentRepository;
import at.learnhub.repository.TopicPoolRepository;
import at.learnhub.service.FileStorageService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import java.util.List;
import java.util.Map;

@Path("/api/topic-pools/{topicPoolId}/notes")
@Produces(MediaType.APPLICATION_JSON)
public class TopicNotesResource {

    @Inject TopicContentRepository topicContentRepo;
    @Inject TopicPoolRepository topicPoolRepo;
    @Inject FileStorageService storage;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list(@PathParam("topicPoolId") Long topicPoolId) {
        try {
            java.nio.file.Path base = java.nio.file.Paths.get(System.getProperty("app.upload.dir", "/app/uploads"));
            java.nio.file.Path dir  = base.resolve(java.nio.file.Paths.get("topic-notes", String.valueOf(topicPoolId)));

            if (!java.nio.file.Files.exists(dir)) {
                return Response.ok(java.util.Collections.emptyList()).build();
            }

            final String baseUrl = System.getProperty("app.public.base", "https://vm91.htl-leonding.ac.at");

            java.util.List<java.util.Map<String, Object>> files = new java.util.ArrayList<>();
            try (java.util.stream.Stream<java.nio.file.Path> s = java.nio.file.Files.list(dir)) {
                s.filter(p -> java.nio.file.Files.isRegularFile(p)
                                && p.getFileName().toString().toLowerCase().endsWith(".pdf"))
                        .forEach(p -> {
                            try {
                                var attrs = java.nio.file.Files.readAttributes(p, java.nio.file.attribute.BasicFileAttributes.class);
                                String fileName = p.getFileName().toString();
                                long size = java.nio.file.Files.size(p);
                                long lastModified = attrs.lastModifiedTime().toMillis();

                                var item = new java.util.LinkedHashMap<String, Object>();
                                String publicUrl = baseUrl + "/uploads/topic-notes/" + topicPoolId + "/" + fileName;

                                item.put("fileName", fileName);
                                item.put("url", publicUrl);
                                item.put("publicUrl", publicUrl);
                                item.put("size", size);
                                item.put("lastModified", lastModified);

                                String baseName = baseNameOf(fileName);
                                java.nio.file.Path metaPath = dir.resolve(baseName + ".meta.json");
                                if (java.nio.file.Files.exists(metaPath)) {
                                    try {
                                        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                        var node = mapper.readTree(metaPath.toFile());
                                        if (node.hasNonNull("title"))        item.put("title",        node.get("title").asText());
                                        if (node.hasNonNull("description"))  item.put("description",  node.get("description").asText());
                                        if (node.hasNonNull("uploaderName")) item.put("uploaderName", node.get("uploaderName").asText());
                                        if (node.hasNonNull("teacherId"))    item.put("teacherId",    node.get("teacherId").asLong());
                                        if (node.hasNonNull("createdAt"))    item.put("createdAt",    node.get("createdAt").asLong());
                                    } catch (Exception ignore) {}
                                } else {
                                    item.put("createdAt", lastModified);
                                }

                                files.add(item);
                            } catch (Exception ignore) {}
                        });
            }
            files.sort((a,b) -> Long.compare((long)b.get("lastModified"), (long)a.get("lastModified")));
            return Response.ok(files).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(java.util.Map.of("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upload(@PathParam("topicPoolId") long topicPoolId,
                           MultipartFormDataInput input) {
        try {
            if (input == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(java.util.Map.of("error", "Missing multipart body")).build();
            }

            Map<String, List<InputPart>> map = input.getFormDataMap();
            InputPart filePart = first(map, "file");
            if (filePart == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(java.util.Map.of("error", "Missing file")).build();
            }

            String safeName = filename(filePart);

            String replaceFileName = trimOrNull(field(map, "replaceFileName"));
            boolean isReplace = replaceFileName != null;
            if (isReplace) {
                safeName = java.nio.file.Paths.get(replaceFileName).getFileName().toString();
            }

            java.nio.file.Path base = java.nio.file.Paths.get(System.getProperty("app.upload.dir", "/app/uploads"));
            java.nio.file.Path dir  = base.resolve(java.nio.file.Paths.get("topic-notes", String.valueOf(topicPoolId)));
            java.nio.file.Files.createDirectories(dir);

            if (!isReplace) {
                String baseName = baseNameOf(safeName);
                String ext = safeName.contains(".") ? safeName.substring(safeName.lastIndexOf('.')) : "";
                java.nio.file.Path target = dir.resolve(safeName);
                while (java.nio.file.Files.exists(target)) {
                    safeName = baseName + "-" + System.currentTimeMillis() + ext;
                    target = dir.resolve(safeName);
                }
            }

            try (java.io.InputStream is = filePart.getBody(java.io.InputStream.class, null)) {
                java.nio.file.Files.copy(is, dir.resolve(safeName), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                String title        = trimOrNull(field(map, "title"));
                String description  = trimOrNull(field(map, "description"));
                String uploaderName = trimOrNull(field(map, "uploaderName"));
                Long   teacherId    = fieldLong(map, "teacherId");

                long createdAt = System.currentTimeMillis();
                String baseName = baseNameOf(safeName);

                java.util.Map<String, Object> meta = new java.util.LinkedHashMap<>();
                meta.put("fileName", safeName);
                meta.put("title", title != null ? title : baseName);
                meta.put("description", description);
                meta.put("uploaderName", uploaderName);
                meta.put("teacherId", teacherId);
                meta.put("createdAt", createdAt);

                var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.nio.file.Path metaPath = dir.resolve(baseName + ".meta.json");
                mapper.writerWithDefaultPrettyPrinter().writeValue(metaPath.toFile(), meta);
            }

            final String baseUrl = System.getProperty("app.public.base", "https://vm91.htl-leonding.ac.at");
            String publicUrl = baseUrl + "/uploads/topic-notes/" + topicPoolId + "/" + safeName;

            return Response.ok(java.util.Map.of(
                    "topicPoolId", topicPoolId,
                    "fileName", safeName,
                    "publicUrl", publicUrl
            )).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(java.util.Map.of("error", e.getMessage())).build();
        }
    }

    @DELETE
    @Path("/{fileName}")
    @Transactional
    public Response delete(@PathParam("topicPoolId") Long topicPoolId,
                           @PathParam("fileName") String fileName) {
        try {
            String safe = java.nio.file.Paths.get(fileName).getFileName().toString();
            java.nio.file.Path base = java.nio.file.Paths.get(System.getProperty("app.upload.dir", "/app/uploads"));
            java.nio.file.Path dir  = base.resolve(java.nio.file.Paths.get("topic-notes", String.valueOf(topicPoolId)));
            java.nio.file.Path pdf  = dir.resolve(safe);
            java.nio.file.Path meta = dir.resolve(baseNameOf(safe) + ".meta.json");

            boolean existed = false;
            if (java.nio.file.Files.exists(pdf))  { java.nio.file.Files.delete(pdf);  existed = true; }
            if (java.nio.file.Files.exists(meta)) { java.nio.file.Files.delete(meta); }

            return existed ? Response.noContent().build()
                    : Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(java.util.Map.of("error", e.getMessage())).build();
        }
    }

    private static InputPart first(Map<String, List<InputPart>> map, String key) {
        var list = map.get(key);
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }
    private static String field(Map<String, List<InputPart>> map, String key) throws Exception {
        var p = first(map, key);
        return p == null ? null : p.getBodyAsString();
    }
    private static String trimOrNull(String s) {
        if (s == null) return null;
        var t = s.trim();
        return t.isEmpty() ? null : t;
    }
    private static Long fieldLong(Map<String, List<InputPart>> map, String key) throws Exception {
        String s = field(map, key);
        if (s == null || s.isBlank()) return null;
        return Long.parseLong(s.trim());
    }
    private static String filename(InputPart part) {
        var hdr = part.getHeaders().getFirst("Content-Disposition");
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
    private static String baseNameOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return (dot > 0) ? fileName.substring(0, dot) : fileName;
    }
}
