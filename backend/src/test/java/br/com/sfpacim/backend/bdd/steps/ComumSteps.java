package br.com.sfpacim.backend.bdd.steps;

import org.springframework.beans.factory.annotation.Autowired;
import br.com.sfpacim.backend.bdd.contexts.ApiContext;
import br.com.sfpacim.backend.bdd.contexts.BancoDadosContext;
import br.com.sfpacim.backend.bdd.contexts.ComumValidacaoContext;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

/**
 * Steps Genéricos e Reutilizáveis (Shared Steps).
 * 
 * <p>
 * Esta classe atua como um orquestrador, delegando a execução para os contextos
 * especializados
 * ({@link ApiContext}, {@link BancoDadosContext},
 * {@link ComumValidacaoContext}).
 * Concentra passos que podem ser reutilizados em múltiplas features, evitando
 * duplicação de código para ações comuns como: enviar requisições HTTP, validar
 * JSONs de resposta e preparar massa de dados.
 *
 * @author Matheus F. N. Pereira
 */
public class ComumSteps {

    @Autowired
    private ApiContext apiContext;

    @Autowired
    private ComumValidacaoContext validacaoContext;

    @Autowired
    private BancoDadosContext bancoDadosContext;

    /**
     * Passo Genérico para definição de Payload (Body da requisição).
     * 
     * <p>
     * Utiliza uma Expressão Regular (Regex) para casar com qualquer frase que
     * comece com o prefixo padrão.
     * Isso permite reutilizar o mesmo método Java para diferentes contextos de
     * negócio.
     *
     * @param jsonPayload O conteúdo da DocString (JSON) fornecido no arquivo
     *                    .feature.
     */
    @Dado("que eu tenho os seguintes dados para .*$")
    @Dado("que eu tenho o seguinte dado para .*$")
    public void definirPayloadGenerico(String jsonPayload) {
        apiContext.setCorpoRequisicao(jsonPayload);
    }

    /**
     * Prepara o banco de dados criando um usuário específico antes do teste
     * (Pré-condição).
     * 
     * <p>
     * Útil para cenários de conflito (ex: tentar cadastrar email duplicado) ou
     * login.
     * A senha utilizada será sempre a padrão definida no {@link BancoDadosContext}.
     *
     * @param nome  O nome completo do usuário a ser criado.
     * @param email O e-mail do usuário a ser criado.
     */
    @Dado("que já existe um usuário cadastrado com nome {string} e email {string}")
    public void dadoQueJaExisteUmUsuarioCadastradoComNomeEEmail(String nome, String email) {
        bancoDadosContext.criarUsuarioComSenhaPadrao(nome, email);
    }

    /**
     * Executa uma requisição POST genérica para o endpoint informado.
     * 
     * <p>
     * O corpo da requisição deve ter sido definido previamente pelo passo de
     * payload.
     * A resposta é armazenada no {@link ApiContext} para validações posteriores.
     *
     * @param url O endpoint relativo (ex: "/autenticacao/cadastro").
     */
    @Quando("o cliente faz uma requisição POST para {string}")
    public void oClienteFazUmaRequisicaoPostPara(String url) {
        apiContext.executarPost(url);
    }

    /**
     * Valida o Código de Status HTTP da resposta.
     *
     * @param codigoStatus O status code esperado.
     */
    @Então("o status da resposta deve ser {int}")
    public void oStatusDaRespostaDeveSer(int codigoStatus) {
        validacaoContext.verificarCodigoStatus(codigoStatus);
    }

    /**
     * Valida se um determinado campo existe no JSON de resposta e não é nulo.
     *
     * @param campo O caminho do campo no JSON (ex: "id", "usuario.token").
     */
    @Então("o corpo da resposta deve conter o campo {string}")
    public void oCorpoDaRespostaDeveConterOCampo(String campo) {
        validacaoContext.verificarCorpoContemCampo(campo);
    }

    /**
     * Valida se o valor de um campo no JSON é estritamente igual ao esperado.
     *
     * @param campo         O caminho do campo no JSON.
     * @param valorEsperado O valor esperado como String.
     */
    @Então("o campo {string} na resposta deve ser {string}")
    public void oCampoNaRespostaDeveSer(String campo, String valorEsperado) {
        validacaoContext.verificarValorCampoResposta(campo, valorEsperado);
    }

    /**
     * Valida se uma lista (array) no JSON contém um item específico.
     * <p>
     * Muito utilizado para validar listas de erros retornadas pela API.
     * Ex: Verificar se a lista "erros.mensagem" contém "Senha inválida".
     *
     * @param campo         O caminho da lista no JSON (ex: "erros", "mensagens").
     * @param valorEsperado O texto que deve estar contido na lista.
     */
    @Então("o campo {string} deve conter o item {string}")
    public void oCampoDeveConterOItem(String campo, String valorEsperado) {
        validacaoContext.verificarListaContemValor(campo, valorEsperado);
    }

    /**
     * Valida que o corpo da resposta HTTP está vazio.
     *
     * <p>
     * Utilizado principalmente para respostas com status {@code 204 (No Content)},
     * retornadas independentemente da existência do recurso,
     * como medida de segurança para evitar a descoberta de usuários.
     */
    @Então("o corpo da resposta deve ser vazio")
    public void oCorpoDaRespostaDeveSerVazio() {
        validacaoContext.verificarCorpoRespostaVazio();
    }
}
