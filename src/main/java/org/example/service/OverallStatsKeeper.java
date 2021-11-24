package org.example.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class OverallStatsKeeper {

    private final Map<String, RPSResolver.BaseStats> overallStats = new ConcurrentHashMap<>();

    public void count(String sessionId, RPSResolver.Response res) {
        overallStats.put(sessionId, res.statistics);

    }

    public double getUserPlace(String sessionId) {
        RPSResolver.BaseStats userFinalStats = overallStats.get(sessionId);
        if (userFinalStats != null) {
            double userWinRate = userFinalStats.winRate();

            Map<Integer, Integer> partitioningMap = overallStats.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> Double.compare(e.getValue().winRate(), userWinRate) <= 0 ? 0 : 1,
                            e -> 1,
                            Integer::sum));
            return partitioningMap.getOrDefault(0, 0) /
                    (double) (partitioningMap.getOrDefault(0, 0) + partitioningMap.getOrDefault(1, 0));
        }
        return 0.0;
    }
}
