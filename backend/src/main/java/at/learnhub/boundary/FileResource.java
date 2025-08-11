package at.learnhub.boundary;

import at.learnhub.model.TopicContent;
import at.learnhub.repository.TopicContentRepository;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.io.File;

@Path("/api/files")
@RequestScoped
public class FileResource {

    @Inject TopicContentRepository topicContentRepo;

    @GET
    @Path("/thumbnail/{topicContentId}")
    public Response getThumbnail(@PathParam("topicContentId") Long id) {
        TopicContent tc = topicContentRepo.getById(id);
        if (tc.getMedia() == null) throw new NotFoundException("No media for this topic content");

        File file = new File(tc.getMedia().getPath());
        return Response.ok(file).type("image/jpeg").build();
    }

    @GET
    @Path("/pdf/{topicContentId}")
    public Response getPdf(@PathParam("topicContentId") Long id) {
        TopicContent tc = topicContentRepo.getById(id);
        if (tc.getMedia() == null) throw new NotFoundException("No media for this topic content");

        File file = new File(tc.getMedia().getPath());
        return Response.ok(file).type("application/pdf").build();
    }
}
