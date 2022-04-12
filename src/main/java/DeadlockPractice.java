import java.util.concurrent.ConcurrentHashMap;

public class DeadlockPractice {

    public static void main(String[] args) {
        final Object lock = new Object();
        ConcurrentHashMap<Integer, Integer> map = new ConcurrentHashMap<>();

        new Thread(() -> {
            synchronized (lock) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
                map.put(12, 12);
            }
        }).start();
        new Thread(() -> {
            map.compute(12, (k, v) -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
                synchronized (lock) {
                    System.out.println("Never happen");
                }
                return 12;
            });
        }).start();
    }
}
