package web.rumers.app.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, List<LocalDateTime>> tracker = new ConcurrentHashMap<>();

    private static final int MAX_POSTS = 5;
    private static final int WINDOW_MINUTES = 60;

    public boolean isLimited(String ip) {
        clean(ip);
        List<LocalDateTime> times = tracker.getOrDefault(ip, new ArrayList<>());
        return times.size() >= MAX_POSTS;
    }

    public void track(String ip) {
        tracker.computeIfAbsent(ip, k -> new ArrayList<>()).add(LocalDateTime.now());
    }

    public long getRetryAfter(String ip) {
        List<LocalDateTime> times = tracker.getOrDefault(ip, new ArrayList<>());
        if (times.isEmpty()) return 0;
        LocalDateTime oldest = times.getFirst();
        return ChronoUnit.SECONDS.between(LocalDateTime.now(),
                oldest.plusMinutes(WINDOW_MINUTES));
    }

    // Remove expired entries
    private void clean(String ip) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(WINDOW_MINUTES);
        tracker.computeIfPresent(ip, (k, v) -> {
            v.removeIf(t -> t.isBefore(cutoff));
            return v;
        });
    }

    // Clean all expired every 30 mins
    @Scheduled(fixedRate = 1800000)
    public void cleanAll() {
        tracker.forEach((ip, times) -> clean(ip));
    }
}