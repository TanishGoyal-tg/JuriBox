package juribox.model;

/*
  Module: Property Law Subsystem & Injunction Priority
  Member: Kabir
  Date: 13 November 2026
  Topic: Domain Specialization, Land Injunctions & Polymorphic Behavior
 */
public class PropertyCase extends Case {
    private String propertyLocation;
    private boolean injunctionRequested;

    public PropertyCase(String caseId, String title, Priority priority, int durationSlots,
        String propertyLocation, boolean injunctionRequested) {
        super(caseId, title, CaseType.PROPERTY, priority, durationSlots, CaseType.PROPERTY);
        this.propertyLocation = (propertyLocation != null && !propertyLocation.trim().isEmpty())
        ? propertyLocation.trim() : "Unspecified";
        this.injunctionRequested = injunctionRequested;
    }

    public String getPropertyLocation() {
        return propertyLocation;
    }

    public void setPropertyLocation(String propertyLocation) {
        this.propertyLocation = propertyLocation;
    }

    public boolean isInjunctionRequested() {
        return injunctionRequested;
    }

    public void setInjunctionRequested(boolean injunctionRequested) {
        this.injunctionRequested = injunctionRequested;
    }

    @Override
    public String getCaseDetails() {
        return String.format("Property Dispute | Location: %s | Injunction/Stay Requested: %s",
           propertyLocation, (injunctionRequested ? "Yes" : "No"));
    }

    @Override
    public double calculateUrgencyScore() {
        double score = getPriority().getWeight();
        if (injunctionRequested) {
            score += 0.6;
        }
        return score;
    }

    @Override
    public boolean requiresHighSecurity() {
        return false;
    }
}

