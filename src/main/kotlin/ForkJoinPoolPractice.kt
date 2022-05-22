import java.util.concurrent.CountedCompleter
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RecursiveTask
import java.util.concurrent.atomic.AtomicInteger

class LetsCount(
    private val start: Int,
    private val end: Int,
) : RecursiveTask<Int>() {

    companion object {
        private const val THRESHOLD = 1
    }

    override fun compute(): Int {
        if (end - start == THRESHOLD) {
            return end - start
        }
        println("IN $start $end ${Thread.currentThread().name}")

        val mid = (start + end) / 2
        val task1 = LetsCount(start, mid)
        val task2 = LetsCount(mid, end)
        invokeAll(task1, task2)

        println("SPLIT $start $end ${Thread.currentThread().name}")
        val ans = task1.join() + task2.join()

        println("OUT $start $end ${Thread.currentThread().name}")
        return ans
    }
}

class LetsCount2(
    parent: CountedCompleter<Int>?,
    private val start: Int,
    private val end: Int,
    private val parentResult: AtomicInteger,
) : CountedCompleter<Int>(parent) {

    companion object {
        private const val THRESHOLD = 1
    }

    private val result = AtomicInteger()

    override fun compute() {
        println("IN $start $end")
        if (end - start == THRESHOLD) {
            result.addAndGet(end - start)
        } else {
            addToPendingCount(2)
            val mid = (start + end) / 2
            LetsCount2(this, start, mid, result).fork()
            LetsCount2(this, mid, end, result).fork()
        }

        tryComplete()
        println("OUT $start $end")
    }

    override fun onCompletion(caller: CountedCompleter<*>?) {
        println("COMPLETE $start $end")
        parentResult.addAndGet(result.get())
    }

    override fun getRawResult() = result.get()
}

fun main() {
    val poll = ForkJoinPool.commonPool()
    val task = LetsCount(0, 16)
    val ans = poll.invoke(task)
    println(ans)

    val result = AtomicInteger()
    val task2 = LetsCount2(null,0, 1024, result)
    val ans2 = poll.invoke(task2)
    println("$ans2 ${result.get()}")
}
