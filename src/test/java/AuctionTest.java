import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuctionTest {

    @Test
    public void performanceTest() throws InterruptedException {
        int threadsNum = 10;
        List<Auction.Bid> bids = generateBids(threadsNum);
        long maxPrice = bids.stream().mapToLong(i -> i.price).max().getAsLong();

        List<Thread> threads = new ArrayList<>();
        try (Auction auction = new Auction()) {
            for (int i = 0; i < threadsNum; i++) {
                threads.add(runThread(i, threadsNum, auction, bids));
            }

            for (int i = 0; i < threadsNum; i++) {
                threads.get(i).join();
            }

            assertEquals(auction.getLatestBid().price, maxPrice);
        }
    }

    private List<Auction.Bid> generateBids(int threadsNum) {
        Random random = new Random();
        List<Auction.Bid> bids = new ArrayList<>();
        for (int i = 0; i < 1000000; i++) {
            bids.add(new Auction.Bid((long) i, (long) i % threadsNum, (long) random.nextInt(50000000)));
        }
        return bids;
    }

    private List<Auction.Bid> generateBids2(int threadsNum) {
        List<Auction.Bid> bids = new ArrayList<>();
        for (int i = 0; i < 1000000; i++) {
            bids.add(new Auction.Bid((long) i, (long) i % threadsNum, (long) i));
        }
        return bids;
    }

    private Thread runThread(int threadId, int threadsNum, Auction auction, List<Auction.Bid> bids) {
        var thread = new Thread(() -> {
            for (int i = threadId; i < bids.size(); i += threadsNum) {
                auction.propose(bids.get(i));
            }
        });
        thread.start();
        return thread;
    }
}
