package br.com.sfpacim.backend.bdd.contexts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import br.com.sfpacim.backend.config.SegurancaConfig;
import br.com.sfpacim.backend.exceptions.TratadorDeErrosGlobal;
import br.com.sfpacim.backend.repositories.UsuarioRepository;
import io.cucumber.java.After;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import jakarta.annotation.PostConstruct;

/**
 * Contexto de Teste para a funcionalidade de Cadastro de Usuário (Endpoint
 * /autenticacao/cadastro).
 * 
 * <p>
 * Herda as configurações base de API e os métodos de verificação do
 * {@link BaseApiContext}.
 *
 * @author Matheus F. N. Pereira
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({ SegurancaConfig.class, TratadorDeErrosGlobal.class })
public class CadastroUsuarioContext extends BaseApiContext {

    /**
     * Porta aleatória injetada pelo Spring onde o servidor de teste está rodando.
     */
    @LocalServerPort
    protected int porta;

    /**
     * Repositório de usuários injetado para manipulação direta do banco de dados
     * durante os testes (ex: limpeza de tabelas, criação de massa de dados).
     */
    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Configura o RestAssured automaticamente assim que o contexto do Spring
     * carrega.
     * Define a porta e o caminho base (/api) para todos os testes desta classe.
     */
    @PostConstruct
    public void configurarRestAssured() {
        RestAssured.port = porta;
        RestAssured.basePath = "/api";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    /**
     * Remove todos os registros de usuários do banco de dados.
     * 
     * <p>
     * Este método é executado automaticamente após qualquer cenário
     * anotado com <b>@limparUsuarios</b>.
     */
    @After("@limparUsuarios")
    public void limparTodosOsUsuarios() {
        usuarioRepository.deleteAll();
    }

}
