import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class Auction implements AutoCloseable {

    public static class Bid {
        final Long id;
        final Long participantId;
        final Long price;

        public Bid(Long id, Long participantId, Long price) {
            this.id = id;
            this.participantId = participantId;
            this.price = price;
        }
    }

    public static class Notifier {
        public void sendOutdatedMessage(Bid bid) {
            try {
                System.out.println(String.format("Bid %d with price %d is outdated", bid.id, bid.price));
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private final AtomicReference<Bid> latestBid;
    private final Notifier notifier;
    private final ExecutorService notificationSenderExecutor;

    public Auction() {
        latestBid = new AtomicReference<>();
        notifier = new Notifier();
        notificationSenderExecutor = Executors.newFixedThreadPool(100);
    }

    public boolean propose(Bid bid) {
        Bid currentBid = latestBid.get();
        if (currentBid != null && bid.price <= currentBid.price) {
            return false;
        }

        synchronized (latestBid) {
            currentBid = latestBid.get();
            if (currentBid != null && bid.price <= currentBid.price) {
                return false;
            }
            latestBid.set(bid);
        }

        sendNotificationAsync(currentBid);

        return true;
    }

    private void sendNotificationAsync(Bid outdatedBid) {
        if (outdatedBid == null) {
            return;
        }
        notificationSenderExecutor.submit(() -> notifier.sendOutdatedMessage(outdatedBid));
    }

    public Bid getLatestBid() {
        return latestBid.get();
    }

    @Override
    public void close() throws InterruptedException {
        notificationSenderExecutor.shutdown();
        notificationSenderExecutor.awaitTermination(10, TimeUnit.SECONDS);
    }
}
