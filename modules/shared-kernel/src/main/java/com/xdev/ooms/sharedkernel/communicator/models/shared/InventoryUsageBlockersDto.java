package com.xdev.ooms.sharedkernel.communicator.models.shared;

import java.util.ArrayList;
import java.util.List;

public class InventoryUsageBlockersDto {

    private List<Blocker> blockers = new ArrayList<>();

    public boolean isBlocked() {
        return blockers != null && !blockers.isEmpty();
    }

    public List<Blocker> getBlockers() {
        return blockers;
    }

    public void setBlockers(List<Blocker> blockers) {
        this.blockers = blockers;
    }

    public static class Blocker {
        private String code;
        private String message;
        private long count;

        public Blocker() {
        }

        public Blocker(String code, String message, long count) {
            this.code = code;
            this.message = message;
            this.count = count;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }
}
