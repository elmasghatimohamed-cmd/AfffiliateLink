package ma.affilatelink.offer.services;

public interface EmailService {

    void sendEmail(String recipient, String subject, String body);

}