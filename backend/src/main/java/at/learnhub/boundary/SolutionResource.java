package at.learnhub.boundary;

import at.learnhub.dto.simple.SolutionVoteCountDto;
import at.learnhub.dto.simple.SolutionVoteDto;
import at.learnhub.model.Solution;
import at.learnhub.model.SolutionVote;
import at.learnhub.service.SolutionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.PathParam;

@Path("/api/solutions/{solutionId}/votes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SolutionResource {

    @Inject
    SolutionService solutionService;

    @POST
    @Path("/up")
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
    @Path("/down")
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
    @Path("/count")
    public SolutionVoteCountDto count(@PathParam("solutionId") Long solutionId) {
        int score = solutionService.getVoteCount(solutionId);
        return new SolutionVoteCountDto(solutionId, score);
    }

}

