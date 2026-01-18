package at.learnhub.boundary;

import at.learnhub.dto.simple.SolutionSlimDto;
import at.learnhub.dto.simple.SolutionVoteCountDto;
import at.learnhub.dto.simple.SolutionVoteDto;
import at.learnhub.mapper.SolutionMapper;
import at.learnhub.model.Solution;
import at.learnhub.model.SolutionStep;
import at.learnhub.model.SolutionVote;
import at.learnhub.service.SolutionService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/solutions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SolutionResource {

    @Inject
    SolutionService solutionService;

    @POST
    @Path("/{solutionId}/votes/up")
    public SolutionVoteDto upvote(@PathParam("solutionId") Long solutionId,
                                  @QueryParam("userId") Long userId) {
        if (userId == null) {
            throw new BadRequestException("userId must be provided");
        }
        SolutionVote vote = solutionService.castVote(solutionId, userId, true);
        return new SolutionVoteDto(
                vote.getId(),
                vote.getUpVote(),
                vote.getSolution().getId(),
                vote.getUser().getId()
        );
    }

    @POST
    @Path("/{solutionId}/votes/down")
    public SolutionVoteDto downvote(@PathParam("solutionId") Long solutionId,
                                    @QueryParam("userId") Long userId) {
        if (userId == null) {
            throw new BadRequestException("userId must be provided");
        }
        SolutionVote vote = solutionService.castVote(solutionId, userId, false);
        return new SolutionVoteDto(
                vote.getId(),
                vote.getUpVote(),
                vote.getSolution().getId(),
                vote.getUser().getId()
        );
    }

    @GET
    @Path("/{solutionId}/votes/count")
    public SolutionVoteCountDto getCount(@PathParam("solutionId") Long solutionId) {
        return solutionService.getVoteCounts(solutionId);
    }

    @POST
    @Path("/create")
    public Response createSolution(@QueryParam("questionId") Long questionId,
                                   @QueryParam("userId") Long userId,
                                   SolutionSlimDto dto) {

        if (questionId == null || userId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("questionId und userId müssen angegeben werden.")
                    .build();
        }

        try {
            Solution savedSolution = solutionService.createSolution(questionId, userId, dto);

            SolutionSlimDto resultDto = SolutionMapper.toSlimDto(savedSolution);

            return Response.status(Response.Status.CREATED)
                    .entity(resultDto)
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        }
    }
}