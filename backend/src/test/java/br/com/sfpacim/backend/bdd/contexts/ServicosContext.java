package br.com.sfpacim.backend.bdd.contexts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.services.TokenService;
import io.cucumber.spring.ScenarioScope;

/**
 * Contexto auxiliar para manipulação de serviços nos testes.
 *
 * @author Catherine Aussourd
 */

@Component
@ScenarioScope
public class ServicosContext {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private BancoDadosContext bancoDadosContext;
    
    /**
     * Gera um token de redefinição de senha para o usuário com o email fornecido e
     * a senha desejada, e prepara o corpo da requisição para o endpoint de redefinição de senha. 
     * @param email
     * @param senha
     * @return
     */
    public String gerarCorpoTokenRedefinicaoSenha(String email, String senha) {

      Usuario usuario = bancoDadosContext.buscarUsuarioPorEmail(email);
      String token = tokenService.gerarTokenRecuperacao(usuario);

      return  """
              {
                "token": "%s",
                "senha": "%s"
              }
              """.formatted(token, senha);
    }
}
