package custom.circuit.breaker

import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import packageName.custom.circuit.breaker.CircuitBreaker
import packageName.custom.circuit.breaker.ExternalService
import packageName.wrappers.LoggerWrapper
import java.time.Duration

class CircuitBreakerTest {

    private var success = "Success calling external service"

    @Mock
    private lateinit var externalService: ExternalService

    private val logger: LoggerWrapper = LoggerWrapper()
    private lateinit var circuitBreaker: CircuitBreaker

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun should_not_retry_when_successful() {
        circuitBreaker = CircuitBreaker(logger)

        val result = circuitBreaker.run { success }

        Assertions.assertThat(result).isEqualTo(success)
        Assertions.assertThat(circuitBreaker.errorCounter).isEqualTo(0)
    }

    @Test
    fun should_throw_Exception_and_remain_circuit_closed_when_first_fails() {
        circuitBreaker = CircuitBreaker(logger)
        Mockito.`when`(externalService.run())
            .thenThrow(RuntimeException("Something was wrong..."))

        Assertions.assertThatThrownBy {
            circuitBreaker.run(externalService::run)
        }.isInstanceOf(Exception::class.java)
            .hasMessage("Something was wrong")

        Assertions.assertThat(circuitBreaker.circuitState).isEqualTo(CircuitBreaker.CircuitBreakerState.CLOSED)
        Assertions.assertThat(circuitBreaker.errorCounter).isEqualTo(1)
    }

    @Test
    fun should_throw_Exception_and_remain_circuit_closed_when_second_fails() {
        circuitBreaker = CircuitBreaker(logger)
        Mockito.`when`(externalService.run())
            .thenThrow(RuntimeException("Something was wrong..."))
            .thenThrow(RuntimeException("Something was wrong..."))

        Assertions.assertThatThrownBy {
            circuitBreaker.run(externalService::run)
        }.isInstanceOf(Exception::class.java)
            .hasMessage("Something was wrong")

        Assertions.assertThatThrownBy {
            circuitBreaker.run(externalService::run)
        }.isInstanceOf(Exception::class.java)
            .hasMessage("Something was wrong")

        Assertions.assertThat(circuitBreaker.circuitState).isEqualTo(CircuitBreaker.CircuitBreakerState.CLOSED)
        Assertions.assertThat(circuitBreaker.errorCounter).isEqualTo(2)
    }

    @Test
    fun should_throw_Exception_and_change_circuit_to_open_when_third_fails() {
        circuitBreaker = CircuitBreaker(logger)
        Mockito.`when`(externalService.run())
            .thenThrow(RuntimeException("Something was wrong..."))
            .thenThrow(RuntimeException("Something was wrong..."))
            .thenThrow(RuntimeException("Something was wrong..."))

        Assertions.assertThatThrownBy {
            circuitBreaker.run(externalService::run)
        }.isInstanceOf(Exception::class.java)
            .hasMessage("Something was wrong")

        Assertions.assertThatThrownBy {
            circuitBreaker.run(externalService::run)
        }.isInstanceOf(Exception::class.java)
            .hasMessage("Something was wrong")

        Assertions.assertThatThrownBy {
            circuitBreaker.run(externalService::run)
        }.isInstanceOf(Exception::class.java)
            .hasMessage("Something was wrong")

        Assertions.assertThat(circuitBreaker.circuitState).isEqualTo(CircuitBreaker.CircuitBreakerState.OPEN)
        Assertions.assertThat(circuitBreaker.errorCounter).isEqualTo(3)
    }

    @Test
    fun should_fails_max_attemps_and_change_circuit_to_half_open_when_reset_and_success() {
        circuitBreaker = CircuitBreaker(logger)
        Mockito.`when`(externalService.run())
            .thenThrow(RuntimeException("Something was wrong..."))
            .thenThrow(RuntimeException("Something was wrong..."))
            .thenThrow(RuntimeException("Something was wrong..."))
            .thenReturn(success)

        Assertions.assertThatThrownBy {
            circuitBreaker.run(externalService::run)
        }.isInstanceOf(Exception::class.java)
            .hasMessage("Something was wrong")

        Assertions.assertThatThrownBy {
            circuitBreaker.run(externalService::run)
        }.isInstanceOf(Exception::class.java)
            .hasMessage("Something was wrong")

        Assertions.assertThatThrownBy {
            circuitBreaker.run(externalService::run)
        }.isInstanceOf(Exception::class.java)
            .hasMessage("Something was wrong")

        Thread.sleep(10000L)

        circuitBreaker.run(externalService::run)

        Assertions.assertThat(circuitBreaker.circuitState).isEqualTo(CircuitBreaker.CircuitBreakerState.CLOSED)
        Assertions.assertThat(circuitBreaker.errorCounter).isEqualTo(0)
    }

    @Test
    fun should_success_and_change_circuit_to_open_when_reset_and_success() {
        circuitBreaker = CircuitBreaker(logger)
        Mockito.`when`(externalService.run())
            .thenThrow(RuntimeException("Something was wrong..."))
            .thenThrow(RuntimeException("Something was wrong..."))
            .thenThrow(RuntimeException("Something was wrong..."))
            .thenReturn(success)

        Assertions.assertThatThrownBy {
            circuitBreaker.run(externalService::run)
        }.isInstanceOf(Exception::class.java)
            .hasMessage("Something was wrong")

        Assertions.assertThatThrownBy {
            circuitBreaker.run(externalService::run)
        }.isInstanceOf(Exception::class.java)
            .hasMessage("Something was wrong")

        Assertions.assertThatThrownBy {
            circuitBreaker.run(externalService::run)
        }.isInstanceOf(Exception::class.java)
            .hasMessage("Something was wrong")

        Thread.sleep(10000L)

        circuitBreaker.run(externalService::run)

        Assertions.assertThat(circuitBreaker.circuitState).isEqualTo(CircuitBreaker.CircuitBreakerState.CLOSED)
        Assertions.assertThat(circuitBreaker.errorCounter).isEqualTo(0)
    }
}
