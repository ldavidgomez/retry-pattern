package packageName

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.retry.annotation.EnableRetry

@SpringBootApplication
@EnableRetry
class SpringEntryPoint {
    fun getApplicationContext(args: Array<String>): ConfigurableApplicationContext? {
        return SpringApplication.run(SpringEntryPoint::class.java, *args)
    }

    companion object {
        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            SpringEntryPoint().getApplicationContext(args)
        }
    }
}
