import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RestaurantSearchServiceTest {

    @Test
    public void simpleTest() {
        RestaurantSearchService searchService = new RestaurantSearchService();
        searchService.getByName("A");
        searchService.getByName("A");
        searchService.getByName("A");
        searchService.getByName("B");
        searchService.getByName("B");
        searchService.getByName("B");
        assertEquals(searchService.printStat(), Set.of("A - 3", "B - 3"));
    }

    @Test
    public void mutlithreadTest() {
        int threadsNum = 4;
        int iterations = 100000000;
        CountDownLatch latch = new CountDownLatch(threadsNum);

        RestaurantSearchService searchService = new RestaurantSearchService();
        List<Thread> workers = new ArrayList<>(threadsNum);
        for (int i = 0; i < threadsNum; i++) {
            var thread = new Thread(() -> {
                latch.countDown();
                try {
                    latch.await();
                } catch (InterruptedException ignored) { }

                for (int it = 0; it < iterations; it++) {
                    searchService.getByName("A");
                }
            });
            workers.add(thread);
            thread.start();
        }

        workers.forEach(worker -> {
            try {
                worker.join();
            } catch (InterruptedException ignored) { }
        });

        assertEquals(searchService.printStat(), Set.of("A - " + (threadsNum * iterations)));
    }
}
