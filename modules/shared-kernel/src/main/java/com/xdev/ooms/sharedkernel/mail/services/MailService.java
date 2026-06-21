package com.xdev.ooms.sharedkernel.mail.services;

import com.xdev.ooms.sharedkernel.mail.models.MailRequest;
import jakarta.mail.MessagingException;

public interface MailService {
    boolean isDeliveryEnabled();

    void sendEmail(MailRequest request) throws MessagingException;
}

