package at.learnhub.boundary;

import at.learnhub.model.StreakTracking;
import at.learnhub.model.User;
import at.learnhub.repository.StreakTrackingRepository;
import at.learnhub.service.StreakTrackingService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/streak")
@Produces(MediaType.APPLICATION_JSON)
public class StreakTrackingResource {

    //TODO: Login / Update



        @Inject
        StreakTrackingService streakTrackingService;

        //TODO: findByid in User reintun
        @Inject
        StreakTrackingRepository streakTrackingRepository;

        @GET
        @Path("/user/{userId}")
        public Response getStreak(@PathParam("userId") Long userId) {
            User user = streakTrackingRepository.findById(userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("User not found")
                        .build();
            }

            StreakTracking streakTracking = streakTrackingService.findByUser(user);
            int streak = (streakTracking != null) ? streakTracking.getStreak() : 0;

            return Response.ok(new StreakDTO(streak)).build();
        }

        public static class StreakDTO {
            public int streak;

            public StreakDTO(int streak) {
                this.streak = streak;
            }
        }
}
