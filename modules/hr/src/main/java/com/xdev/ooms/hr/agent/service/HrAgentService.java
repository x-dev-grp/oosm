package com.xdev.ooms.hr.agent.service;

import com.xdev.ooms.hr.agent.dto.AgentQueryRequest;
import com.xdev.ooms.hr.agent.dto.AgentResponse;
import com.xdev.ooms.hr.agent.entity.HrAgentActionLog;
import com.xdev.ooms.hr.agent.enums.AgentRiskLevel;
import com.xdev.ooms.hr.agent.repository.HrAgentActionLogRepository;
import com.xdev.ooms.hr.common.HrPermissionSupport;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.utils.AuditHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HR Agent query entry point.
 * <p>
 * Intent resolution is <strong>deterministic keyword matching</strong> (not an LLM call).
 * A future LLM can replace {@link #resolveToolName(AgentQueryRequest)} while reusing
 * {@link HrAgentToolRegistry} tools, permission checks, confirmation gates, and action logs.
 */
@Service
public class HrAgentService {

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    private final HrAgentToolRegistry toolRegistry;
    private final HrAgentActionLogRepository actionLogRepository;

    public HrAgentService(HrAgentToolRegistry toolRegistry, HrAgentActionLogRepository actionLogRepository) {
        this.toolRegistry = toolRegistry;
        this.actionLogRepository = actionLogRepository;
    }

    @Transactional
    public AgentResponse query(AgentQueryRequest request) {
        HrPermissionSupport.requireAction("HRAGENT", Action.READ);

        if (request == null || request.getPrompt() == null || request.getPrompt().isBlank()) {
            return AgentResponse.error("Prompt is required");
        }

        String toolName = resolveToolName(request);
        if (toolName == null) {
            AgentResponse unknown = AgentResponse.error(
                    "Could not map prompt to a tool. Try keywords like: search employee, missing cnss, "
                            + "expiring contracts, pending leave, payroll, compliance, payroll cost.");
            logAction(request, "unknown", null, AgentRiskLevel.READ, false, unknown.getMessage());
            return unknown;
        }

        HrAgentToolRegistry.ToolDefinition tool = toolRegistry.get(toolName);
        if (tool == null) {
            return AgentResponse.error("Unknown tool: " + toolName);
        }

        try {
            toolRegistry.checkPermission(tool);
        } catch (RuntimeException ex) {
            logAction(request, toolName, toolName, tool.riskLevel(), false, ex.getMessage());
            return AgentResponse.error(ex.getMessage());
        }

        boolean needsConfirm = tool.riskLevel() == AgentRiskLevel.CONTROLLED_WRITE
                || tool.riskLevel() == AgentRiskLevel.CRITICAL;
        if (needsConfirm) {
            HrPermissionSupport.requireAction("HRAGENT", Action.APPROVE);
            if (!request.isConfirmed()) {
                AgentResponse pending = AgentResponse.needsConfirmation(
                        toolName,
                        toolName,
                        tool.riskLevel(),
                        "Confirmation required for " + tool.riskLevel() + " action. Resubmit with confirmed=true.");
                logAction(request, toolName, toolName, tool.riskLevel(), true, pending.getMessage());
                return pending;
            }
        }

        Map<String, String> params = extractParams(request.getPrompt());
        try {
            Object data = tool.handler().apply(params);
            AgentResponse ok = AgentResponse.ok(toolName, toolName, tool.riskLevel(), "OK", data);
            logAction(request, toolName, toolName, tool.riskLevel(), needsConfirm, summarize(data));
            return ok;
        } catch (RuntimeException ex) {
            logAction(request, toolName, toolName, tool.riskLevel(), needsConfirm, ex.getMessage());
            return AgentResponse.error(ex.getMessage());
        }
    }

    /**
     * Keyword router — replaceable by an LLM later.
     */
    private String resolveToolName(AgentQueryRequest request) {
        if (request.getToolHint() != null && !request.getToolHint().isBlank()) {
            return request.getToolHint().trim();
        }
        String p = request.getPrompt().toLowerCase(Locale.ROOT);

        if (containsAny(p, "missing cnss", "sans cnss", "cnss manquant", "listmissingcnss")) {
            return "employee.listMissingCnss";
        }
        if (containsAny(p, "search employee", "trouver employ", "chercher employ", "employee search")) {
            return "employee.search";
        }
        if (containsAny(p, "get employee", "employee get", "employé ", "employee id")) {
            return "employee.get";
        }
        if (containsAny(p, "expiring contract", "contrat expir", "contract ending", "fin de contrat")) {
            return "contract.expiring";
        }
        if (containsAny(p, "pending leave", "congé en attente", "leave pending", "demandes de congé")) {
            return "leave.pending";
        }
        if (containsAny(p, "payroll anomal", "anomalie paie", "payslip anomal")) {
            return "payroll.anomalies";
        }
        if (containsAny(p, "payroll cost", "coût paie", "cout paie", "masse salariale", "reports.payroll")) {
            return "reports.payrollCost";
        }
        if (containsAny(p, "payroll", "paie", "période de paie", "periode de paie")) {
            return "payroll.get";
        }
        if (containsAny(p, "compliance violation", "violation", "non-conform")) {
            return "compliance.violations";
        }
        if (containsAny(p, "compliance", "conformité", "conformite", "score")) {
            return "compliance.summary";
        }
        return null;
    }

    private Map<String, String> extractParams(String prompt) {
        Map<String, String> params = new HashMap<>();
        Matcher uuidMatcher = UUID_PATTERN.matcher(prompt);
        if (uuidMatcher.find()) {
            String id = uuidMatcher.group();
            params.put("id", id);
            params.put("periodId", id);
        }
        // free-text search term: after "search" / "chercher"
        String lower = prompt.toLowerCase(Locale.ROOT);
        int idx = indexOfAny(lower, "search employee", "chercher", "trouver", "search");
        if (idx >= 0) {
            String after = prompt.substring(idx).replaceAll("(?i)(search employee|chercher|trouver|search)", "").trim();
            if (!after.isBlank()) {
                params.put("q", after.split("\\s+")[0]);
            }
        }
        return params;
    }

    private void logAction(
            AgentQueryRequest request,
            String intent,
            String tool,
            AgentRiskLevel risk,
            boolean confirmationRequired,
            String result
    ) {
        HrAgentActionLog log = new HrAgentActionLog();
        log.setUserId(currentUserId());
        log.setPrompt(request.getPrompt());
        log.setIntent(intent);
        log.setToolCalled(tool);
        log.setParameters(request.getToolHint());
        log.setResult(truncate(result, 4000));
        log.setRiskLevel(risk != null ? risk : AgentRiskLevel.READ);
        log.setConfirmationRequired(confirmationRequired);
        log.setConfirmed(request.isConfirmed());
        if (request.isConfirmed()) {
            log.setApprovedBy(currentUserId());
        }
        log.setTimestamp(LocalDateTime.now());
        AuditHelper.applyAuditOnCreate(log);
        actionLogRepository.save(log);
    }

    private UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        try {
            Method getId = principal.getClass().getMethod("getId");
            Object id = getId.invoke(principal);
            if (id instanceof UUID uuid) {
                return uuid;
            }
            if (id != null) {
                return UUID.fromString(id.toString());
            }
        } catch (Exception ignored) {
            // fall through
        }
        return null;
    }

    private static boolean containsAny(String text, String... needles) {
        for (String n : needles) {
            if (text.contains(n)) {
                return true;
            }
        }
        return false;
    }

    private static int indexOfAny(String text, String... needles) {
        int best = -1;
        for (String n : needles) {
            int i = text.indexOf(n);
            if (i >= 0 && (best < 0 || i < best)) {
                best = i;
            }
        }
        return best;
    }

    private static String summarize(Object data) {
        if (data == null) {
            return "null";
        }
        return truncate(data.toString(), 4000);
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
