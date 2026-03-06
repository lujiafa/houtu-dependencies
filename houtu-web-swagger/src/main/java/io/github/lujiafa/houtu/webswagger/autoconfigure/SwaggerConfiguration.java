package io.github.lujiafa.houtu.webswagger.autoconfigure;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@AutoConfiguration
@Profile({"dev", "test"})
public class SwaggerConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Docs")
                        .description("This is the Swagger API documentation.")
                        .version("1.0.0")
                );
    }

}
