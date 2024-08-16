package uk.gov.justice.digital.hmpps.prisonperson.config

import io.swagger.v3.core.util.PrimitiveType
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.DateTimeSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.security.OAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.Scopes
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfiguration(
  buildProperties: BuildProperties,
  @Value("\${api.base.url.oauth}") val oauthUrl: String,
) {
  private val version: String = buildProperties.version

  @Bean
  fun customOpenAPI(): OpenAPI = OpenAPI()
    .servers(
      listOf(
        Server().url("/").description("Current url"),
      ),
    )
    .info(
      Info().title("Prison Person API")
        .version(version)
        .description("API for access to Prison Person Data (data about a person that is specific to their time in prison)")
        .contact(Contact().name("HMPPS Digital Studio").email("feedback@digital.justice.gov.uk")),
    )
    .components(
      Components().addSecuritySchemes(
        "bearer-jwt",
        SecurityScheme()
          .type(SecurityScheme.Type.HTTP)
          .scheme("bearer")
          .bearerFormat("JWT")
          .`in`(SecurityScheme.In.HEADER)
          .name("Authorization"),
      )
        .addSecuritySchemes(
          "hmpps-auth",
          SecurityScheme()
            .flows(getFlows())
            .type(SecurityScheme.Type.OAUTH2)
            .openIdConnectUrl("$oauthUrl/.well-known/openid-configuration"),
        ),
    )
    .addSecurityItem(SecurityRequirement().addList("bearer-jwt", listOf("read", "write")))
    .addSecurityItem(SecurityRequirement().addList("hmpps-auth"))

  fun getFlows(): OAuthFlows {
    val flows = OAuthFlows()
    val clientCredflow = OAuthFlow()
    clientCredflow.tokenUrl = "$oauthUrl/oauth/token"
    val scopes = Scopes()
      .addString("read", "Allows read of data")
      .addString("write", "Allows write of data")
    clientCredflow.scopes = scopes
    val authflow = OAuthFlow()
    authflow.authorizationUrl = "$oauthUrl/oauth/authorize"
    authflow.tokenUrl = "$oauthUrl/oauth/token"
    authflow.scopes = scopes
    return flows.clientCredentials(clientCredflow).authorizationCode(authflow)
  }

  @Bean
  fun openAPICustomiser(): OpenApiCustomizer {
    PrimitiveType.enablePartialTime() // Prevents generation of a LocalTime schema which causes conflicts with java.time.LocalTime
    return OpenApiCustomizer {
      it.components.schemas.forEach { (_, schema: Schema<*>) ->
        val properties = schema.properties ?: mutableMapOf()
        for (propertyName in properties.keys) {
          val propertySchema = properties[propertyName]!!
          if (propertySchema is DateTimeSchema) {
            properties.replace(
              propertyName,
              StringSchema()
                .example("2024-06-14T10:35:17+0100")
                .format("yyyy-MM-dd'T'HH:mm:ssX")
                .description(propertySchema.description)
                .required(propertySchema.required),
            )
          }
        }
      }
    }
  }
}
