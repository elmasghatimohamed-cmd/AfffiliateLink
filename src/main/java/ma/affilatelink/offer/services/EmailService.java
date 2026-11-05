package ma.affilatelink.offer.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String fromAddress
    ) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void sendEmail(String recipient, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromAddress);
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}