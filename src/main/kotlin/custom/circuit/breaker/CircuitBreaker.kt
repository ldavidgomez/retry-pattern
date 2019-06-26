package packageName.custom.circuit.breaker

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import packageName.wrappers.LoggerWrapper
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KFunction0

@Service
class CircuitBreaker @Autowired constructor(private val logger: LoggerWrapper) {

    private val maxAttempts = 2
    private val openTimeout = Duration.ofMillis(10000L)
    private var errorsCount: AtomicInteger = AtomicInteger(0)
    @Volatile
    private var state = CircuitBreakerState.CLOSED
    @Volatile
    private var lastExceptionThrown: Exception? = null
    @Volatile
    private var lastFailure: LocalDateTime? = null

    private val isTimerExpired: Boolean
        get() = lastFailure!!.plus(openTimeout) < LocalDateTime.now()

    val errorCounter: Int
        get() = errorsCount.get()

    val circuitState: CircuitBreakerState
        get() = state

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
                resetCircuit()
                logger.info("Success calling external service")
            } catch (ex: Exception) {
                handleException(ex)
                throw Exception("Something was wrong")
            }

        } else {
            if (state == CircuitBreakerState.HALF_OPEN || isTimerExpired) {
                state = CircuitBreakerState.HALF_OPEN

                logger.info("Time to retry...")

                try {
                    action.invoke()
                    logger.info("Success when HALF_OPEN")
                    resetCircuit()
                } catch (ex: Exception) {
                    logger.info("Fails when HALF_OPEN")
                    openCircuit(ex)
                    throw Exception("Fails when HALF_OPEN")
                }

            } else {
                logger.info("Circuit is still opened. Retrying at ${lastFailure!!.plus(openTimeout)}")
                throw Exception("Circuit is still opened. Retrying at ${lastFailure!!.plus(openTimeout)}")
            }

            throw lastExceptionThrown!!
        }
    }

    private fun openCircuit(exception: Exception) {
        lastFailure = LocalDateTime.now()
        lastExceptionThrown = exception
        state = CircuitBreakerState.OPEN
        logger.error("Opening circuit...")
    }

    private fun resetCircuit() {
        errorsCount.set(0)
        lastExceptionThrown = null
        state = CircuitBreakerState.CLOSED
        logger.info("Closing circuit...")
    }

    private fun handleException(exception: Exception) {
        errorsCount.incrementAndGet()
        logger.error("Service fails for $errorsCount times.")
        if (errorsCount.get() !in 0..maxAttempts) {
            openCircuit(exception)
        }
    }
}
