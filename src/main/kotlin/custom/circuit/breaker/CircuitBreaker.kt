package packageName.custom.circuit.breaker

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import packageName.wrappers.LoggerWrapper
import java.time.Duration
import java.time.LocalDateTime
import kotlin.reflect.KFunction0

@Service
class CircuitBreaker @Autowired constructor(private val logger: LoggerWrapper) {

    private val maxAttempts = 2
    private val openTimeout = Duration.ofMillis(5000L)
    private var errorsCount: Int = 0
    @Volatile
    private var state = CircuitBreakerState.CLOSED
    private var lastExceptionThrown: Exception? = null
    private var lastFailure: LocalDateTime? = null

    private val isTimerExpired: Boolean
        get() = lastFailure!!.plus(openTimeout) < LocalDateTime.now()

    enum class CircuitBreakerState {
        CLOSED,
        OPEN,
        HALF_OPEN
    }

    @Throws(Exception::class)
    fun run(action: KFunction0<String>) {
        logger.info("state is $state")
        if (state == CircuitBreakerState.CLOSED) {
            try {
                action.invoke()
                reset()
                logger.info("Success calling external service")
            } catch (ex: Exception) {
                handleException(ex)
                throw Exception("Something was wrong")
            }

        } else {
            if (state == CircuitBreakerState.HALF_OPEN || isTimerExpired) {
                state = CircuitBreakerState.HALF_OPEN

                try {
                    action.invoke()
                    reset()
                } catch (ex: Exception) {
                    open(ex)
                    throw Exception("Fails when HALF_OPEN")
                }

            } else {
                throw Exception("Circuit is still opened until ${lastFailure!!.plus(openTimeout)}")
            }

            throw lastExceptionThrown!!
        }
    }

    private fun open(exception: Exception) {
        logger.error("Opening circuit...")
        state = CircuitBreakerState.OPEN
        lastFailure = LocalDateTime.now()
        lastExceptionThrown = exception
    }

    private fun reset() {
        errorsCount = 0
        lastExceptionThrown = null
        state = CircuitBreakerState.CLOSED
        logger.info("Closing circuit...")
    }

    private fun handleException(exception: Exception) {
        errorsCount++
        logger.error("Service fails for $errorsCount times.")
        if (errorsCount >= maxAttempts) {
            open(exception)
        }
    }
}
