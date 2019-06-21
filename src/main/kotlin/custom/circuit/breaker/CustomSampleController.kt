package packageName.spring.circuit.breaker

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import packageName.custom.circuit.breaker.CircuitBreaker

@RestController
class CustomSampleController @Autowired constructor(
    private val customSampleService: CustomSampleService,
    private val circuitBreaker: CircuitBreaker) {

    @GetMapping("/call-custom-external-service")
    fun callExternalService(): String {
        return try {
            circuitBreaker.run(customSampleService::run).toString()
            "Success calling external service"
        } catch (e: Exception) {
            return e.message.toString()
        }
    }
}
