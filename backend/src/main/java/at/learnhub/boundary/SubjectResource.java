package at.learnhub.boundary;

import at.learnhub.dto.request.CreateSubjectRequestDto;
import at.learnhub.dto.request.UpdateSubjectRequestDto;
import at.learnhub.dto.simple.SubjectDto;
import at.learnhub.mapper.SubjectMapper;
import at.learnhub.model.MediaFile;
import at.learnhub.model.Subject;
import at.learnhub.repository.MediaFileRepository;
import at.learnhub.repository.SubjectRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

@Path("/api/subjects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SubjectResource {

    @Inject
    SubjectRepository subjectRepo;

    @Inject
    MediaFileRepository mediaFileRepo;

    @GET
    public List<SubjectDto> getAllSubjects() {
        return subjectRepo.findAllOrderedByName();
    }

    @GET
    @Path("/{id}")
    public SubjectDto getSubject(@PathParam("id") Long id) {
        return subjectRepo.getDtoById(id);
    }

    @POST
    public Response createSubject(CreateSubjectRequestDto dto) {
        MediaFile img = null;
        if (dto.imgId() != null) {
            img = mediaFileRepo.getById(dto.imgId());
        }

        Subject subject = SubjectMapper.fromCreateDto(dto, img);
        subjectRepo.create(subject);

        return Response.created(URI.create("/api/subjects/" + subject.getId()))
                .entity(SubjectMapper.toDto(subject))
                .build();
    }

    @PUT
    @Path("/{id}")
    public SubjectDto updateSubject(@PathParam("id") Long id, UpdateSubjectRequestDto dto) {
        Subject subject = subjectRepo.getById(id);

        MediaFile img = null;
        if (dto.imgId() != null) {
            img = mediaFileRepo.getById(dto.imgId());
        }

        SubjectMapper.applyUpdate(subject, dto, img);
        subject = subjectRepo.update(subject);

        return SubjectMapper.toDto(subject);
    }

    @DELETE
    @Path("/{id}")
    public Response deleteSubject(@PathParam("id") Long id) {
        subjectRepo.delete(id);
        return Response.noContent().build();
    }
}
