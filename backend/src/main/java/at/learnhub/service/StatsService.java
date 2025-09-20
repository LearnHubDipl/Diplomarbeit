package at.learnhub.service;

import at.learnhub.dto.simple.StatsLegendEntry;
import at.learnhub.dto.simple.StatsOverviewDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class StatsService {
    @Inject
    EntityManager em;

    private final String[] labels = {"ausreichend gelernt","2x richtig beantwortet","1x richtig beantwortet","falsch","nicht beantwortet"};
    private final String[] colors = {"#309F22","#3DD32B","#B7F0B0","#FE8B8B","#FFEAA4"};

    /**
     * Globale Statistik für einen User
     */
    public StatsOverviewDto calculateStatsOverview(Long userId) {
        return calculateStatsOverviewInternal(userId, null);
    }

    /**
     * Statistik für einen User und optionalen TopicPool
     */
    public StatsOverviewDto calculateStatsOverviewForTopicPool(Long userId, Long topicPoolId) {
        return calculateStatsOverviewInternal(userId, topicPoolId);
    }
    /**
     * Berechnet die Statistik und die Legend-Prozentwerte direkt in der DB
     */
    private StatsOverviewDto calculateStatsOverviewInternal(Long userId, Long topicPoolId) {
        String query = "SELECT " +
                "SUM(CASE WHEN e.lastAnsweredCorrectly IS NULL THEN 1 ELSE 0 END), " +
                "SUM(CASE WHEN e.correctCount = 0 AND e.lastAnsweredCorrectly IS NOT NULL THEN 1 ELSE 0 END), " +
                "SUM(CASE WHEN e.correctCount = 1 THEN 1 ELSE 0 END), " +
                "SUM(CASE WHEN e.correctCount = 2 THEN 1 ELSE 0 END), " +
                "SUM(CASE WHEN e.correctCount >= 3 THEN 1 ELSE 0 END) " +
                "FROM QuestionPoolEntry e " +
                "WHERE e.questionPool.user.id = :userId";

        if (topicPoolId != null) {
            query += " AND e.questionPool.id = :topicPoolId";
        }

        var q = em.createQuery(query);
        q.setParameter("userId", userId);
        if (topicPoolId != null) q.setParameter("topicPoolId", topicPoolId);

        Object[] result = (Object[]) q.getSingleResult();

        int unanswered   = ((Number) result[0]).intValue();
        int incorrect    = ((Number) result[1]).intValue();
        int correctOnce  = ((Number) result[2]).intValue();
        int correctTwice = ((Number) result[3]).intValue();
        int sufficient   = ((Number) result[4]).intValue();

        int[] values = {sufficient, correctTwice, correctOnce, incorrect, unanswered};
        int total = Arrays.stream(values).sum();

        List<StatsLegendEntry> legend = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            int percent = total > 0 ? (int) Math.round((values[i] / (double) total) * 100) : 0;
            legend.add(new StatsLegendEntry(labels[i], percent, colors[i]));
        }

        return new StatsOverviewDto(incorrect, sufficient, correctTwice, correctOnce, unanswered, legend);
    }
}