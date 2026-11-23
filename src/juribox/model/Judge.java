package juribox.model;

import juribox.model.Case.CaseType;

import java.util.*;

/**
 * Represents a judge presiding in the court.
 * Encapsulates judge specialization, working availability, and assigned schedules.
 */
public class Judge {
    private String judgeId;
    private String name;
    private CaseType specialization;
    // Map: Date (YYYY-MM-DD) -> Set of available time slots (e.g., 1, 2, 3, 4, 5, 6)
    private Map<String, Set<Integer>> availableSlots;
    // Map: Date -> Set of booked time slots
    private Map<String, Set<Integer>> bookedSlots;
    // List of schedule IDs currently assigned to this judge
    private List<String> assignedScheduleIds;

    public Judge(String judgeId, String name, CaseType specialization) {
        if (judgeId == null || judgeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Judge ID cannot be null or empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Judge name cannot be null or empty.");
        }
        this.judgeId = judgeId.trim().toUpperCase();
        this.name = name.trim();
        this.specialization = Objects.requireNonNull(specialization, "Specialization cannot be null.");
        this.availableSlots = new HashMap<>();
        this.bookedSlots = new HashMap<>();
        this.assignedScheduleIds = new ArrayList<>();
    }

    public String getJudgeId() {
        return judgeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
    }

    public CaseType getSpecialization() {
        return specialization;
    }

    public void setSpecialization(CaseType specialization) {
        this.specialization = Objects.requireNonNull(specialization, "Specialization cannot be null.");
    }

    public List<String> getAssignedScheduleIds() {
        return Collections.unmodifiableList(assignedScheduleIds);
    }

    /**
     * Initializes or updates availability for a specific date.
     */
    public void setAvailability(String date, Set<Integer> slots) {
        availableSlots.put(date, new TreeSet<>(slots));
        bookedSlots.putIfAbsent(date, new TreeSet<>());
    }

    /**
     * Sets default working slots (e.g. slots 1 to maxSlots) for a date.
     */
    public void setDefaultAvailability(String date, int maxSlots) {
        Set<Integer> slots = new TreeSet<>();
        for (int i = 1; i <= maxSlots; i++) {
            slots.add(i);
        }
        setAvailability(date, slots);
    }

    public Set<Integer> getAvailableSlotsForDate(String date) {
        return availableSlots.getOrDefault(date, Collections.emptySet());
    }

    public Set<Integer> getBookedSlotsForDate(String date) {
        return bookedSlots.getOrDefault(date, Collections.emptySet());
    }

    /**
     * Checks if the judge is working on the given date and free for the consecutive slots.
     */
    public boolean isAvailable(String date, int startSlot, int duration) {
        Set<Integer> workingSlots = availableSlots.get(date);
        if (workingSlots == null) {
            return false;
        }
        Set<Integer> booked = bookedSlots.getOrDefault(date, Collections.emptySet());

        for (int s = startSlot; s < startSlot + duration; s++) {
            if (!workingSlots.contains(s) || booked.contains(s)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Reserves consecutive slots for an assigned case.
     */
    public void bookSlots(String date, int startSlot, int duration, String scheduleId) {
        bookedSlots.putIfAbsent(date, new TreeSet<>());
        Set<Integer> booked = bookedSlots.get(date);
        for (int s = startSlot; s < startSlot + duration; s++) {
            booked.add(s);
        }
        if (!assignedScheduleIds.contains(scheduleId)) {
            assignedScheduleIds.add(scheduleId);
        }
    }

    /**
     * Releases booked slots (used during rescheduling or case cancellation).
     */
    public void releaseSlots(String date, int startSlot, int duration, String scheduleId) {
        Set<Integer> booked = bookedSlots.get(date);
        if (booked != null) {
            for (int s = startSlot; s < startSlot + duration; s++) {
                booked.remove(s);
            }
        }
        assignedScheduleIds.remove(scheduleId);
    }

    public int getTotalBookedSlotsCount() {
        int count = 0;
        for (Set<Integer> slots : bookedSlots.values()) {
            count += slots.size();
        }
        return count;
    }

    public int getTotalAvailableSlotsCount() {
        int count = 0;
        for (Set<Integer> slots : availableSlots.values()) {
            count += slots.size();
        }
        return count;
    }

    public Map<String, Set<Integer>> getAllAvailableSlots() {
        return availableSlots;
    }

    public Map<String, Set<Integer>> getAllBookedSlots() {
        return bookedSlots;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Judge judge = (Judge) o;
        return Objects.equals(judgeId, judge.judgeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(judgeId);
    }

    @Override
    public String toString() {
        return String.format("[%s] %-24s | Specialization: %-9s | Active Cases: %d",
                judgeId, name, specialization.getDisplayName(), assignedScheduleIds.size());
    }
}

