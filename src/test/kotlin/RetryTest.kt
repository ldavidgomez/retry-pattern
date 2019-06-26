import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import packageName.custom.circuit.breaker.ExternalService
import packageName.custom.retry.Retry
import packageName.wrappers.LoggerWrapper

class RetryTest {

    private var success = "success"
    private var maxRetries = 3

    @Mock
    private lateinit var externalService: ExternalService

    private val logger: LoggerWrapper = LoggerWrapper()
    private lateinit var retry: Retry<String>

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun should_not_retry_when_successful() {
        retry = Retry(logger, maxRetries)

        val result = retry.run { success }

        assertThat(success).isEqualTo(result)
        assertThat(0).isEqualTo(retry.retryCounter)
    }

    @Test
    fun should_retry_once_then_succeed_when_first_fails_and_second_successes() {
        retry = Retry(logger, maxRetries)
        `when`(externalService.run())
            .thenThrow(RuntimeException("Command Failed"))
            .thenReturn(success)

        val result = retry.run(externalService::run)


        assertThat(success).isEqualTo(result)
        assertThat(1).isEqualTo(retry.retryCounter)
    }

    @Test
    fun should_throw_exception_when_max_retries() {
        retry = Retry(logger, maxRetries)
        `when`(externalService.run())
            .thenThrow(RuntimeException("Command Failed"))
            .thenThrow(RuntimeException("Command Failed"))
            .thenThrow(RuntimeException("Command Failed"))

        assertThatThrownBy {
            retry.run(externalService::run)
        }.isInstanceOf(RuntimeException::class.java)
            .hasMessage("Command fails on all of $maxRetries retries")

    }
}
