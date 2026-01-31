package br.com.sfpacim.backend.bdd.contexts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.cucumber.spring.ScenarioScope;
import io.restassured.path.json.JsonPath;

/**
 * Contexto responsável pelas validações genéricas de API.
 * Encapsula asserções JUnit e manipulação de JsonPath para evitar repetição de
 * código nos Steps.
 */
@Component
@ScenarioScope
public class ComumValidacaoContext {

    @Autowired
    private ApiContext context;

    /**
     * Verifica se o código de status da última resposta corresponde ao valor
     * esperado.
     *
     * @param codigoStatusEsperado O código de status HTTP a ser verificado.
     */
    public void verificarCodigoStatus(int codigoStatusEsperado) {
        assertEquals(codigoStatusEsperado, context.getCodigoStatus(),
                "O Status Code retornado pela API está incorreto.");
    }

    /**
     * Verifica se o corpo da última resposta é exatamente igual à string esperada.
     *
     * @param corpoEsperado O corpo da resposta (String completa) a ser verificado.
     */
    public void verificarCorpoResposta(String corpoEsperado) {
        assertEquals(corpoEsperado, context.getCorpoResposta());
    }

    /**
     * Verifica se o corpo da resposta JSON contém um campo específico e se ele não
     * é nulo.
     * 
     * @param campo O caminho do campo a ser verificado (ex: "id",
     *              "usuario.email").
     */
    public void verificarCorpoContemCampo(String campo) {
        Object valorCampo = this.getJsonPath().get(campo);

        assertNotNull(valorCampo,
                String.format(
                        "O corpo da resposta JSON não contém o campo esperado: '%s'. O valor retornado foi null ou inexistente.JSON: %s",
                        campo, context.getCorpoResposta()));
    }

    /**
     * Verifica se o valor de um campo específico no corpo JSON corresponde ao valor
     * esperado.
     * 
     * @param campo         O caminho do campo JSON (ex: "usuario.nome").
     * @param valorEsperado O valor (como String) esperado para aquele campo.
     */
    public void verificarValorCampoResposta(String campo, String valorEsperado) {
        Object valorBruto = this.getJsonPath().get(campo);
        assertNotNull(valorBruto,
                String.format("Tentou validar o valor de '%s', mas esse campo não existe na resposta ou é nulo.",
                        campo));

        String valorReal = String.valueOf(valorBruto);
        assertEquals(valorEsperado, valorReal,
                String.format("O campo '%s' tem valor '%s', mas deveria ser '%s'.",
                        campo, valorReal, valorEsperado));
    }

    /**
     * Verifica se uma lista no corpo JSON contém um valor específico.
     * 
     * @param campo         O caminho do campo lista no JSON (ex: "erros",
     *                      "usuarios.email").
     * @param valorEsperado O valor que deve estar presente dentro desta lista.
     */
    public void verificarListaContemValor(String campo, String valorEsperado) {
        List<String> lista = this.getJsonPath().getList(campo, String.class);

        if (lista == null) {
            throw new AssertionError(String.format("O campo '%s' não foi encontrado ou não é uma lista.", campo));
        }

        // Lógica robusta: Filtra nulos, remove espaços e busca o valor
        boolean contemValor = lista.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .anyMatch(item -> item.equals(valorEsperado.trim()));

        assertTrue(contemValor,
                String.format("A lista no campo '%s' deveria conter o valor '%s'.Lista atual encontrada: %s",
                        campo, valorEsperado, lista.toString()));
    }

    /**
     * Retorna um objeto JsonPath configurado com a resposta atual para facilitar a
     * extração de dados.
     * 
     * @return Instância de JsonPath da resposta armazenada no contexto.
     */
    private JsonPath getJsonPath() {
        return context.getResposta().jsonPath();
    }
}
