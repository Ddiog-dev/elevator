package be.alexis.elevator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {
    @Value("1.0.0")
    private String version;

    @Value("Elevator")
    private String name;

    @Bean
    public OpenAPI opcOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(name + " - Api Documentation")
                        .description("This is api documentation of " + name + " backend")
                        .version(version)
                );
    }
}
