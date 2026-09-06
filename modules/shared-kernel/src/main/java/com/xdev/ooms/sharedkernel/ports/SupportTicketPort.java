package com.xdev.ooms.sharedkernel.ports;

/**
 * Cross-module port: open a support ticket (security module owns persistence).
 */
public interface SupportTicketPort {

    /**
     * Creates an OPEN support ticket for the current authenticated user/tenant.
     *
     * @return ticket id as string, or null if creation failed / unavailable
     */
    String create(SupportTicketCreateCommand command);
}
