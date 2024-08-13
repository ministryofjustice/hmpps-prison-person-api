package uk.gov.justice.digital.hmpps.prisonperson.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import uk.gov.justice.digital.hmpps.prisonperson.annotation.ACTIVE_CASE_LOAD_ID
import uk.gov.justice.digital.hmpps.prisonperson.annotation.SERVICE_NAME
import uk.gov.justice.digital.hmpps.prisonperson.annotation.USERNAME
import uk.gov.justice.digital.hmpps.prisonperson.client.documentservice.dto.DocumentRequestContext

@Configuration
class PhotographRequestContextConfiguration(private val documentRequestContextInterceptor: PhotographRequestContextInterceptor) : WebMvcConfigurer {
  override fun addInterceptors(registry: InterceptorRegistry) {
    log.info("Adding document request context interceptor")
    registry.addInterceptor(documentRequestContextInterceptor).addPathPatterns("/photographs/**")
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}

@Configuration
class PhotographRequestContextInterceptor : HandlerInterceptor {
  override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
    val serviceName = request.getHeader(SERVICE_NAME)?.trim()
    require(!serviceName.isNullOrEmpty()) {
      "$SERVICE_NAME header is required"
    }
    val username = request.getHeader(USERNAME)?.trim()?.takeUnless(String::isBlank)
    val activeCaseLoadId = request.getHeader(ACTIVE_CASE_LOAD_ID)?.trim()?.takeUnless(String::isBlank)

    request.setAttribute(DocumentRequestContext::class.simpleName, DocumentRequestContext(serviceName, activeCaseLoadId, username))

    return true
  }
}
