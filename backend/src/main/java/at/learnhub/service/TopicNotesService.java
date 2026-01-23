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

    @Inject EntityManager em;

    @ConfigProperty(name = "app.upload.dir",  defaultValue = "uploads/pdfs")
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

    /** Upload: Base64 -> Datei → MediaFile + TopicContent → TopicContentDto */
    @Transactional
    public TopicContentDto uploadBase64AndCreate(UploadPdfBase64RequestDto dto) {
        if (dto.base64() == null || dto.base64().isBlank())
            throw new IllegalArgumentException("base64 fehlt");
        if (dto.title() == null || dto.title().isBlank())
            throw new IllegalArgumentException("title fehlt");
        if (dto.topicPoolId() == null)
            throw new IllegalArgumentException("topicPoolId fehlt");

        TopicPool pool = em.find(TopicPool.class, dto.topicPoolId());
        if (pool == null) throw new BadRequestException("TopicPool nicht gefunden: " + dto.topicPoolId());

        // 3) Lehrer/User referenzieren
        User teacher = (dto.teacherId() != null) ? em.find(User.class, dto.teacherId()) : null;

        // 4) Datei aus Base64 speichern
        String raw = dto.base64();
        String b64 = raw.contains(",") ? raw.substring(raw.indexOf(',') + 1) : raw;
        byte[] bytes = Base64.getDecoder().decode(b64);

        String safeTitle = dto.title().toLowerCase().replaceAll("[^a-z0-9\\-]+","-");
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

        TopicContent tc = new TopicContent();
        tc.setTitle(dto.title());
        tc.setDescription(dto.description());
        tc.setDate(LocalDate.now());
        tc.setMedia(mf);
        tc.setTopicPool(pool);
        tc.setTaughtBy(teacher);
        tc.setApproved(true);
        em.persist(tc);

        return TopicContentMapper.toDto(tc);
    }
}
