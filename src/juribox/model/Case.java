package juribox.model;

import java.util.Objects;

/*
  Module: Core Domain Modeling & Abstraction
  Member: Kabir
  Date: 10 November 2026 - 11 November 2026
  Topic: Abstract Classes, Encapsulation, Polymorphism & Static Nested Enums
 */
public abstract class Case {
    private String caseId;
    private String title;
    private CaseType caseType;
    private Priority priority;
    private int durationSlots;
    private CaseType requiredSpecialization;
    private CaseStatus status;
    private String assignedScheduleId;

    public Case(String caseId, String title, CaseType caseType, Priority priority,
                int durationSlots, CaseType requiredSpecialization) {
        if (caseId == null || caseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Case ID cannot be null or empty.");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Case title cannot be null or empty.");
        }
        if (durationSlots <= 0) {
            throw new IllegalArgumentException("Duration must be at least 1 time slot.");
        }
        this.caseId = caseId.trim().toUpperCase();
        this.title = title.trim();
        this.caseType = Objects.requireNonNull(caseType, "Case type cannot be null.");
        this.priority = Objects.requireNonNull(priority, "Priority cannot be null.");
        this.durationSlots = durationSlots;
        this.requiredSpecialization = (requiredSpecialization != null) ? requiredSpecialization : caseType;
        this.status = CaseStatus.PENDING;
        this.assignedScheduleId = null;
    }

    //  [Kabir - 10 Nov 2026] Abstract methods to be implemented by subclasses (Polymorphism) 

    public abstract String getCaseDetails();

    public abstract double calculateUrgencyScore();

    public abstract boolean requiresHighSecurity();

    public String getCaseId() {
        return caseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title != null && !title.trim().isEmpty()) {
            this.title = title.trim();
        }
    }

    public CaseType getCaseType() {
        return caseType;
    }

    protected void setCaseType(CaseType caseType) {
        this.caseType = caseType;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = Objects.requireNonNull(priority, "Priority cannot be null.");
    }

    public int getDurationSlots() {
        return durationSlots;
    }

    public void setDurationSlots(int durationSlots) {
        if (durationSlots <= 0) {
            throw new IllegalArgumentException("Duration must be at least 1 time slot.");
        }
        this.durationSlots = durationSlots;
    }

    public CaseType getRequiredSpecialization() {
        return requiredSpecialization;
    }

    public void setRequiredSpecialization(CaseType requiredSpecialization) {
        this.requiredSpecialization = Objects.requireNonNull(requiredSpecialization, "Specialization cannot be null.");
    }

    public CaseStatus getStatus() {
        return status;
    }

    public void setStatus(CaseStatus status) {
        this.status = Objects.requireNonNull(status, "Status cannot be null.");
    }

    public String getAssignedScheduleId() {
        return assignedScheduleId;
    }

    public void setAssignedScheduleId(String assignedScheduleId) {
        this.assignedScheduleId = assignedScheduleId;
    }

    public boolean isUrgent() {
        return this.priority == Priority.HIGH;
    }

    public boolean isScheduled() {
        return this.status == CaseStatus.SCHEDULED || this.status == CaseStatus.RESCHEDULED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Case aCase = (Case) o;
        return Objects.equals(caseId, aCase.caseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(caseId);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | Type: %-8s | Priority: %-6s | Duration: %d slot(s) | Status: %-11s | Spec: %s",
                caseId, title, caseType.getDisplayName(), priority.getDisplayName(),
                durationSlots, status.getDisplayName(), requiredSpecialization.getDisplayName());
    }

    //  [Kabir - 11 Nov 2026] Nested Domain Enums (CaseType, Priority, CaseStatus) 

    public enum CaseType {
        CIVIL("Civil"),
        CRIMINAL("Criminal"),
        PROPERTY("Property");

        private final String displayName;

        CaseType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static CaseType fromString(String text) {
            for (CaseType type : CaseType.values()) {
                if (type.name().equalsIgnoreCase(text) || type.displayName.equalsIgnoreCase(text)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown case type: " + text + ". Allowed: CIVIL, CRIMINAL, PROPERTY");
        }
    }

    public enum Priority {
        HIGH(3, "High"),
        MEDIUM(2, "Medium"),
        LOW(1, "Low");

        private final int weight;
        private final String displayName;

        Priority(int weight, String displayName) {
            this.weight = weight;
            this.displayName = displayName;
        }

        public int getWeight() {
            return weight;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static Priority fromString(String text) {
            for (Priority p : Priority.values()) {
                if (p.name().equalsIgnoreCase(text) || p.displayName.equalsIgnoreCase(text)) {
                    return p;
                }
            }
            throw new IllegalArgumentException("Unknown priority: " + text + ". Allowed: HIGH, MEDIUM, LOW");
        }
    }

    public enum CaseStatus {
        PENDING("Pending"),
        SCHEDULED("Scheduled"),
        RESCHEDULED("Rescheduled");

        private final String displayName;

        CaseStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static CaseStatus fromString(String text) {
            for (CaseStatus status : CaseStatus.values()) {
                if (status.name().equalsIgnoreCase(text) || status.displayName.equalsIgnoreCase(text)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown status: " + text);
        }
    }
}

