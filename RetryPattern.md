### El patrón Retry

En aplicaciones distribuidas, donde se realizan constantes comunicaciones entre servicios y/o recursos externos, 
se pueden producir errores temporales o transitorios (transient failures) cuando se interactúa con estos entornos. Estos fallos pueden estar causados por diferentes motivos, entre los más comunes encontramos las perdidas momentáneas de conexión a la red, servicios temporalmente no disponibles, tiempos de respuesta excedidos por picos de servicio elevados, etc.

Normalmente estos errores se solucionan de manera automática y en un breve lapso de tiempo de manera que si el servicio o recurso vuelve a ser invocado inmediatamente responde de manera correcta. Un ejemplo clásico de error transitorio es el fallo de conexión con la base de datos debido a un pico de conexiones simultaneas que exceden el máximo número permitido por configuración. 

<p align="center">
  <img src="https://raw.githubusercontent.com/ldavidgomez/retry-pattern/master/Retry%20Pattern_trans.png">
</p>

Sin embargo y a pesar de que son errores poco frecuentes, estos fallos deben ser gestionados de manera correcta por la aplicación para minimizar el impacto en esta. Una posible solución a este problema es la aplicación del patrón Retry. 

#### Retry Pattern

El patrón Retry está englobado dentro de lo que se ha comenzado a conocer como patrones de estabilidad y tal y como indica su nombre, consiste en reintentar una operación que ha fallado. En realidad es una definición muy simplista y es necesario añadir que según el tipo de error detectado y/o el número de intentos se pueden realizar diversas acciones:

* *Reintentar*: Si el error indica que es un fallo temporal o un fallo atípico la aplicación puede reintentar la misma operación de manera inmediata ya que seguramente no vuelva a producirse el mismo error.

* *Reintentar tras un tiempo de espera*: Si el error se ha producido debido a un problema de conexión o bien por un pico de rendimiento, puede ser prudente dejar pasar un tiempo antes de intentar volver a realizar la operación.

* *Cancelar*: Si el error indica que no nos encontramos ante un fallo temporal la operación debería ser cancelada y el error reportado o gestionado de manera adecuada.

Estas acciones pueden combinarse para crear una política de reintentos ajustada a las necesidades de nuestra aplicación.

<p align="center">
  <img src="https://raw.githubusercontent.com/ldavidgomez/retry-pattern/master/Retry_pattern_flow.png">
</p>

Este sería un ejemplo de una implementación simple en Kotlin donde solo se tiene en cuenta el número de intentos fallidos:

```kotlin
    fun run(action: () -> T): T {
        return try {
            action.invoke()
        } catch (e: Exception) {
            retry(action)
        }

    }

    @Throws(RuntimeException::class)
    private fun retry(action: () -> T): T {
        retryCounter = 1
        while (retryCounter < maxRetries) {
            try {
                return action.invoke()
            } catch (ex: Exception) {
                retryCounter++
                if (retryCounter >= maxRetries) {
                    break
                }
            }

        }
        throw RuntimeException("Command fails on all retries")
    }
```

Por supuesto las implementaciones pueden ser mucho más sofisticadas. Podemos tener, por ejemplo, una implementación que se inicie con una política de reintentos consecutivos ya que lo normal es que el servicio se recupere rápidamente. Si tras un número de reintentos consecutivos el error continúa, podemos pasar a incluir un tiempo de espera prudencial entre reintentos y finalmente si el servicio sigue sin recuperarse podemos proceder a cancelar la operación. 

**La complejidad de la implementación debe responder a las necesidades reales de nuestra aplicación y a los requerimientos de negocio.**

Existen librerías que implementan de manera muy sencilla el patrón Retry, como por ejemplo Spring Retry.

```kotin
	@Configuration
	@EnableRetry
	class Application {

	    @Bean
	    fun service(): Service {
	        return Service()
	    }

	}
```


```kotlin
   @Service
   class Service {

    	@Retryable(maxAttempts = 2, include = [RemoteAccessException::class])
    	fun service() {
            // ... do something
            logger.info("Success calling external service")
        }
        
        @Recover
        fun recover(e: RemoteAccessException) {
            // ... do something when call to service fails
            logger.error("Recover for external service")
        }
    }
```

Como podemos ver la implementación mediante librería es muy sencilla.
En el ejemplo podemos observar que lo primero que hay que hacer es configurar la aplicación con la anotación `@EnableRetry`.

A continuación añadimos la anotación `@Retryable` con el que se indica el método que va a ser 'reintentable' en caso de error. La anotación `@Recover` indica por donde continuará la ejecución en el caso de que se superen el número máximo de intentos (`maxAttempst = 2`) y siempre y cuando el error sea del tipo `RemoteAccessException`.

Podemos comprobar en la salida por consola el flujo del patrón. La ejecución se ha realizado correctamente hasta que ha encontrado un error transitorio, en ese momento ha reintentado la operación dos veces tal y como se ha especificado en la configuración y al continuar dándose el mismo error ha salido por el método de recover.

```bash
INFO 81668 --- [ Logge : RetryService ] - Calling external service...
INFO 81668 --- [ Logge : RetryService ] - Success calling external service
INFO 81668 --- [ Logge : RetryService ] - Calling external service...
INFO 81668 --- [ Logge : RetryService ] - Success calling external service
INFO 81668 --- [ Logge : RetryService ] - Calling external service...
INFO 81668 --- [ Logge : RetryService ] - Calling external service...
ERROR 81668 --- [Logger : RetryService ] - Recover for external service
```

Este patrón funciona muy bien cuando los errores son transitorios, esporádicos y se solucionan en una llamada posterior, pero *se deben tener en cuenta una serie de consideraciones al aplicarlo*:

* *El tipo de error:* debe ser un error que nos indique que puede recuperarse rápidamente.

* *La criticidad del error:* reintentar la operación puede influir negativamente en el rendimiento de la aplicación. En algunas situaciones es más óptimo gestionar el error y pedir al usuario que decida si quiere reintentar la operación.

* *La política de reintentos:* una política de continuos reintentos de la operación, especialmente sin tiempos de espera, podría empeorar el estado del servicio remoto.

* *Efectos colaterales:* Si la operación no es idempotente no se puede garantizar que reintentar su ejecución concluya  con el resultado esperado.


**No es recomendable utilizar este patrón para:**

* La gestión de errores no transitorios y que no están relacionados con fallos de conexión o servicio (como los errores de lógica de negocio).
 
* Los errores de larga duración. El tiempo de espera y los recursos necesarios son demasiado elevados. Para estos casos existen soluciones como la aplicación del patrón Circuit Breaker del que hablaremos en otro artículo.

Por último indicar que es altamente recomendable guarda un registro de las operaciones fallidas ya que es una información de gran utilidad para ayudar a dimensionar correctamente las infraestructuras de un proyecto y a encontrar errores recurrentes y silenciados por la gestión de errores de la aplicación.

Podéis encontrar los ejemplos completos tratados en este artículo en nuestro [github].

[github]: https://github.com/ldavidgomez/retry-pattern
