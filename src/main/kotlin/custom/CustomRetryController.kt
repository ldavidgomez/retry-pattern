package packageName.custom

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import packageName.custom.circuit.breaker.ExternalService
import java.lang.Exception

@RestController
class CustomRetryController @Autowired constructor(private val externalService: ExternalService) {

    @GetMapping("/call-custom-external-retry-service")
    fun callExternalRetryService(): String = try {
        externalService.run()
    } catch (e: Exception) {
        "Something was wrong..."
    }
}
