package packageName.spring.circuit.breaker

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SpringCircuitBreakerController @Autowired constructor(private val springExternalSampleService: SpringExternalSampleService) {

    @GetMapping("/call-spring-external-service")
    fun callExternalSservice(): String {
        return springExternalSampleService.run()
    }
}
