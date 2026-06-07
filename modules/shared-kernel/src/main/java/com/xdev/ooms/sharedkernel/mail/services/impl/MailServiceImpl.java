package com.xdev.ooms.sharedkernel.mail.services.impl;

import com.xdev.ooms.sharedkernel.mail.models.MailRequest;
import com.xdev.ooms.sharedkernel.mail.services.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    public MailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendEmail(MailRequest request) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
        helper.setTo(request.getTo());
        helper.setSubject(request.getSubject());
        helper.setText(request.getBody());
        mailSender.send(message);
    }
}
