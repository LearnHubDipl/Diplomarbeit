package at.learnhub.service;

import at.learnhub.dto.simple.*;
import at.learnhub.model.QuestionPoolEntry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

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
                "COALESCE(SUM(CASE WHEN e.lastAnsweredCorrectly IS NULL THEN 1 ELSE 0 END), 0), " +
                "COALESCE(SUM(CASE WHEN e.correctCount = 0 AND e.lastAnsweredCorrectly IS NOT NULL THEN 1 ELSE 0 END), 0), " +
                "COALESCE(SUM(CASE WHEN e.correctCount = 1 THEN 1 ELSE 0 END), 0), " +
                "COALESCE(SUM(CASE WHEN e.correctCount = 2 THEN 1 ELSE 0 END), 0), " +
                "COALESCE(SUM(CASE WHEN e.correctCount >= 3 THEN 1 ELSE 0 END), 0) " +
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


    @Transactional
    public ProgressOverviewDto calculateProgressOverview(Long userId, Long topicPoolId) {

        // Wenn topicPoolId == null - alle Einträge des Users
        List<QuestionPoolEntry> entries = topicPoolId == null ?
                em.createQuery("SELECT e FROM QuestionPoolEntry e WHERE e.questionPool.user.id = :userId", QuestionPoolEntry.class)
                        .setParameter("userId", userId)
                        .getResultList() :
                em.createQuery("SELECT e FROM QuestionPoolEntry e WHERE e.questionPool.user.id = :userId AND e.questionPool.id = :poolId", QuestionPoolEntry.class)
                        .setParameter("userId", userId)
                        .setParameter("poolId", topicPoolId)
                        .getResultList();

        int incorrect = 0;
        int sufficient = 0;
        int correctTwice = 0;
        int correctOnce = 0;
        int unanswered = 0;

        for (QuestionPoolEntry entry : entries) {
            if (entry.getLastAnsweredCorrectly() == null) {
                unanswered++;
            } else if (entry.getCorrectCount() == 0) {
                incorrect++;
            } else if (entry.getCorrectCount() == 1) {
                correctOnce++;
            } else if (entry.getCorrectCount() == 2) {
                correctTwice++;
            } else if (entry.getCorrectCount() >= 3) {
                sufficient++;
            }
        }

        List<ProgressLevelDto> levels = new ArrayList<>();

        levels.add(new ProgressLevelDto("Startlevel", List.of(
                new ProgressEntryDto("Falsch", "#FE8B8B", incorrect, incorrect + " Fragen falsch beantwortet"),
                new ProgressEntryDto("Nicht beantwortet", "#FFEAA4", unanswered, unanswered + " Fragen noch nicht beantwortet")
        )));
        levels.add(new ProgressLevelDto("Basislevel", List.of(
                new ProgressEntryDto("1x richtig", "#B7F0B0", correctOnce, correctOnce + " Fragen einmal richtig beantwortet")
        )));
        levels.add(new ProgressLevelDto("Trainingslevel", List.of(
                new ProgressEntryDto("2x richtig", "#3DD32B", correctTwice, correctTwice + " Fragen zweimal richtig beantwortet")
        )));
        levels.add(new ProgressLevelDto("Highscorelevel", List.of(
                new ProgressEntryDto("Ausreichend geübt", "#309F22", sufficient, sufficient + " Fragen ausreichend geübt")
        )));

        return new ProgressOverviewDto(levels);
    }
}