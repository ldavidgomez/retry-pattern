package packageName.custom.circuit.breaker

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CustomCircuitBreakerController @Autowired constructor(
    private val externalService: ExternalService,
    private val circuitBreaker: CircuitBreaker) {

    @GetMapping("/call-custom-external-service")
    fun callExternalService(): String {
        return try {
            circuitBreaker.run(externalService::run).toString()
            "Success calling external service"
        } catch (e: Exception) {
            return e.message.toString()
        }
    }
}
