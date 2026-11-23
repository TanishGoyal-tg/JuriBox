package juribox.model;

/*
  Module: Civil Law Subsystem & Financial Urgency Scoring
  Member: Kabir
  Date: 11 November 2026
  Topic: Inheritance, Method Overriding (Polymorphism) & Financial Thresholds
 */
public class CivilCase extends Case {
    private double disputeAmount;
    private boolean mediationAttempted;

    public CivilCase(String caseId, String title, Priority priority, int durationSlots,
                     double disputeAmount, boolean mediationAttempted) {
        super(caseId, title, CaseType.CIVIL, priority, durationSlots, CaseType.CIVIL);
        this.disputeAmount = Math.max(0.0, disputeAmount);
        this.mediationAttempted = mediationAttempted;
    }

    public double getDisputeAmount() {
        return disputeAmount;
    }

    public void setDisputeAmount(double disputeAmount) {
        this.disputeAmount = Math.max(0.0, disputeAmount);
    }

    public boolean isMediationAttempted() {
        return mediationAttempted;
    }

    public void setMediationAttempted(boolean mediationAttempted) {
        this.mediationAttempted = mediationAttempted;
    }

    @Override
    public String getCaseDetails() {
        return String.format("Civil Dispute | Claim: $%.2f | Mediation Attempted: %s",
                disputeAmount, (mediationAttempted ? "Yes" : "No"));
    }

    @Override
    public double calculateUrgencyScore() {
        double score = getPriority().getWeight();
        if (disputeAmount > 100_000) {
            score += 0.3;
        }
        if (mediationAttempted) {
            score += 0.2;
        }
        return score;
    }

    @Override
    public boolean requiresHighSecurity() {
        return false;
    }
}

