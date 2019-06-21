package packageName.spring.circuit.breaker

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SpringSampleController @Autowired constructor(private val springSystemService: SpringSampleService) {

    @GetMapping("/callExternalService")
    fun callExternalService(): String {
        return springSystemService.run()
    }
}
