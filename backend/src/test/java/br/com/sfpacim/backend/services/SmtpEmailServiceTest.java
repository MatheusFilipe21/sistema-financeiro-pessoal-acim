package br.com.sfpacim.backend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testes unitários para a classe {@link SmtpEmailService}.
 *
 * <p>
 * Verifica o fluxo de criação da mensagem e o tratamento de exceções
 * ao interagir com o JavaMailSender.
 *
 * @author Matheus F. N. Pereira
 */
@ExtendWith(MockitoExtension.class)
class SmtpEmailServiceTest {

    @InjectMocks
    private SmtpEmailService emailService;

    @Mock
    private JavaMailSender mailSender;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> messageCaptor;

    private static final String REMETENTE = "nao-responda@sfpacim.com.br";
    private static final String DESTINATARIO = "matheusfnpereira@gmail.com";
    private static final String ASSUNTO = "Bem-vindo ao SFP-ACIM";
    private static final String MENSAGEM = "Olá, seu cadastro foi realizado com sucesso.";

    /**
     * Configura o SmtpEmailService antes de cada teste.
     * Injeta a propriedade 'remetente' via ReflectionTestUtils.
     */
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "remetente", REMETENTE);
    }

    /**
     * Testa o método {@link SmtpEmailService#enviar(String, String, String)}.
     * Valida se o JavaMailSender é chamado com os dados corretos.
     */
    @Test
    @DisplayName("enviar: Deve configurar a mensagem corretamente e invocar o mailSender")
    void testeEnviar_QuandoDadosValidos_DeveEnviarEmail() {
        emailService.enviar(DESTINATARIO, ASSUNTO, MENSAGEM);

        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage emailCapturado = messageCaptor.getValue();

        assertNotNull(emailCapturado, "O objeto de e-mail não deve ser nulo");
        assertEquals(REMETENTE, emailCapturado.getFrom(), "O remetente deve ser o configurado no serviço");
        assertEquals(DESTINATARIO, emailCapturado.getTo()[0],
                "O destinatário deve corresponder ao parâmetro informado");
        assertEquals(ASSUNTO, emailCapturado.getSubject(), "O assunto deve estar correto");
        assertEquals(MENSAGEM, emailCapturado.getText(), "O corpo da mensagem deve estar correto");
    }

    /**
     * Testa o método {@link SmtpEmailService#enviar(String, String, String)}.
     * Valida se a exceção é capturada e logada (não quebrando a aplicação).
     */
    @Test
    @DisplayName("enviar: Quando ocorrer erro no envio, deve capturar a exceção e não lançar erro")
    void testeEnviar_QuandoOcorrerErroNoSmtp_DeveCapturarExcecao() {
        doThrow(new MailSendException("Erro de conexão SMTP"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> {
            emailService.enviar(DESTINATARIO, ASSUNTO, MENSAGEM);
        }, "O serviço deve tratar a exceção internamente e não repassá-la");

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
