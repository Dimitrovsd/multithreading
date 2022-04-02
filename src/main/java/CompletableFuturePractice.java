import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CompletableFuturePractice {

    public static class PriceRetriever {

        public double getPrice(long itemId, long shopId) {
            // имитация долгого HTTP-запроса
            int delay = ThreadLocalRandom.current().nextInt(10);
            try {
                Thread.sleep(delay * 1000);
            } catch (InterruptedException e) {
            }

            return ThreadLocalRandom.current().nextDouble(1000);
        }
    }

    public static class PriceAggregator implements AutoCloseable {

        private static final double UNDEFINED_PRICE = Double.MAX_VALUE;

        private PriceRetriever priceRetriever = new PriceRetriever();

        private Set<Long> shopIds = Set.of(10l, 45l, 66l, 345l, 234l, 333l, 67l, 123l, 768l);

        private final ExecutorService executorService = Executors.newFixedThreadPool(shopIds.size());

        public Optional<Double> getMinPrice(long itemId) {
            List<CompletableFuture<Double>> futures = shopIds.stream()
                    .map(shopId -> CompletableFuture.supplyAsync(() -> priceRetriever.getPrice(itemId, shopId),
                                    executorService)
                            .completeOnTimeout(UNDEFINED_PRICE, 2900, TimeUnit.MILLISECONDS))
                    .collect(Collectors.toList());

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(unused -> futures.stream()
                            .map(future -> future.getNow(UNDEFINED_PRICE))
                            .filter(price -> price != UNDEFINED_PRICE)
                            .reduce(Math::min))
                    .join();
        }

        @Override
        public void close() {
            executorService.shutdownNow();
        }
    }

    public static void main(String[] args) {
        try (PriceAggregator priceAggregator = new PriceAggregator()) {
            long itemId = 12l;

            long start = System.currentTimeMillis();
            Optional<Double> min = priceAggregator.getMinPrice(itemId);
            long end = System.currentTimeMillis();

            System.out.println(min);
            System.out.println((end - start) < 3000); // should be true
        }
    }
}
