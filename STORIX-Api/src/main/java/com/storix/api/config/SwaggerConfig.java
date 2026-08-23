package com.storix.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {

        Info info = new Info().title("STORIX API 명세서")
                .description("Team STORIX Swagger UI입니다.")
                .version("0.0.1");

        String securityScheme = "JWT TOKEN";

        SecurityRequirement securityRequirement = new SecurityRequirement().addList(securityScheme);

        Components components = new Components()
                .addSecuritySchemes(securityScheme, new SecurityScheme()
                        .name(securityScheme)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("Bearer")
                        .bearerFormat("JWT"));

        // servers 를 비우면 springdoc 이 요청 기준으로 잡는다. 운영 URL 을 스펙에 박지 않기 위함
        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);

    }

}
