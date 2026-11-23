package juribox.model;

/*
  Module: Criminal Law Subsystem & Security Constraint Verification
  Member: Kabir
  Date: 12 November 2026
  Topic: Subclass Inheritance, High-Security Flagging & Speedy Trial Rights
 */
public class CriminalCase extends Case {
    private boolean inCustody;
    private boolean violentOffense;

    public CriminalCase(String caseId, String title, Priority priority, int durationSlots,
                        boolean inCustody, boolean violentOffense) {
        super(caseId, title, CaseType.CRIMINAL, priority, durationSlots, CaseType.CRIMINAL);
        this.inCustody = inCustody;
        this.violentOffense = violentOffense;
    }

    public boolean isInCustody() {
        return inCustody;
    }

    public void setInCustody(boolean inCustody) {
        this.inCustody = inCustody;
    }

    public boolean isViolentOffense() {
        return violentOffense;
    }

    public void setViolentOffense(boolean violentOffense) {
        this.violentOffense = violentOffense;
    }

    @Override
    public String getCaseDetails() {
        return String.format("Criminal Proceeding | Accused In Custody: %s | Violent Offense: %s",
                (inCustody ? "Yes" : "No"), (violentOffense ? "Yes" : "No"));
    }

    @Override
    public double calculateUrgencyScore() {
        double score = getPriority().getWeight();
        if (inCustody) {
            score += 0.8;
        }
        if (violentOffense) {
            score += 0.4;
        }
        return score;
    }

    @Override
    public boolean requiresHighSecurity() {
        return inCustody || violentOffense;
    }
}

