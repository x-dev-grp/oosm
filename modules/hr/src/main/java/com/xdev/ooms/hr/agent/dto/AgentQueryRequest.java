package com.xdev.ooms.hr.agent.dto;

public class AgentQueryRequest {

    private String prompt;
    private boolean confirmed;
    private String toolHint;

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String getToolHint() {
        return toolHint;
    }

    public void setToolHint(String toolHint) {
        this.toolHint = toolHint;
    }
}
