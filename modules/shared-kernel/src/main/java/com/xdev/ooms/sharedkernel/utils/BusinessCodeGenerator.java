package com.xdev.ooms.sharedkernel.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
public class BusinessCodeGenerator {

    private static final int PREFIX_LENGTH = 2;
    private static final int SEQUENCE_WIDTH = 3;

    @PersistenceContext
    private EntityManager entityManager;

    public synchronized String generate(Class<?> entityClass, String codeFieldName) {
        return generate(entityClass, codeFieldName, entityClass.getSimpleName());
    }

    public synchronized String generate(Class<?> entityClass, String codeFieldName, String prefixSource) {
        Objects.requireNonNull(entityClass, "entityClass is required");
        Objects.requireNonNull(codeFieldName, "codeFieldName is required");

        String prefix = normalizePrefix(prefixSource);
        String yearSuffix = currentYearSuffix();
        String pattern = prefix + "-" + yearSuffix + "-%";

        int nextSequence = findExistingCodes(entityClass, codeFieldName, pattern).stream()
                .mapToInt(code -> extractSequence(code, prefix, yearSuffix))
                .max()
                .orElse(0) + 1;

        return String.format(Locale.ROOT, "%s-%s-%0" + SEQUENCE_WIDTH + "d", prefix, yearSuffix, nextSequence);
    }

    private List<String> findExistingCodes(Class<?> entityClass, String codeFieldName, String pattern) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<?> root = query.from(entityClass);
        Expression<String> codeField = root.get(codeFieldName).as(String.class);

        query.select(codeField)
                .where(
                        cb.and(
                                cb.isNotNull(codeField),
                                cb.like(cb.upper(codeField), pattern.toUpperCase(Locale.ROOT))
                        )
                );

        return entityManager.createQuery(query).getResultList();
    }

    private int extractSequence(String code, String prefix, String yearSuffix) {
        if (code == null || code.isBlank()) {
            return 0;
        }

        String normalized = code.trim().toUpperCase(Locale.ROOT);
        String expectedStart = prefix + "-" + yearSuffix + "-";
        if (!normalized.startsWith(expectedStart)) {
            return 0;
        }

        String suffix = normalized.substring(expectedStart.length());
        if (!suffix.matches("\\d+")) {
            return 0;
        }

        try {
            return Integer.parseInt(suffix);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String normalizePrefix(String prefixSource) {
        String cleaned = Objects.requireNonNull(prefixSource, "prefixSource is required")
                .trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]", "");

        if (cleaned.length() < PREFIX_LENGTH) {
            throw new IllegalArgumentException("Business code prefix must contain at least two letters or digits");
        }

        return cleaned.substring(0, PREFIX_LENGTH);
    }

    private String currentYearSuffix() {
        return String.format(Locale.ROOT, "%02d", Year.now().getValue() % 100);
    }
}
