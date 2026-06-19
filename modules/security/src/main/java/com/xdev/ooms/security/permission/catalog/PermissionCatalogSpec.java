package com.xdev.ooms.security.permission.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PermissionCatalogSpec {

    private int version;
    private Map<String, List<String>> actionProfiles = new LinkedHashMap<>();
    private Map<String, EntitySpec> entities = new LinkedHashMap<>();
    private List<RoleMirrorSpec> roleMirrors = List.of();

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Map<String, List<String>> getActionProfiles() {
        return actionProfiles;
    }

    public void setActionProfiles(Map<String, List<String>> actionProfiles) {
        this.actionProfiles = actionProfiles;
    }

    public Map<String, EntitySpec> getEntities() {
        return entities;
    }

    public void setEntities(Map<String, EntitySpec> entities) {
        this.entities = entities;
    }

    public List<RoleMirrorSpec> getRoleMirrors() {
        return roleMirrors;
    }

    public void setRoleMirrors(List<RoleMirrorSpec> roleMirrors) {
        this.roleMirrors = roleMirrors != null ? roleMirrors : List.of();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EntitySpec {
        private String module;
        private String description;
        private String section;
        private String profile;
        private List<String> permissions;
        private List<String> addActions;
        private List<String> removeActions;
        private List<String> legacyAliases;

        public String getModule() {
            return module;
        }

        public void setModule(String module) {
            this.module = module;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getSection() {
            return section;
        }

        public void setSection(String section) {
            this.section = section;
        }

        public String getProfile() {
            return profile;
        }

        public void setProfile(String profile) {
            this.profile = profile;
        }

        public List<String> getPermissions() {
            return permissions;
        }

        public void setPermissions(List<String> permissions) {
            this.permissions = permissions;
        }

        public List<String> getAddActions() {
            return addActions;
        }

        public void setAddActions(List<String> addActions) {
            this.addActions = addActions;
        }

        public List<String> getRemoveActions() {
            return removeActions;
        }

        public void setRemoveActions(List<String> removeActions) {
            this.removeActions = removeActions;
        }

        public List<String> getLegacyAliases() {
            return legacyAliases;
        }

        public void setLegacyAliases(List<String> legacyAliases) {
            this.legacyAliases = legacyAliases;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RoleMirrorSpec {
        private String sourceModule;
        private String sourceEntity;
        private String targetModule;
        private String targetEntity;

        public String getSourceModule() {
            return sourceModule;
        }

        public void setSourceModule(String sourceModule) {
            this.sourceModule = sourceModule;
        }

        public String getSourceEntity() {
            return sourceEntity;
        }

        public void setSourceEntity(String sourceEntity) {
            this.sourceEntity = sourceEntity;
        }

        public String getTargetModule() {
            return targetModule;
        }

        public void setTargetModule(String targetModule) {
            this.targetModule = targetModule;
        }

        public String getTargetEntity() {
            return targetEntity;
        }

        public void setTargetEntity(String targetEntity) {
            this.targetEntity = targetEntity;
        }
    }
}
