package at.learnhub.service;

import at.learnhub.dto.request.UploadPdfBase64RequestDto;
import at.learnhub.dto.response.TopicContentDto;
import at.learnhub.mapper.TopicContentMapper;
import at.learnhub.model.MediaFile;
import at.learnhub.model.TopicContent;
import at.learnhub.model.TopicPool;
import at.learnhub.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.file.*;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TopicNotesService {

    @Inject
    EntityManager em;

    @ConfigProperty(name = "app.upload.dir", defaultValue = "uploads/pdfs")
    String uploadDirConfig;

    @ConfigProperty(name = "app.public.base", defaultValue = "/uploads/pdfs/")
    String publicBaseConfig;

    @Transactional
    public List<TopicContentDto> listNotesByTopicPool(Long topicPoolId) {
        List<TopicContent> entities = em.createQuery("""
                SELECT tc FROM TopicContent tc
                WHERE tc.topicPool.id = :tp
                  AND tc.media IS NOT NULL
                  AND tc.media.type = 'pdf'
                ORDER BY tc.date DESC, tc.id DESC
                """, TopicContent.class)
                .setParameter("tp", topicPoolId)
                .getResultList();

        return entities.stream().map(TopicContentMapper::toDto).toList();
    }

    /**
     * Upload: Base64 -> Datei -> MediaFile + TopicContent -> TopicContentDto
     *
     * Logik:
     * - Schüler (teacherId gesetzt) -> approved = false, wartet auf Freigabe
     * - Lehrer/Admin (kein teacherId) -> approved = true, sofort sichtbar
     */
    @Transactional
    public TopicContentDto uploadBase64AndCreate(UploadPdfBase64RequestDto dto) {
        if (dto.base64() == null || dto.base64().isBlank())
            throw new IllegalArgumentException("base64 fehlt");
        if (dto.title() == null || dto.title().isBlank())
            throw new IllegalArgumentException("title fehlt");
        if (dto.topicPoolId() == null)
            throw new IllegalArgumentException("topicPoolId fehlt");

        TopicPool pool = em.find(TopicPool.class, dto.topicPoolId());
        if (pool == null)
            throw new BadRequestException("TopicPool nicht gefunden: " + dto.topicPoolId());

        // Zugewiesene Lehrperson (nur bei Schüler-Uploads gesetzt)
        User teacher = (dto.teacherId() != null) ? em.find(User.class, dto.teacherId()) : null;

        // Datei aus Base64 speichern
        String raw = dto.base64();
        String b64 = raw.contains(",") ? raw.substring(raw.indexOf(',') + 1) : raw;
        byte[] bytes = Base64.getDecoder().decode(b64);

        String safeTitle = dto.title().toLowerCase().replaceAll("[^a-z0-9\\-]+", "-");
        String fileName = UUID.randomUUID() + "-" + safeTitle + ".pdf";

        Path uploadDir = Paths.get(uploadDirConfig);
        try {
            Files.createDirectories(uploadDir);
            Files.write(uploadDir.resolve(fileName), bytes, StandardOpenOption.CREATE_NEW);
        } catch (Exception e) {
            throw new RuntimeException("PDF speichern fehlgeschlagen", e);
        }

        String publicUrl = publicBaseConfig + fileName;

        MediaFile mf = new MediaFile();
        mf.setPath(publicUrl);
        mf.setType("pdf");
        mf.setDescription(dto.description());
        em.persist(mf);

        // Schüler-Upload (teacherId gesetzt) -> pending, Lehrer/Admin -> sofort approved
        boolean needsApproval = (teacher != null);

        TopicContent tc = new TopicContent();
        tc.setTitle(dto.title());
        tc.setDescription(dto.description());
        tc.setDate(LocalDate.now());
        tc.setMedia(mf);
        tc.setTopicPool(pool);
        tc.setTaughtBy(teacher);
        tc.setApproved(!needsApproval);
        em.persist(tc);

        return TopicContentMapper.toDto(tc);
    }

    /**
     * Lehrer gibt eine Mitschrift frei.
     * Setzt approved = true und approvedBy = der freigebende Lehrer.
     */
    @Transactional
    public TopicContentDto approveNote(Long topicContentId, Long approvingTeacherId) {
        TopicContent tc = em.find(TopicContent.class, topicContentId);
        if (tc == null)
            throw new BadRequestException("TopicContent nicht gefunden: " + topicContentId);

        User approver = em.find(User.class, approvingTeacherId);
        if (approver == null)
            throw new BadRequestException("Lehrer nicht gefunden: " + approvingTeacherId);

        tc.setApproved(true);
        tc.setApprovedBy(approver);
        em.merge(tc);

        return TopicContentMapper.toDto(tc);
    }

    /**
     * Lehrer lehnt eine Mitschrift ab — löscht sie komplett.
     */
    @Transactional
    public void rejectNote(Long topicContentId) {
        TopicContent tc = em.find(TopicContent.class, topicContentId);
        if (tc == null)
            throw new BadRequestException("TopicContent nicht gefunden: " + topicContentId);

        // PDF-Datei vom Dateisystem löschen
        if (tc.getMedia() != null && tc.getMedia().getPath() != null) {
            try {
                String path = tc.getMedia().getPath();
                // publicBase aus dem Pfad entfernen um den Dateinamen zu bekommen
                String fileName = path.substring(path.lastIndexOf('/') + 1);
                Path file = Paths.get(uploadDirConfig).resolve(fileName);
                Files.deleteIfExists(file);
            } catch (Exception ignored) {}
        }

        em.remove(tc);
    }

    /**
     * Gibt alle pending Mitschriften zurück die einer bestimmten Lehrperson zugewiesen sind.
     */
    @Transactional
    public List<TopicContentDto> listPendingForTeacher(Long teacherId) {
        List<TopicContent> entities = em.createQuery("""
                SELECT tc FROM TopicContent tc
                WHERE tc.taughtBy.id = :teacherId
                  AND (tc.isApproved = false OR tc.isApproved IS NULL)
                  AND tc.media IS NOT NULL
                  AND tc.media.type = 'pdf'
                ORDER BY tc.date DESC, tc.id DESC
                """, TopicContent.class)
                .setParameter("teacherId", teacherId)
                .getResultList();

        return entities.stream().map(TopicContentMapper::toDto).toList();
    }
}