package br.com.sfpacim.backend.bdd.contexts;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.springframework.test.context.TestPropertySource;

/**
 * Classe base abstrata para todos os Contextos de Teste de API.
 * <p>
 * Centraliza a configuração do Spring Boot, a injeção da porta
 * aleatória, o estado da resposta e os métodos de verificação básicos.
 * Todas as classes de Contexto API devem herdar desta classe.
 *
 * @author Matheus F. N. Pereira
 */
@TestPropertySource(properties = {
        "api.security.token.secret=chave-secreta-de-teste-minimo-32-bytes-para-jjwt",
        "api.security.token.expiration-ms=3600000"
})
public abstract class BaseApiContext {

    /**
     * Armazena a resposta HTTP recebida do endpoint da API.
     */
    protected Response resposta;

    /**
     * Armazena o corpo JSON que será enviado (Payload)
     */
    protected String corpoRequisicao;

    /**
     * Executa uma requisição POST genérica para o endpoint informado.
     *
     * @param endpoint A URL relativa (ex: "/autenticacao/cadastro").
     */
    public void executarPost(String endpoint) {
        endpoint = endpoint.replace("/api", "");

        this.resposta = given()
                .contentType("application/json")
                .body(this.corpoRequisicao)
                .when()
                .post(endpoint);
    }

    /**
     * Retorna o código de status HTTP da última resposta da API.
     *
     * @return O código de status HTTP (ex: 200, 404).
     */
    public int getCodigoStatus() {
        return resposta.getStatusCode();
    }

    /**
     * Retorna o corpo da última resposta da API como uma {@code String}.
     *
     * @return O corpo da resposta (body).
     */
    public String getCorpoResposta() {
        return resposta.getBody().asString();
    }

    /**
     * Verifica se o código de status da última resposta corresponde ao valor
     * esperado.
     *
     * @param codigoStatusEsperado O código de status a ser verificado.
     */
    public void verificarCodigoStatus(int codigoStatusEsperado) {
        assertEquals(codigoStatusEsperado, this.getCodigoStatus());
    }

    /**
     * Verifica se o corpo da última resposta corresponde ao corpo esperado.
     *
     * @param corpoEsperado O corpo da resposta (String) a ser verificado.
     */
    public void verificarCorpoResposta(String corpoEsperado) {
        assertEquals(corpoEsperado, this.getCorpoResposta());
    }

    /**
     * Método genérico para definir o corpo da requisição.
     * 
     * @param jsonPayload A String do payload.
     */
    public void definirCorpoRequisicao(String jsonPayload) {
        this.corpoRequisicao = jsonPayload;
    }

    /**
     * Retorna um objeto JsonPath para facilitar a navegação e extração de dados da
     * resposta.
     * 
     * @return JsonPath da resposta.
     */
    private JsonPath getJsonPath() {
        return resposta.jsonPath();
    }

    /**
     * Verifica se o corpo da resposta JSON contém um campo específico.
     * 
     * @param campo O nome do campo a ser verificado ("id", "email", etc.).
     */
    public void verificarCorpoContemCampo(String campo) {
        Object valorCampo = resposta.path(campo);
        assertNotNull(valorCampo,
                String.format("O corpo da resposta JSON não contém o campo esperado: '%s'. O valor retornado foi null.",
                        campo));
    }

    /**
     * Verifica se o valor de um campo específico no corpo JSON corresponde
     * ao valor esperado.
     * 
     * @param campo         O nome do campo JSON.
     * @param valorEsperado O valor (como String) esperado para aquele campo.
     */
    public void verificarValorCampoResposta(String campo, String valorEsperado) {
        String valorReal = this.getJsonPath().getString(campo);
        assertEquals(valorEsperado, valorReal,
                String.format("O campo '%s' tem valor '%s', mas deveria ser '%s'.",
                        campo, valorReal, valorEsperado));
    }
}
