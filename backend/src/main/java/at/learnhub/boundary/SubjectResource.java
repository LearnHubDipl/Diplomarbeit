package at.learnhub.boundary;

import at.learnhub.dto.request.CreateSubjectRequestDto;
import at.learnhub.dto.request.UpdateSubjectRequestDto;
import at.learnhub.dto.simple.SubjectDto;
import at.learnhub.mapper.SubjectMapper;
import at.learnhub.model.MediaFile;
import at.learnhub.model.Subject;
import at.learnhub.repository.MediaFileRepository;
import at.learnhub.repository.SubjectRepository;
import at.learnhub.security.CustomSecurityContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

@Path("/api/subjects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SubjectResource {

    @Inject
    SubjectRepository subjectRepo;

    @Inject
    MediaFileRepository mediaFileRepo;

    @Context
    SecurityContext securityContext;

    private boolean isStudentFromToken() {
        return false;
    }

    private void requireTeacherOrAdmin() {
        if (isStudentFromToken()) {
            throw new WebApplicationException(
                    "Not authorized to manage subjects",
                    Response.Status.FORBIDDEN
            );
        }
    }

    @GET
    public List<SubjectDto> getAllSubjects() {
        return subjectRepo.findAllOrderedByName();
    }

    @GET
    @Path("/{id}")
    public SubjectDto get(@PathParam("id") Long id) {
        Subject s = subjectRepo.getById(id);
        return SubjectMapper.toDto(s);
    }

    @POST
    @Transactional
    public SubjectDto create(CreateSubjectRequestDto dto) {

        requireTeacherOrAdmin();

        MediaFile img = (dto.imgId() != null) ? mediaFileRepo.getById(dto.imgId()) : null;
        Subject s = SubjectMapper.fromCreateDto(dto, img);
        subjectRepo.create(s);
        return SubjectMapper.toDto(s);
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public SubjectDto update(@PathParam("id") Long id, UpdateSubjectRequestDto dto) {
        requireTeacherOrAdmin();

        Subject s = subjectRepo.getById(id);
        MediaFile img = (dto.imgId() != null) ? mediaFileRepo.getById(dto.imgId()) : null;
        SubjectMapper.applyUpdate(s, dto, img);
        subjectRepo.update(s);
        return SubjectMapper.toDto(s);
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public void delete(@PathParam("id") Long id) {
        requireTeacherOrAdmin();

        subjectRepo.delete(id);
    }
}
