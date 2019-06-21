package packageName.resources

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController @Autowired constructor(){

    @GetMapping("/api/health")
    fun health() = "Green"

}
