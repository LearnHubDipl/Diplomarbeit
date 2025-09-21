package at.learnhub.boundary;

import at.learnhub.dto.simple.ProgressOverviewDto;
import at.learnhub.dto.simple.StatsOverviewDto;
import at.learnhub.service.StatsService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/api/stats")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StatsResource {

    @Inject
    StatsService statsService;

    //Globale Statistik für einen User
    @GET
    @Path("/{userId}/overview")
    public StatsOverviewDto getStatsOverview(@PathParam("userId") Long userId) {
        return statsService.calculateStatsOverview(userId);
    }

    //Statistik für einen User und TopicPool
    @GET
    @Path("/{userId}/topicPool/{topicPoolId}/overview")
    public StatsOverviewDto getStatsOverviewForTopicPool(
            @PathParam("userId") Long userId,
            @PathParam("topicPoolId") Long topicPoolId) {
        return statsService.calculateStatsOverviewForTopicPool(userId, topicPoolId);
    }

    @GET
    @Path("/{userId}")
    public ProgressOverviewDto getUserProgress(@PathParam("userId") Long userId,
                                               @QueryParam("topicPoolId") Long topicPoolId) {
        return statsService.calculateProgressOverview(userId, topicPoolId);
    }


}
