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
   * a senha desejada, e prepara o corpo da requisição para o endpoint de
   * redefinição de senha.
   * 
   * @param email Email do usuário.
   * @param senha Nova senha do usuário.
   * @return O corpo da requisição em formato JSON contendo o token e a nova
   *         senha.
   */
  public String gerarCorpoTokenRedefinicaoSenha(String email, String senha) {
    Usuario usuario = bancoDadosContext.buscarUsuarioPorEmail(email);
    String token = tokenService.gerarTokenRecuperacao(usuario);

    return """
        {
          "token": "%s",
          "senha": "%s"
        }
        """.formatted(token, senha);
  }

  /**
   * Gera um JWT (JSON Web Token) real para autenticação em endpoints protegidos.
   * 
   * <p>
   * Este método utiliza o {@link TokenService} da aplicação para gerar um token
   * válido baseado nas credenciais de um usuário recuperado do banco de dados.
   *
   * @param email O e-mail do usuário para o qual o token será gerado.
   * @return Uma {@link String} contendo o JWT gerado.
   * 
   * @author Matheus F. N. Pereira
   */
  public String obterTokenAutenticacaoParaUsuario(String email) {
    Usuario usuario = bancoDadosContext.buscarUsuarioPorEmail(email);
    return tokenService.gerarToken(usuario);
  }
}
