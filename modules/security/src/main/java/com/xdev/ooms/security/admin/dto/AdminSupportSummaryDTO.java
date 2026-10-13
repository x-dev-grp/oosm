package com.xdev.ooms.security.admin.dto;

public class AdminSupportSummaryDTO {
    private long open;
    private long inProgress;
    private long resolved;
    private long closed;

    public long getOpen() {
        return open;
    }

    public void setOpen(long open) {
        this.open = open;
    }

    public long getInProgress() {
        return inProgress;
    }

    public void setInProgress(long inProgress) {
        this.inProgress = inProgress;
    }

    public long getResolved() {
        return resolved;
    }

    public void setResolved(long resolved) {
        this.resolved = resolved;
    }

    public long getClosed() {
        return closed;
    }

    public void setClosed(long closed) {
        this.closed = closed;
    }
}
