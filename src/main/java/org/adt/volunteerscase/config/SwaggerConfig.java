package org.adt.volunteerscase.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "jwtAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@OpenAPIDefinition(
        info = @Info(
                title = "Volunteers API",
                version = "1.0",
                description = "documentation for backend endpoints"
        ),
        servers = {
                @Server(url = "https://adt.rss14.ru/", description = "Production"),
                @Server(url = "http://localhost:8080/", description = "Dev")
        }
)
public class SwaggerConfig {}