package packageName.spring.retry

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SpringRetryController @Autowired constructor(private val springExternalRetryService: SpringExternalRetryService) {

    @GetMapping("/call-spring-external-retry-service")
    fun callExternalRetryService(): String {
        return springExternalRetryService.run()
    }
}
