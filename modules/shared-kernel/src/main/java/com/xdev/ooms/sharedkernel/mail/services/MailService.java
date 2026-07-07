package com.xdev.ooms.sharedkernel.mail.services;

import com.xdev.ooms.sharedkernel.mail.exception.MailDeliveryException;
import com.xdev.ooms.sharedkernel.mail.models.MailRequest;

public interface MailService {
    boolean isDeliveryEnabled();

    void sendEmail(MailRequest request) throws MailDeliveryException;
}
