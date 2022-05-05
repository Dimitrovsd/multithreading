import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

class Restaurant {}

public class RestaurantSearchService {

    private final ConcurrentHashMap<String, LongAdder> stat;

    public RestaurantSearchService() {
        stat = new ConcurrentHashMap<>();
    }

    public Restaurant getByName(String restaurantName) {
        addToStat(restaurantName);
        return new Restaurant();
    }

    private void addToStat(String restaurantName) {
        stat.computeIfAbsent(restaurantName, rn -> new LongAdder())
                .increment();
    }

    public Set<String> printStat() {
        return stat.entrySet().stream()
                .map(e -> e.getKey() + " - " + e.getValue())
                .collect(Collectors.toSet());
    }
}
