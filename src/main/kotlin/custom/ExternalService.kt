package packageName.custom.circuit.breaker

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import packageName.wrappers.LoggerWrapper

@Service
@Slf4j
class ExternalService @Autowired constructor(private val logger: LoggerWrapper) {

    fun run(): String {
        logger.info("Calling external service...")
        val random = Math.random()
        if (random > 0.5) {
            throw RuntimeException("Something was wrong...")
        }
        logger.info("Success calling external service")
        return "Success calling external service"
    }
}
