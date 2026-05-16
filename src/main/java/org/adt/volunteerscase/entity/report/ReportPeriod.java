package org.adt.volunteerscase.entity.report;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum ReportPeriod {
    MONTHLY("monthly"),
    OVERALL("overall");

    private final String value;

    ReportPeriod(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ReportPeriod fromRequest(String value) {
        if (value == null || value.isBlank()) {
            return MONTHLY;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);

        for (ReportPeriod period : values()) {
            if (period.value.equals(normalized)) {
                return period;
            }
        }

        throw new IllegalArgumentException("period must be one of: monthly, overall");
    }
}