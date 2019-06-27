package packageName.custom.retry

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import packageName.wrappers.LoggerWrapper

@Service
class Retry<T> @Autowired constructor(private val logger: LoggerWrapper) {
    var retryCounter: Int = 0
    private val maxRetries: Int = 3

    fun run(action: () -> T): T {
        return try {
            action.invoke()
        } catch (e: Exception) {
            retry(action)
        }

    }

    @Throws(RuntimeException::class)
    private fun retry(action: () -> T): T {
        logger.error("FAILED - Command fails, will be retried $maxRetries times.")
        retryCounter = 1
        while (retryCounter < maxRetries) {
            try {
                return action.invoke()
            } catch (ex: Exception) {
                retryCounter++
                logger.error("FAILED - Command fails on retry $retryCounter of $maxRetries error: $ex")
                if (retryCounter >= maxRetries) {
                    logger.info("Max retries exceeded.")
                    break
                }
                Thread.sleep(5000L)
            }

        }
        throw RuntimeException("Command fails on all of $maxRetries retries")
    }
}

