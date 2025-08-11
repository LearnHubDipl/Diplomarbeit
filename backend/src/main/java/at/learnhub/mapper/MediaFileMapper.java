package at.learnhub.mapper;

import at.learnhub.dto.simple.MediaFileSlimDto;
import at.learnhub.model.MediaFile;

public final class MediaFileMapper {

    private MediaFileMapper() {}

    public static MediaFileSlimDto toSlimDto(MediaFile m) {
        if (m == null) return null;
        return new MediaFileSlimDto(
                m.getId(),
                m.getPath(),
                m.getType(),
                m.getDescription(),
                m.getUploadedAt()
        );
    }
}
