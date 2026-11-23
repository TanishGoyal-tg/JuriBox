package juribox.model;

import java.util.Objects;

/**
 * Represents a confirmed court hearing schedule binding a case, judge, courtroom, date, and time slots.
 */
public class Schedule {
    private String scheduleId;
    private String caseId;
    private String judgeId;
    private String courtroomId;
    private String date; // Format: YYYY-MM-DD
    private int startSlot; // 1-indexed (e.g., 1, 2, 3...)
    private int durationSlots;

    public Schedule(String scheduleId, String caseId, String judgeId, String courtroomId,
                    String date, int startSlot, int durationSlots) {
        if (scheduleId == null || scheduleId.trim().isEmpty()) {
            throw new IllegalArgumentException("Schedule ID cannot be null or empty.");
        }
        if (caseId == null || caseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Case ID cannot be null or empty.");
        }
        if (judgeId == null || judgeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Judge ID cannot be null or empty.");
        }
        if (courtroomId == null || courtroomId.trim().isEmpty()) {
            throw new IllegalArgumentException("Courtroom ID cannot be null or empty.");
        }
        if (date == null || date.trim().isEmpty()) {
            throw new IllegalArgumentException("Date cannot be null or empty.");
        }
        if (startSlot <= 0 || durationSlots <= 0) {
            throw new IllegalArgumentException("Slots must be positive integers.");
        }

        this.scheduleId = scheduleId.trim().toUpperCase();
        this.caseId = caseId.trim().toUpperCase();
        this.judgeId = judgeId.trim().toUpperCase();
        this.courtroomId = courtroomId.trim().toUpperCase();
        this.date = date.trim();
        this.startSlot = startSlot;
        this.durationSlots = durationSlots;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getJudgeId() {
        return judgeId;
    }

    public void setJudgeId(String judgeId) {
        this.judgeId = judgeId;
    }

    public String getCourtroomId() {
        return courtroomId;
    }

    public void setCourtroomId(String courtroomId) {
        this.courtroomId = courtroomId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getStartSlot() {
        return startSlot;
    }

    public void setStartSlot(int startSlot) {
        this.startSlot = startSlot;
    }

    public int getDurationSlots() {
        return durationSlots;
    }

    public void setDurationSlots(int durationSlots) {
        this.durationSlots = durationSlots;
    }

    public int getEndSlot() {
        return startSlot + durationSlots - 1;
    }

    public boolean containsSlot(int slot) {
        return slot >= startSlot && slot <= getEndSlot();
    }

    /**
     * Checks if two schedules occupy overlapping time slots on the same date.
     */
    public boolean overlapsWith(Schedule other) {
        if (!this.date.equalsIgnoreCase(other.date)) {
            return false;
        }
        return !(this.getEndSlot() < other.getStartSlot() || other.getEndSlot() < this.getStartSlot());
    }

    public String getSlotRangeDescription() {
        if (durationSlots == 1) {
            return "Slot " + startSlot;
        } else {
            return String.format("Slot %d-%d (%d slots)", startSlot, getEndSlot(), durationSlots);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Schedule schedule = (Schedule) o;
        return Objects.equals(scheduleId, schedule.scheduleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheduleId);
    }

    @Override
    public String toString() {
        return String.format("[%s] Date: %s | %s | Case: %s | Judge: %s | Room: %s",
                scheduleId, date, getSlotRangeDescription(), caseId, judgeId, courtroomId);
    }
}

