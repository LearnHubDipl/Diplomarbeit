package at.learnhub.boundary;

import at.learnhub.dto.request.UploadPdfBase64RequestDto;
import at.learnhub.dto.response.TopicContentDto;
import at.learnhub.service.TopicNotesService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/topic-pools/{topicPoolId}/notes")
@Produces(MediaType.APPLICATION_JSON)
public class TopicNotesBase64Resource {

    @Inject TopicNotesService service;

    @POST
    @Path("/upload-base64")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response uploadBase64(@PathParam("topicPoolId") Long topicPoolId,
                                 UploadPdfBase64RequestDto dto) {
        if (dto.topicPoolId() == null) {
            dto = new UploadPdfBase64RequestDto(
                    dto.base64(), dto.title(), dto.description(),
                    dto.uploaderName(), dto.teacherId(), dto.subjectId(), topicPoolId
            );
        }
        TopicContentDto out = service.uploadBase64AndCreate(dto);
        return Response.status(Response.Status.CREATED).entity(out).build();
    }
}
