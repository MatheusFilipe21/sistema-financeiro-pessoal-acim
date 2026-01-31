package br.com.sfpacim.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Configuração da documentação da API utilizando o OpenAPI 3 (Swagger).
 * 
 * <p>
 * Define as informações globais da API (título, versão, contato) e
 * o esquema de segurança (Bearer Auth) para autenticação JWT.
 *
 * @author Matheus F. N. Pereira
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configura e define o Bean principal do OpenAPI.
     *
     * @return A instância de OpenAPI configurada.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("token-jwt",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")))
                .addSecurityItem(new SecurityRequirement().addList("token-jwt"))
                .info(new Info()
                        .title("Sistema Financeiro Pessoal ACIM (SFP-ACIM) API Backend")
                        .description("API RESTful para o SFP-ACIM.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Matheus F. N. Pereira")
                                .email("matheusfnpereira@gmail.com")));
    }
}
