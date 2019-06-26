package packageName.spring.retry

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.remoting.RemoteAccessException
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import packageName.wrappers.LoggerWrapper


@Service
@Slf4j
class SpringExternalRetryService @Autowired constructor(private val logger: LoggerWrapper) {

    /**
     * After throw an Exceptions, the next calls goes direct to recover() method.
     */
    @Retryable(maxAttempts = 2, include = [RemoteAccessException::class])
    fun run(): String {
        logger.info("Calling external service...")
        if (Math.random() > 0.3) {
            throw RemoteAccessException("Something was wrong...")
        }
        logger.info("Success calling external service")
        return "Success calling external service"
    }

    /**
     * The recover method needs to have same return type.
     */
    @Recover
    private fun recover(e: RemoteAccessException): String {
        logger.error("Recover for external service")
        return "Succes on fallback"
    }
}
