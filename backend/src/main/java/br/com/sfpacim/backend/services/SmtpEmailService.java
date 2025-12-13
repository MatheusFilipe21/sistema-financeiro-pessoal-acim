package br.com.sfpacim.backend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import br.com.sfpacim.backend.services.interfaces.EmailService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementação de {@link EmailService} para ambiente de produção (SMTP).
 *
 * <p>
 * Utiliza o {@link JavaMailSender} do Spring Boot para realizar o disparo real
 * de e-mails através de um servidor SMTP externo.
 *
 * @author Matheus F. N. Pereira
 */
@Slf4j
@Service
@Profile("prod")
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    /**
     * O e-mail que aparecerá como remetente (injetado via application-prod.yaml).
     */
    @Value("${spring.mail.username}")
    private String remetente;

    /**
     * Construtor para injeção do JavaMailSender configurado pelo Spring Boot
     * Starter Mail.
     *
     * @param mailSender O bean responsável pela comunicação SMTP.
     */
    public SmtpEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void enviar(String destinatario, String assunto, String mensagem) {
        try {
            log.info("Iniciando envio de e-mail para: {} | Assunto: {}", destinatario, assunto);

            SimpleMailMessage email = new SimpleMailMessage();
            email.setFrom(remetente);
            email.setTo(destinatario);
            email.setSubject(assunto);
            email.setText(mensagem);

            mailSender.send(email);

            log.info("E-mail enviado com sucesso para: {} | Assunto: {}", destinatario, assunto);
        } catch (Exception e) {
            log.error("Erro ao enviar e-mail para: {} | Assunto: {}", destinatario, assunto, e);
        }
    }
}
