package br.com.sfpacim.backend.bdd.contexts;

import io.cucumber.spring.ScenarioScope;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.Data;

import static io.restassured.RestAssured.given;

import org.springframework.stereotype.Component;

/**
 * Componente central de estado para Testes de API (Contexto).
 * <p>
 * Responsável por armazenar o estado da transação atual (Payload enviado e
 * Resposta recebida)
 * e executar os verbos HTTP via RestAssured.
 *
 * @author Matheus F. N. Pereira
 */
@Component
@ScenarioScope
@Data
public class ApiContext {

    /**
     * Armazena a resposta HTTP recebida do endpoint da API.
     */
    protected Response resposta;

    /**
     * Armazena o corpo JSON que será enviado (Payload).
     */
    protected String corpoRequisicao;

    /**
     * Armazena o token JWT para requisições autenticadas.
     */
    protected String tokenAutenticacao;

    /**
     * Executa uma requisição POST para o endpoint informado usando o payload
     * armazenado.
     * 
     * <p>
     * O método prepara uma {@link RequestSpecification}, configurando o
     * Content-Type como JSON
     * e anexando o corpo da requisição. Caso um token de autenticação esteja
     * presente no contexto, ele é injetado automaticamente no cabeçalho
     * 'Authorization' via Bearer Token.
     *
     * @param endpoint A URL relativa.
     */
    public void executarPost(String endpoint) {
        String endpointLimpo = endpoint.replace("/api", "");

        RequestSpecification requisicao = given()
                .contentType("application/json")
                .body(this.corpoRequisicao);

        if (this.tokenAutenticacao != null && !this.tokenAutenticacao.isBlank()) {
            requisicao.header("Authorization", "Bearer " + this.tokenAutenticacao);
        }

        this.resposta = requisicao.when()
                .post(endpointLimpo);
    }

    /**
     * Retorna o código de status HTTP da última resposta da API.
     *
     * @return O código de status HTTP.
     */
    public int getCodigoStatus() {
        if (resposta == null) {
            throw new IllegalStateException(
                    "Tentou recuperar o Status Code, mas nenhuma requisição foi executada ainda.");
        }
        return resposta.getStatusCode();
    }

    /**
     * Retorna o corpo da última resposta da API como uma {@code String}.
     *
     * @return O corpo da resposta (body).
     */
    public String getCorpoResposta() {
        if (resposta == null) {
            throw new IllegalStateException(
                    "Tentou recuperar o Corpo da Resposta, mas nenhuma requisição foi executada ainda.");
        }
        return resposta.getBody().asString();
    }
}
