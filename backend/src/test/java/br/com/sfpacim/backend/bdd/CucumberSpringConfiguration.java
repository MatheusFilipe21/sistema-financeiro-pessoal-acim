package br.com.sfpacim.backend.bdd;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import br.com.sfpacim.backend.bdd.contexts.CadastroUsuarioContext;
import br.com.sfpacim.backend.bdd.contexts.LoginUsuarioContext;
import br.com.sfpacim.backend.config.SegurancaConfig;
import br.com.sfpacim.backend.exceptions.TratadorDeErrosGlobal;
import br.com.sfpacim.backend.services.interfaces.EmailService;

/**
 * Classe responsável exclusivamente por inicializar o contexto do Spring
 * para os testes do Cucumber.
 *
 * @author Matheus F. N. Pereira
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "api.security.token.secret=chave-secreta-de-teste-minimo-32-bytes-para-jjwt",
        "api.security.token.expiration-ms=3600000"
})
@Import({ SegurancaConfig.class, TratadorDeErrosGlobal.class, LoginUsuarioContext.class,
        CadastroUsuarioContext.class })
public class CucumberSpringConfiguration {

    /**
     * Cria um Mock (falso) do EmailService.
     * O Spring vai injetar este mock no AutenticacaoService.
     * Isso evita erros de dependência e impede o envio real de emails nos testes.
     */
    @MockitoBean
    private EmailService emailService;
}
