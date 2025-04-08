package com.beyond.university.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "University Service",
                description = "University Service API",
                version = "v1.0.0"
        )
)
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
//        Server server = new Server().url("http://beyond.university-api.com:31895");
        Server server = new  Server().url("https://beyond.university-api.com:31050");

        return new OpenAPI()
                .addServersItem(server)
                .components(new Components().addSecuritySchemes(
                        // 보안 스키마의 이름
                        "bearer-auth",
                        // 보안 방식을 정의
                        new SecurityScheme()
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .type(SecurityScheme.Type.HTTP)
                        // .name("Authorization")
                ))
                // 보안 방식 적용
                .addSecurityItem(
                        new SecurityRequirement().addList("bearer-auth")
                );
    }
}