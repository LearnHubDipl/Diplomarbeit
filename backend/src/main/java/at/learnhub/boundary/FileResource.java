// src/main/java/at/learnhub/boundary/FileResource.java
package at.learnhub.boundary;

import at.learnhub.dto.simple.PdfBase64UploadDto;
import at.learnhub.dto.simple.UploadPdfResult;
import at.learnhub.model.TopicContent;
import at.learnhub.repository.SubjectRepository;
import at.learnhub.repository.TopicContentRepository;
import at.learnhub.repository.TopicPoolRepository;
import at.learnhub.repository.UserRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Base64;
import java.util.UUID;

@Path("/api/files")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FileResource {

    private static final String UPLOAD_DIR = "uploads";

    @Inject TopicContentRepository topicContentRepo;
    @Inject SubjectRepository subjectRepo;
    @Inject TopicPoolRepository topicPoolRepo;
    @Inject UserRepository userRepository;

    @POST
    @Path("/pdf-base64")
    @Transactional
    public Response uploadPdfBase64(PdfBase64UploadDto dto) {
        if (dto == null || dto.base64 == null || dto.base64.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("base64 fehlt").build();
        }
        if (dto.title == null || dto.title.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("title fehlt").build();
        }

        try {
            String b64 = stripDataUrlPrefix(dto.base64);

            byte[] data = Base64.getDecoder().decode(b64);

            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                Files.createDirectories(dir.toPath());
            }

            String safeName = (dto.fileName != null && !dto.fileName.isBlank())
                    ? dto.fileName.replaceAll("[^a-zA-Z0-9._-]", "_")
                    : ("upload-" + UUID.randomUUID() + ".pdf");

            File out = new File(dir, safeName);
            try (FileOutputStream fos = new FileOutputStream(out)) {
                fos.write(data);
            }

            String publicUrl = "http://localhost:8080/api/files/download/" + safeName;

            TopicContent tc = new TopicContent();

            setFirstExistingStringField(tc, dto.title, "title", "name");

            setFirstExistingStringField(tc, publicUrl, "pdfUrl", "fileUrl", "url", "path", "storagePath", "storageUrl");

            if (dto.uploaderUserId != null) {
                try {
                    var user = userRepository.getUserById(dto.uploaderUserId);
                    if (user != null) {
                        setFirstExistingRelation(tc, user, "user", "uploader", "owner", "createdBy");
                    }
                } catch (Throwable ignore) {}
            }
            if (dto.topicPoolId != null) {
                try {
                    var tp = topicPoolRepo.getTopicPoolById(dto.topicPoolId);
                    if (tp != null) {
                        setFirstExistingRelation(tc, tp, "topicPool", "pool");
                    }
                } catch (Throwable ignore) {}
            }
            if (dto.subjectId != null) {
                try {
                    var s = subjectRepo.getById(dto.subjectId);
                    if (s != null) {
                        setFirstExistingRelation(tc, s, "subject");
                    }
                } catch (Throwable ignore) {}
            }

            topicContentRepo.persist(tc);

            Long id = tryGetId(tc);
            UploadPdfResult result = new UploadPdfResult(id, publicUrl, null);
            return Response.ok(result).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Ungültiges Base64").build();
        } catch (Exception e) {
            return Response.serverError().entity("Fehler beim Speichern: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/download/{file}")
    @Produces("application/pdf")
    public Response download(@PathParam("file") String file) {
        File f = new File(UPLOAD_DIR, file);
        if (!f.exists()) throw new NotFoundException();
        return Response.ok(f)
                .header("Content-Disposition", "inline; filename=\"" + file + "\"")
                .build();
    }

    private String stripDataUrlPrefix(String raw) {
        int comma = raw.indexOf(',');
        if (raw.startsWith("data:") && comma > 0) {
            return raw.substring(comma + 1).trim();
        }
        return raw.trim();
    }

    private static void setFirstExistingStringField(Object target, String value, String... candidates) {
        Class<?> c = target.getClass();
        for (String name : candidates) {
            try {
                var m = c.getMethod("set" + capitalize(name), String.class);
                m.invoke(target, value);
                return;
            } catch (NoSuchMethodException ignored) {
                try {
                    var f = c.getDeclaredField(name);
                    if (f.getType() == String.class) {
                        f.setAccessible(true);
                        f.set(target, value);
                        return;
                    }
                } catch (NoSuchFieldException | IllegalAccessException ignored2) {}
            } catch (ReflectiveOperationException ignored) {}
        }
    }

    private static void setFirstExistingRelation(Object target, Object related, String... candidates) {
        Class<?> c = target.getClass();
        Class<?> relType = related.getClass();
        for (String name : candidates) {
            var setter = findCompatibleSetter(c, "set" + capitalize(name), relType);
            if (setter != null) {
                try { setter.invoke(target, related); return; }
                catch (ReflectiveOperationException ignored) {}
            }
            try {
                var f = c.getDeclaredField(name);
                if (f.getType().isAssignableFrom(relType)) {
                    f.setAccessible(true);
                    f.set(target, related);
                    return;
                }
            } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        }
    }

    private static java.lang.reflect.Method findCompatibleSetter(Class<?> c, String methodName, Class<?> relType) {
        for (var m : c.getMethods()) {
            if (!m.getName().equals(methodName)) continue;
            var p = m.getParameterTypes();
            if (p.length == 1 && p[0].isAssignableFrom(relType)) return m;
        }
        return null;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static Long tryGetId(TopicContent tc) {
        try {
            var getId = TopicContent.class.getMethod("getId");
            Object v = getId.invoke(tc);
            if (v instanceof Long l) return l;
            if (v instanceof Number n) return n.longValue();
        } catch (Throwable ignored) {}
        try {
            var getId2 = TopicContent.class.getMethod("getContentId");
            Object v = getId2.invoke(tc);
            if (v instanceof Long l) return l;
            if (v instanceof Number n) return n.longValue();
        } catch (Throwable ignored) {}
        return null;
    }
}
