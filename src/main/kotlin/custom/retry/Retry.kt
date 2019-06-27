package packageName.custom.retry

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import packageName.wrappers.LoggerWrapper
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicInteger

@Service
class Retry<T> @Autowired constructor(private val logger: LoggerWrapper) {
    var retryCounter: AtomicInteger = AtomicInteger(0)
    private val maxRetries: Int = 3
    @Volatile
    private var lastFailure: LocalDateTime? = null
    private val delay = Duration.ofMillis(1000L)
    private val countdownFlag: Boolean = false

    private val isTimerExpired: Boolean
        get() = lastFailure!!.plus(delay).isBefore(LocalDateTime.now())

    private val nextTry: Long
        get() = lastFailure!!.plus(delay).toInstant(ZoneOffset.UTC).toEpochMilli().minus(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli())

    fun run(action: () -> T): T {
        return try {
            action.invoke()
        } catch (e: Exception) {
            retryCounter.addAndGet(1)
            lastFailure = LocalDateTime.now()
            logger.error("FAILED - Command fails $retryCounter, it will be retried ${maxRetries.minus(retryCounter.get())} times.")
            retry(action)
        }

    }

    @Throws(RuntimeException::class)
    private fun retry(action: () -> T): T {
        while (retryCounter.get() < maxRetries) {
            if (isTimerExpired) {
                try {
                    return action.invoke()
                } catch (ex: Exception) {
                    retryCounter.addAndGet(1)
                    logger.error("FAILED - Command fails $retryCounter, it will be retried ${maxRetries.minus(retryCounter.get())} times.")
                    if (retryCounter.get() >= maxRetries) {
                        logger.info("Max retries exceeded.")
                        break
                    }

                }
            } else {
                countDown()
            }
        }
        throw RuntimeException("Command fails on all of $maxRetries retries")
    }

    private fun countDown() {
        if (countdownFlag) logger.info("Next try in $nextTry millis")
    }
}

