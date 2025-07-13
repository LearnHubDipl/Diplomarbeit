package at.learnhub.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import jakarta.persistence.EntityNotFoundException;

@Provider
public class EntityNotFoundMapper implements ExceptionMapper<EntityNotFoundException> {
    @Override
    public Response toResponse(EntityNotFoundException e) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(e.getMessage())
                .type("text/plain")
                .build();
    }
}
