package at.learnhub.dto.simple;

import java.util.List;

public record StatsOverviewDto(
        int incorrect,
        int sufficient,
        int correctTwice,
        int correctOnce,
        int unanswered,
        List<StatsLegendEntry> legend
) {}
