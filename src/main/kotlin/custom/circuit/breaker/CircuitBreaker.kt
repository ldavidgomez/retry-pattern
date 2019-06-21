package packageName.custom.circuit.breaker

import java.time.Duration
import java.time.LocalDateTime

internal class CircuitBreaker {

    private val maxAttempts = 3
    private val openTimeout = Duration.ofMillis(5000L)
    private var errorsCount: Int = 0
    @Volatile
    private var state = CircuitBreakerState.CLOSED
    private var lastExceptionThrown: Exception? = null
    private var lastFailure: LocalDateTime? = null

    private val isTimerExpired: Boolean
        get() = lastFailure!!.plus(openTimeout) > LocalDateTime.now()

    enum class CircuitBreakerState {
        CLOSED,
        OPEN,
        HALF_OPEN
    }

    @Throws(Exception::class)
    fun run(action: Runnable) {
        if (state == CircuitBreakerState.CLOSED) {
            try {
                action.run()
            } catch (ex: Exception) {
                handleException(ex)
                throw ex
            }

        } else {
            if (state == CircuitBreakerState.HALF_OPEN || isTimerExpired) {
                state = CircuitBreakerState.HALF_OPEN

                try {
                    action.run()
                    reset()
                } catch (ex: Exception) {
                    open(ex)
                    throw ex
                }

            }

            throw lastExceptionThrown!!
        }
    }

    private fun open(exception: Exception) {
        state = CircuitBreakerState.OPEN
        lastFailure = LocalDateTime.now()
        errorsCount = 0
        lastExceptionThrown = exception
    }

    private fun reset() {
        errorsCount = 0
        lastExceptionThrown = null
        state = CircuitBreakerState.CLOSED
    }

    private fun handleException(exception: Exception) {
        errorsCount++
        if (errorsCount >= maxAttempts) {
            lastExceptionThrown = exception
            state = CircuitBreakerState.OPEN
            lastFailure = LocalDateTime.now()
        }
    }
}
