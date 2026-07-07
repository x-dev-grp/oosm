package com.xdev.ooms.sharedkernel.mail.exception;

public class MailDeliveryException extends Exception {

    public MailDeliveryException(String message) {
        super(message);
    }

    public MailDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
