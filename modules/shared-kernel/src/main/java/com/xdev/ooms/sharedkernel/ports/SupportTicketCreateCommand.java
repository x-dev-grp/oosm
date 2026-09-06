package com.xdev.ooms.sharedkernel.ports;

/**
 * Command to open a support ticket from another module (e.g. day-import commit failure).
 */
public class SupportTicketCreateCommand {

    private final String subject;
    private final String description;
    private final String priority;
    private final String pageUrl;

    public SupportTicketCreateCommand(String subject, String description, String priority, String pageUrl) {
        this.subject = subject;
        this.description = description;
        this.priority = priority;
        this.pageUrl = pageUrl;
    }

    public static SupportTicketCreateCommand of(String subject, String description) {
        return new SupportTicketCreateCommand(subject, description, "HIGH", null);
    }

    public String getSubject() {
        return subject;
    }

    public String getDescription() {
        return description;
    }

    public String getPriority() {
        return priority;
    }

    public String getPageUrl() {
        return pageUrl;
    }
}
