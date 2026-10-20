package br.com.apigestaodeestoque.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Gestão de Estoque e Pedidos")
                        .version("1.0.0")
                        .description("Documentação oficial do sistema de controlo de pedidos, produtos, estoque e auditoria com Spring Boot 3."));
    }
}