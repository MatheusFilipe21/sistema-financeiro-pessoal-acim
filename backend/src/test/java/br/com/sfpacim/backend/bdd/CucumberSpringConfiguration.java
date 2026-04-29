package br.com.sfpacim.backend.bdd;

import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import jakarta.annotation.PostConstruct;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import br.com.sfpacim.backend.config.SegurancaConfig;
import br.com.sfpacim.backend.exceptions.TratadorDeErrosGlobal;
import br.com.sfpacim.backend.services.interfaces.EmailService;

/**
 * Classe responsável exclusivamente por inicializar o contexto do Spring
 * para os testes do Cucumber.
 *
 * @author Matheus F. N. Pereira
 */
@ActiveProfiles("test")
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "api.security.token.secret=chave-secreta-de-teste-minimo-32-bytes-para-jjwt",
        "api.security.token.expiration-ms=3600000"
})
@Import({ SegurancaConfig.class, TratadorDeErrosGlobal.class })
public class CucumberSpringConfiguration {

    /**
     * Cria um Mock (falso) do EmailService.
     * O Spring vai injetar este mock no AutenticacaoService.
     * Isso evita erros de dependência e impede o envio real de emails nos testes.
     */
    @MockitoBean
    private EmailService emailService;

    /**
     * Porta aleatória onde o servidor de teste subiu.
     * Injetada automaticamente pelo Spring devido ao WebEnvironment.RANDOM_PORT.
     */
    @LocalServerPort
    protected int porta;

    /**
     * Configura o RestAssured globalmente assim que a classe é inicializada.
     * 
     * <p>
     * Isso evita a necessidade de configurar porta e URL base em cada Step
     * individual.
     * Executado automaticamente após a injeção de dependências (@PostConstruct).
     */
    @PostConstruct
    public void configurarRestAssured() {
        RestAssured.port = porta;
        RestAssured.basePath = "/api";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
