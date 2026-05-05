package br.com.sfpacim.backend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import br.com.sfpacim.backend.services.interfaces.EmailService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementação híbrida de {@link EmailService} para ambiente de
 * desenvolvimento (Dev).
 *
 * <p>
 * Combina duas funções essenciais para testes:
 * <ol>
 * <li>Registra o conteúdo do e-mail no console (Log) para depuração
 * rápida.</li>
 * <li>Realiza o disparo via {@link JavaMailSender} para captura em ferramentas
 * como Mailhog.</li>
 * </ol>
 *
 * @author Matheus F. N. Pereira
 */
@Slf4j
@Service
@Profile("dev")
public class DevEmailService implements EmailService {

    private final JavaMailSender mailSender;

    /**
     * O remetente é injetado via configuração.
     */
    @Value("${spring.mail.username:nao-responda@sfpacim.local}")
    private String remetente;

    /**
     * Construtor para injeção do JavaMailSender.
     *
     * @param mailSender O bean configurado para apontar para o Mailhog.
     */
    public DevEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void enviar(String destinatario, String assunto, String mensagem) {
        log.info("--------------------------------------------------");
        log.info("SIMULAÇÃO DE ENVIO DE E-MAIL");
        log.info("Para: {}", destinatario);
        log.info("Assunto: {}", assunto);
        log.info("Mensagem: \n{}", mensagem);
        log.info("--------------------------------------------------");

        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setFrom(remetente);
            email.setTo(destinatario);
            email.setSubject(assunto);
            email.setText(mensagem);

            mailSender.send(email);

            log.info("E-mail enviado com sucesso para {} utilizando as configurações de desenvolvimento.",
                    destinatario);
        } catch (Exception e) {
            log.warn("Não foi possível enviar o e-mail utilizando as configurações de desenvolvimento.", e);
        }
    }
}
