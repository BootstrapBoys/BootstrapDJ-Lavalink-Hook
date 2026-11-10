package lavalink.server.config

import dev.arbjerg.lavalink.api.RestInterceptor
import dev.arbjerg.lavalink.protocol.v4.json
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.KotlinSerializationJsonHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
@EnableWebMvc
class WebConfiguration(private val interceptors: List<RestInterceptor>) : WebMvcConfigurer {

    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        converters.add(StringHttpMessageConverter())
        converters.add(KotlinSerializationJsonHttpMessageConverter(json))
        converters.add(MappingJackson2HttpMessageConverter())
    }

    /**
     * The audio-tap plugin streams through a Spring StreamingResponseBody.
     * @EnableWebMvc disables Spring Boot's spring.mvc.async.request-timeout
     * auto-configuration, so the MVC default closes every tap after 30 s.
     * Keep a day-scale ceiling; client disconnects still terminate the stream.
     */
    override fun configureAsyncSupport(configurer: AsyncSupportConfigurer) {
        configurer.setDefaultTimeout(86_400_000L)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        interceptors.forEach { registry.addInterceptor(it) }
    }

}
