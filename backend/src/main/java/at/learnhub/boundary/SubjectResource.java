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
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
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
    public SubjectDto get(@PathParam("id") Long id) {
        Subject s = subjectRepo.getById(id);
        return SubjectMapper.toDto(s);
    }


    @POST
    @Transactional
    public SubjectDto create(CreateSubjectRequestDto dto) {
        MediaFile img = (dto.imgId() != null) ? mediaFileRepo.getById(dto.imgId()) : null;
        Subject s = SubjectMapper.fromCreateDto(dto, img);
        subjectRepo.create(s);
        return SubjectMapper.toDto(s);
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public SubjectDto update(@PathParam("id") Long id, UpdateSubjectRequestDto dto) {
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
        subjectRepo.delete(id);
    }
}
