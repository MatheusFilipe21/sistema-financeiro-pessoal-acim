package br.com.sfpacim.backend.bdd.steps;

import org.springframework.beans.factory.annotation.Autowired;

import br.com.sfpacim.backend.bdd.contexts.CadastroUsuarioContext;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

public class CadastroUsuarioSteps {

    @Autowired
    private CadastroUsuarioContext context;

    /**
     * Step: "Dado que eu tenho os seguintes dados para o novo usuário"
     *
     * @param jsonPayload A String JSON capturada do Gherkin.
     */
    @Dado("que eu tenho os seguintes dados para o novo usuário")
    public void queEuTenhoOsSeguintesDadosParaONovoUsuario(String jsonPayload) {
        context.definirCorpoRequisicao(jsonPayload);
    }

    /**
     * Step: "Dado que já existe um usuário cadastrado com email {string}".
     */
    @Dado("que já existe um usuário cadastrado com email {string}")
    public void dadoQueJaExisteUmUsuarioCadastradoComEmail(String email) {
        context.criarUsuarioParaCenarioDeTeste(email);
    }

    /**
     * Step: "Quando o cliente faz uma requisição POST para {string}"
     */
    @Quando("o cliente faz uma requisição POST para {string}")
    public void oClienteFazUmaRequisicaoPostPara(String url) {
        context.executarPost(url);
    }

    /**
     * Step: "Então o status da resposta deve ser {int}"
     *
     * @param codigoStatus O código de status (ex: 201) esperado.
     */
    @Então("o status da resposta deve ser {int}")
    public void oStatusDaRespostaDeveSer(int codigoStatus) {
        context.verificarCodigoStatus(codigoStatus);
    }

    /**
     * Step: "E o corpo da resposta deve conter o campo {string}"
     *
     * @param campo O nome do campo a ser verificado ("id").
     */
    @Então("o corpo da resposta deve conter o campo {string}")
    public void oCorpoDaRespostaDeveConterOCampo(String campo) {
        context.verificarCorpoContemCampo(campo);
    }

    /**
     * Step: "E o campo {string} na resposta deve ser {string}"
     *
     * @param campo         O nome do campo ("email").
     * @param valorEsperado O valor exato esperado ("matheusfnpereira@gmail.com").
     */
    @Então("o campo {string} na resposta deve ser {string}")
    public void oCampoNaRespostaDeveSer(String campo, String valorEsperado) {
        context.verificarValorCampoResposta(campo, valorEsperado);
    }

    /**
     * Step: "E o campo {string} deve conter o item {string}"
     * 
     * @param campo         O nome do campo lista (ex: "erros").
     * @param valorEsperado O valor que deve constar na lista.
     */
    @Então("o campo {string} deve conter o item {string}")
    public void oCampoDeveConterOItem(String campo, String valorEsperado) {
        context.verificarListaContemValor(campo, valorEsperado);
    }
}
