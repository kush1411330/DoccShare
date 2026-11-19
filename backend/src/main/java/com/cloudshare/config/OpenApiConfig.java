package com.cloudshare.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        description = "Enter JWT Bearer token obtained from /api/auth/login or /api/auth/register"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI cloudShareOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CloudShare REST API")
                        .description("Secure file-sharing platform with expiring, password-protected links and download analytics")
                        .version("1.0.0")
                        .contact(new Contact().name("CloudShare Team"))
                        .license(new License().name("MIT")));
    }
}
