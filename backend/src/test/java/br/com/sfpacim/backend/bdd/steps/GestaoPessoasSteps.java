package br.com.sfpacim.backend.bdd.steps;

import org.springframework.beans.factory.annotation.Autowired;

import br.com.sfpacim.backend.bdd.contexts.BancoDadosContext;
import io.cucumber.java.pt.Dado;

/**
 * Steps específicos para o cenário de gestão de Pessoas.
 * 
 * <p>
 * Esta classe contém passos que são diretamente relacionados ao processo de
 * gestão de Pessoas.
 *
 * @author Ilka Berenguer
 */
public class GestaoPessoasSteps {

    @Autowired
    private BancoDadosContext bancoDadosContext;

    /**
     * Cria uma Pessoa com o nome informado para o Usuário do e-mail.
     * 
     * @param nome  Nome da pessoa.
     * @param email Email do usuário.
     *
     * @author Ilka Berenguer
     */
    @Dado("que já existe uma pessoa cadastrada com nome {string} para o usuário com o email {string}")
    public void dadoQueJaExisteUmaPessoaCadastradaComNomeParaOUsuarioComOEmail(String nome, String email) {
        bancoDadosContext.criarPessoaComNomeParaUsuarioComOEmail(nome, email);
    }
}
