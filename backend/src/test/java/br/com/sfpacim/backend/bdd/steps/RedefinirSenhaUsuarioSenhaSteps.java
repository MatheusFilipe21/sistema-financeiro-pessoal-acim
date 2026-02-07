package br.com.sfpacim.backend.bdd.steps;

import org.springframework.beans.factory.annotation.Autowired;

import br.com.sfpacim.backend.bdd.contexts.ApiContext;
import br.com.sfpacim.backend.bdd.contexts.ServicosContext;
import io.cucumber.java.pt.Dado;

/**
 * Steps específicos para o cenário de redefinição de senha do usuário.
 * 
 * <p>
 * Esta classe contém passos que são diretamente relacionados ao processo de
 * redefinir senha, como a criação de um token válido para a operação.
 *
 * @author Catherine Aussourd
 */
public class RedefinirSenhaUsuarioSenhaSteps {

    @Autowired
    private ServicosContext servicosContext;

    @Autowired
    private ApiContext apiContext;
    
    /**
     * Gera um token de redefinição de senha para o usuário com o email fornecido e
     * a senha desejada, e prepara o corpo da requisição para o endpoint de redefinição de senha.
     * @param email
     * @param senha
     */
    @Dado("que eu tenho o seguinte dado com o token gerado para o usuário de email {string} e a senha {string}")
    public void dadoQueEuTenhoOTokenGeradoParaRedefinirSenha(String email, String senha) {
        String jsonPayload = servicosContext.gerarCorpoTokenRedefinicaoSenha(email, senha);
        apiContext.setCorpoRequisicao(jsonPayload);
    }
}
