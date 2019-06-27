package packageName.custom.circuit.breaker

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import packageName.custom.retry.Retry
import packageName.wrappers.LoggerWrapper

@Service
@Slf4j
class ExternalService @Autowired constructor(private val logger: LoggerWrapper, private val retry: Retry<String>) {

    fun run(): String {
        logger.info("Calling external service...")
        val random = Math.random()
        if (random > 0.5) {
            retry.run {throw RuntimeException("Something was wrong...")}
        }
        retry.run{"Success calling external service"}
        logger.info("Success calling external service")
        return "Success calling external service"
    }
}
