package at.learnhub.boundary;

import at.learnhub.dto.request.CreateMediaRequestDto;
import at.learnhub.model.MediaFile;
import at.learnhub.repository.MediaFileRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

@Path("/api/media")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MediaResource {

    @Inject
    MediaFileRepository mediaRepo;

    @POST
    @Transactional
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Media angelegt"),
            @APIResponse(responseCode = "400", description = "Ungültige Eingaben")
    })
    public Response create(CreateMediaRequestDto dto) {
        if (dto == null || dto.path() == null || dto.path().isBlank()
                || dto.type() == null || dto.type().isBlank()) {
            throw new BadRequestException("path und type sind erforderlich");
        }

        MediaFile mf = new MediaFile();
        mf.setPath(dto.path().trim());
        mf.setType(dto.type().trim());
        mf.setDescription(dto.description());
        mediaRepo.create(mf);
        return Response.status(Response.Status.CREATED).entity(mf).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") Long id) {
        return Response.ok(mediaRepo.getById(id)).build();
    }
}
