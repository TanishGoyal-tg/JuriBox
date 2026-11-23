package juribox.model;

import java.util.*;

/**
 * Represents a physical courtroom within the court complex.
 * Encapsulates room security features, daily operational availability, and booked schedules.
 */
public class Courtroom {
    private String courtroomId;
    private String roomName;
    private boolean highSecurity;
    // Map: Date (YYYY-MM-DD) -> Set of available time slots
    private Map<String, Set<Integer>> availableSlots;
    // Map: Date -> Set of booked time slots
    private Map<String, Set<Integer>> bookedSlots;
    // List of schedule IDs currently assigned to this courtroom
    private List<String> assignedScheduleIds;

    public Courtroom(String courtroomId, String roomName, boolean highSecurity) {
        if (courtroomId == null || courtroomId.trim().isEmpty()) {
            throw new IllegalArgumentException("Courtroom ID cannot be null or empty.");
        }
        if (roomName == null || roomName.trim().isEmpty()) {
            throw new IllegalArgumentException("Room name cannot be null or empty.");
        }
        this.courtroomId = courtroomId.trim().toUpperCase();
        this.roomName = roomName.trim();
        this.highSecurity = highSecurity;
        this.availableSlots = new HashMap<>();
        this.bookedSlots = new HashMap<>();
        this.assignedScheduleIds = new ArrayList<>();
    }

    public String getCourtroomId() {
        return courtroomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        if (roomName != null && !roomName.trim().isEmpty()) {
            this.roomName = roomName.trim();
        }
    }

    public boolean isHighSecurity() {
        return highSecurity;
    }

    public void setHighSecurity(boolean highSecurity) {
        this.highSecurity = highSecurity;
    }

    public List<String> getAssignedScheduleIds() {
        return Collections.unmodifiableList(assignedScheduleIds);
    }

    public void setAvailability(String date, Set<Integer> slots) {
        availableSlots.put(date, new TreeSet<>(slots));
        bookedSlots.putIfAbsent(date, new TreeSet<>());
    }

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
        Courtroom courtroom = (Courtroom) o;
        return Objects.equals(courtroomId, courtroom.courtroomId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courtroomId);
    }

    @Override
    public String toString() {
        return String.format("[%s] %-20s | High Security: %-3s | Active Cases: %d",
                courtroomId, roomName, (highSecurity ? "Yes" : "No"), assignedScheduleIds.size());
    }
}

