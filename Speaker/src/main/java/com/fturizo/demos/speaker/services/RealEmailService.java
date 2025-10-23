package com.fturizo.demos.speaker.services;

import com.fturizo.demos.speaker.entities.Speaker;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Profile("email")
public class RealEmailService implements EmailService{

    private final Logger logger = LoggerFactory.getLogger(RealEmailService.class);
    private final JavaMailSender mailSender;

    public RealEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmailToSpeaker(Speaker speaker){
        try {
            var mimeMessage = mailSender.createMimeMessage();
            var message = new MimeMessageHelper(mimeMessage);
            var content = """
                    <html>
                    <h1>Greetings %s!</h1>
                    <p>You have been registered as a new speaker in our platform!</p>
                    </html>
                    """.formatted(speaker.getName());
            message.setFrom("noreply@conference-demo-service.org");
            message.setTo(speaker.getEmail());
            message.setSubject("Speaker Registration Successful!");
            message.setText(content, true);
            mailSender.send(message.getMimeMessage());
        }catch(MessagingException exception){
            logger.error("Error sending email message", exception);
        }
    }
}
