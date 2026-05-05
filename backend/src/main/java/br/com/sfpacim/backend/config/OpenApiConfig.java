package br.com.sfpacim.backend.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.sfpacim.backend.utils.MetodosUteis;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
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
     * Atributo que armazena o logger da classe.
     */
    private static final Logger log = LoggerFactory.getLogger(OpenApiConfig.class);

    /**
     * Atributo que armazena a instância do MessageSource para a resolução de
     * placeholders.
     */
    private final MessageSource messageSource;

    /**
     * Atributo que armazena a instância do ObjectMapper para a serialização e
     * desserialização de objetos.
     */
    private final ObjectMapper objectMapper;

    /**
     * Construtor da classe.
     * 
     * @param messageSource A instância do MessageSource para a resolução de
     *                      placeholders.
     * @param objectMapper  Instância do Jackson para converter strings de exemplo
     *                      em JSON.
     */
    public OpenApiConfig(MessageSource messageSource, ObjectMapper objectMapper) {
        this.messageSource = messageSource;
        this.objectMapper = objectMapper;
    }

    /**
     * Configura o esquema de segurança (Bearer Auth) para autenticação JWT e define
     * a informação global da API.
     * 
     * @return Um OpenAPI personalizado.
     */
    @Bean
    OpenAPI customOpenAPI() {
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

    /**
     * Varre a árvore inteira do Swagger após ela estar montada para resolver
     * placeholders.
     * 
     * @return Um OpenApiCustomizer personalizado.
     */
    @Bean
    OpenApiCustomizer customizarOpenApiGlobalmente() {
        return openApi -> {

            if (openApi.getComponents() != null && openApi.getComponents().getSchemas() != null) {
                openApi.getComponents().getSchemas().values().forEach(this::resolverPlaceholdersNoSchema);
            }

            if (openApi.getTags() != null) {
                openApi.getTags().forEach(tag -> {
                    if (tag.getName() != null) {
                        tag.setName(traduzir(tag.getName()));
                    }

                    if (tag.getDescription() != null) {
                        tag.setDescription(traduzir(tag.getDescription()));
                    }
                });
            }

            if (openApi.getPaths() != null) {
                openApi.getPaths().values()
                        .forEach(pathItem -> pathItem.readOperations().forEach(this::resolverPlaceholdersOperacao));
            }
        };
    }

    /**
     * Extrai a chave do formato ${chave} e busca a tradução no idioma atual.
     * 
     * @param texto O texto a ser traduzido.
     * @return O texto traduzido.
     */
    private String traduzir(String texto) {
        if (texto != null && texto.startsWith("${") && texto.endsWith("}")) {
            String chave = texto.substring(2, texto.length() - 1);
            return MetodosUteis.obterMensagem(messageSource, chave);
        }
        return texto;
    }

    /**
     * Extrai o texto limpo de um objeto, removendo as aspas literais
     * caso o SpringDoc tenha convertido a string em um nó do Jackson (TextNode).
     * 
     * @param objeto O objeto a ser extraído.
     * @return O texto limpo do objeto.
     */
    private String extrairTextoLimpo(Object objeto) {
        if (objeto instanceof String str) {
            return str;
        } else if (objeto instanceof JsonNode node) {
            return node.asText();
        }
        return objeto != null ? objeto.toString() : null;
    }

    private void resolverPlaceholdersOperacao(Operation operation) {
        if (operation.getTags() != null) {
            List<String> tagsTraduzidas = operation.getTags().stream().map(this::traduzir).toList();
            operation.setTags(tagsTraduzidas);
        }

        if (operation.getSummary() != null) {
            operation.setSummary(traduzir(operation.getSummary()));
        }

        if (operation.getDescription() != null) {
            operation.setDescription(traduzir(operation.getDescription()));
        }

        if (operation.getParameters() != null) {
            operation.getParameters().forEach(this::resolverPlaceholdersParametro);
        }

        if (operation.getResponses() != null) {
            operation.getResponses().values().forEach(this::resolverPlaceholdersResposta);
        }
    }

    /**
     * Resolve placeholders dentro de objetos Schema (Corpos de Requisição / DTOs).
     * 
     * @param schema O schema a ser inspecionado.
     */
    private void resolverPlaceholdersNoSchema(Schema<?> schema) {
        if (schema == null) {
            return;
        }

        if (schema.getDescription() != null) {
            schema.setDescription(traduzir(schema.getDescription()));
        }

        if (schema.getExample() != null) {
            String valorLimpo = extrairTextoLimpo(schema.getExample());

            if (valorLimpo != null && valorLimpo.startsWith("${") && valorLimpo.endsWith("}")) {
                String traduzido = traduzir(valorLimpo);
                try {
                    schema.setExample(objectMapper.readTree(traduzido));
                } catch (Exception _) {
                    schema.setExample(traduzido);
                }
            }
        }

        if (schema.getProperties() != null) {
            schema.getProperties().values().forEach(this::resolverPlaceholdersNoSchema);
        }

        if (schema.getItems() != null) {
            resolverPlaceholdersNoSchema(schema.getItems());
        }
    }

    /**
     * Resolve placeholders para um parâmetro específico da API (ex: Query Params).
     * 
     * @param param O parâmetro a ser resolvido.
     */
    private void resolverPlaceholdersParametro(Parameter param) {
        if (param.getName() != null) {
            String chavePageable = switch (param.getName()) {
                case "page" -> "pageable.page";
                case "size" -> "pageable.size";
                case "sort" -> "pageable.sort";
                default -> null;
            };

            if (chavePageable != null) {
                String traducao = MetodosUteis.obterMensagem(messageSource, chavePageable);

                if (!traducao.equals(chavePageable)) {
                    param.setDescription(traducao);
                }
            }
        }

        if (param.getDescription() != null) {
            param.setDescription(traduzir(param.getDescription()));
        }

        if (param.getExample() != null) {
            param.setExample(traduzir(param.getExample().toString()));
        }
    }

    /**
     * Resolve placeholders nas descrições de resposta e injeta os JSONs de exemplo.
     * 
     * @param apiResponse A resposta HTTP documentada a ser inspecionada.
     */
    private void resolverPlaceholdersResposta(ApiResponse apiResponse) {
        if (apiResponse == null) {
            return;
        }

        if (apiResponse.getDescription() != null) {
            apiResponse.setDescription(traduzir(apiResponse.getDescription()));
        }

        if (apiResponse.getContent() != null) {
            apiResponse.getContent().values().forEach(this::resolverPlaceholdersMediaType);
        }

        if (apiResponse.getHeaders() != null) {
            apiResponse.getHeaders().values().forEach(header -> {
                if (header.getDescription() != null) {
                    header.setDescription(traduzir(header.getDescription()));
                }

                if (header.getSchema() != null)
                    resolverPlaceholdersNoSchema(header.getSchema());
            });
        }
    }

    /**
     * Varre os exemplos dentro de um MediaType específico.
     * 
     * @param mediaType O MediaType a ser inspecionado.
     */
    private void resolverPlaceholdersMediaType(MediaType mediaType) {
        if (mediaType.getExamples() != null) {
            Map<String, Example> exemplosTraduzidos = new LinkedHashMap<>();

            for (Map.Entry<String, Example> entry : mediaType.getExamples()
                    .entrySet()) {
                String nomeDropdown = entry.getKey();
                Example exemploObjeto = entry.getValue();

                if (nomeDropdown != null && nomeDropdown.startsWith("${") && nomeDropdown.endsWith("}")) {
                    String chaveNome = nomeDropdown.substring(2, nomeDropdown.length() - 1);
                    nomeDropdown = MetodosUteis.obterMensagem(messageSource, chaveNome);
                }

                if (exemploObjeto.getDescription() != null && exemploObjeto.getDescription().startsWith("${")) {
                    String chaveDesc = exemploObjeto.getDescription().substring(2,
                            exemploObjeto.getDescription().length() - 1);
                    exemploObjeto.setDescription(MetodosUteis.obterMensagem(messageSource, chaveDesc));
                }

                if (exemploObjeto.getValue() instanceof String valorString && valorString.startsWith("${")
                        && valorString.endsWith("}")) {
                    String chaveValor = valorString.substring(2, valorString.length() - 1);
                    String jsonTraduzido = MetodosUteis.obterMensagem(messageSource, chaveValor);

                    try {
                        JsonNode jsonNode = objectMapper.readTree(jsonTraduzido);
                        exemploObjeto.setValue(jsonNode);
                    } catch (Exception e) {
                        exemploObjeto.setValue(jsonTraduzido);
                    }
                }
                exemplosTraduzidos.put(nomeDropdown, exemploObjeto);
            }
            mediaType.setExamples(exemplosTraduzidos);
        }

        if (mediaType.getExample() != null) {
            String valorLimpo = extrairTextoLimpo(mediaType.getExample());
            if (valorLimpo != null && valorLimpo.startsWith("${") && valorLimpo.endsWith("}")) {
                String traduzido = traduzir(valorLimpo);
                try {
                    mediaType.setExample(objectMapper.readTree(traduzido));
                } catch (Exception _) {
                    mediaType.setExample(traduzido);
                }
            }
        }

        if (mediaType.getExamples() != null) {
            mediaType.getExamples().values().forEach(this::resolverExemploUnico);
        }
    }

    /**
     * Converte o placeholder de um único exemplo em um nó JSON real.
     * 
     * @param example O exemplo a ser inspecionado.
     */
    private void resolverExemploUnico(Example example) {
        if (example.getValue() == null) {
            return;
        }

        String valorLimpo = extrairTextoLimpo(example.getValue());

        if (valorLimpo != null && valorLimpo.startsWith("${") && valorLimpo.endsWith("}")) {
            String traduzido = traduzir(valorLimpo);
            try {
                example.setValue(objectMapper.readTree(traduzido));
            } catch (Exception e) {
                log.error("Erro ao converter JSON do properties para a chave {}: {}", valorLimpo, e.getMessage());
                example.setValue(traduzido);
            }
        }
    }
}
