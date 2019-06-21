package packageName.wrappers

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
@Component
class LoggerWrapper:TraceWrapper {
    override fun info(message: String)  = LOG.info(getCallingClassName() + " - " + message)

    override fun error(message: String) = LOG.error(getCallingClassName() + " - " + message)

    private fun getCallingClassName(): String = Thread.currentThread().stackTrace[3].className

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

}