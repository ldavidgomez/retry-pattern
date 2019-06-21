package packageName.spring.circuit.breaker

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.retry.annotation.CircuitBreaker
import org.springframework.retry.annotation.Recover
import org.springframework.stereotype.Service
import packageName.wrappers.LoggerWrapper

@Service
@Slf4j
class ExternalSampleService @Autowired constructor(private val logger: LoggerWrapper) {

    /**
     * Annotated with maxAttempts = 3 for test.
     * After throw an Exceptions, the next calls goes direct to fallback_run() method.
     *
     * @return
     */
    @CircuitBreaker(maxAttempts = 3, openTimeout = 5000L, resetTimeout = 20000L)
    fun run(): String {
        logger.info("Calling external service...")
        if (Math.random() > 0.5) {
            throw RuntimeException("Something was wrong...")
        }
        logger.info("Success calling external service")
        return "Success calling external service"
    }

    /**
     * The recover method needs to have same return type.
     *
     * @return
     */
    @Recover
    private fun fallback_run(): String {
        logger.error("Fallback for external service")
        return "Succes on fallback"
    }
}
