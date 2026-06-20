package com.xdev.ooms.security.notification.catalog;

import java.util.Map;

public class NotificationRulesSpec {

    private int version;
    private Map<String, NotificationRuleSpec> rules;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Map<String, NotificationRuleSpec> getRules() {
        return rules;
    }

    public void setRules(Map<String, NotificationRuleSpec> rules) {
        this.rules = rules;
    }

    public static class NotificationRuleSpec {
        private String module;
        private String entity;
        private String recipientAction;
        private String title;
        private String recapTemplate;
        private String routeTemplate;
        private String priority;
        private boolean pushEnabled;

        public String getModule() {
            return module;
        }

        public void setModule(String module) {
            this.module = module;
        }

        public String getEntity() {
            return entity;
        }

        public void setEntity(String entity) {
            this.entity = entity;
        }

        public String getRecipientAction() {
            return recipientAction;
        }

        public void setRecipientAction(String recipientAction) {
            this.recipientAction = recipientAction;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getRecapTemplate() {
            return recapTemplate;
        }

        public void setRecapTemplate(String recapTemplate) {
            this.recapTemplate = recapTemplate;
        }

        public String getRouteTemplate() {
            return routeTemplate;
        }

        public void setRouteTemplate(String routeTemplate) {
            this.routeTemplate = routeTemplate;
        }

        public String getPriority() {
            return priority;
        }

        public void setPriority(String priority) {
            this.priority = priority;
        }

        public boolean isPushEnabled() {
            return pushEnabled;
        }

        public void setPushEnabled(boolean pushEnabled) {
            this.pushEnabled = pushEnabled;
        }
    }
}
