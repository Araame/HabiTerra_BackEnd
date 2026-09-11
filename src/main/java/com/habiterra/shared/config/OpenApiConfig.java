package com.habiterra.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI habiTerraOpenApi() {
        return new OpenAPI()
            .info(new Info().title("HabiTerra API")
                .description("API REST de la plateforme HabiTerra").version("v1"))
            .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                .description("Saisir uniquement accessToken, sans le prefixe Bearer.")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
