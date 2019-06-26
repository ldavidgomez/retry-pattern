package packageName.custom.retry

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import packageName.wrappers.LoggerWrapper
import kotlin.reflect.KFunction0

@Service
class Retry<T> @Autowired constructor(private val logger: LoggerWrapper, private val maxRetries: Int) {
    var retryCounter: Int = 0

    fun run(action: KFunction0<T>): T {
        return try {
            action.invoke()
        } catch (e: Exception) {
            retry(action)
        }

    }

    @Throws(RuntimeException::class)
    private fun retry(function: KFunction0<T>): T {
        logger.error("FAILED - Command fails, will be retried $maxRetries times.")
        retryCounter = 1
        while (retryCounter < maxRetries) {
            try {
                return function.invoke()
            } catch (ex: Exception) {
                retryCounter++
                logger.error("FAILED - Command fails on retry $retryCounter of $maxRetries error: $ex")
                if (retryCounter >= maxRetries) {
                    logger.info("Max retries exceeded.")
                    break
                }
            }

        }
        throw RuntimeException("Command fails on all of $maxRetries retries")
    }
}

