package juribox.service;

import juribox.exception.CourtSchedulingException.SchedulingConflictException;
import juribox.model.*;
import juribox.model.Case.*;

import java.util.*;

/*
  Module: Greedy First-Fit Scheduling Algorithm & Multi-Tier Priority Queuing
  Member: Tanish
  Date: 14 November 2026 - 15 November 2026
  Topic: Greedy Algorithm, Multi-Tier Comparator, Resource Allocation
 */
public class Scheduler {
    private final ConflictChecker conflictChecker;
    private int scheduleIdCounter = 1000;

    public Scheduler(ConflictChecker conflictChecker) {
        this.conflictChecker = Objects.requireNonNull(conflictChecker, "ConflictChecker cannot be null.");
    }

    /**
     * Automatically assigns pending cases to available judges and courtrooms.
     * Returns the list of newly created Schedule assignments.
     */
    public List<Schedule> schedulePendingCases(List<Case> cases,
                                              Map<String, Judge> judgeMap,
                                              Map<String, Courtroom> courtroomMap,
                                              List<Schedule> existingSchedules,
                                              List<String> courtDates,
                                              int maxDailySlots) {
        List<Schedule> newlyScheduled = new ArrayList<>();

        // 1. Collect pending cases
        List<Case> pendingCases = new ArrayList<>();
        for (Case c : cases) {
            if (c.getStatus() == CaseStatus.PENDING) {
                pendingCases.add(c);
            }
        }
        if (pendingCases.isEmpty()) return newlyScheduled;

        // 2. Sort by Priority, Urgency Score (Polymorphism), and Duration
        Comparator<Case> priorityComparator = Comparator
                .comparing((Case c) -> c.getPriority().getWeight(), Comparator.reverseOrder())
                .thenComparing(Case::calculateUrgencyScore, Comparator.reverseOrder())
                .thenComparing(Case::getDurationSlots)
                .thenComparing(Case::getCaseId);
        pendingCases.sort(priorityComparator);

        // 3. Search and allocate slots
        for (Case caseObj : pendingCases) {
            List<Judge> eligibleJudges = new ArrayList<>();
            for (Judge j : judgeMap.values()) {
                if (j.getSpecialization() == caseObj.getRequiredSpecialization()) {
                    eligibleJudges.add(j);
                }
            }

            List<Courtroom> eligibleRooms = new ArrayList<>();
            for (Courtroom cr : courtroomMap.values()) {
                if (!caseObj.requiresHighSecurity() || cr.isHighSecurity()) {
                    eligibleRooms.add(cr);
                }
            }

            dateLoop:
            for (String date : courtDates) {
                for (Judge judge : eligibleJudges) {
                    for (Courtroom room : eligibleRooms) {
                        int duration = caseObj.getDurationSlots();

                        for (int startSlot = 1; startSlot <= (maxDailySlots - duration + 1); startSlot++) {
                            try {
                                conflictChecker.validateAssignment(
                                        caseObj, judge, room, date, startSlot, duration,
                                        existingSchedules, null, maxDailySlots
                                );

                                String scheduleId = "SCH-" + (++scheduleIdCounter);
                                Schedule schedule = new Schedule(scheduleId, caseObj.getCaseId(),
                                        judge.getJudgeId(), room.getCourtroomId(), date, startSlot, duration);

                                judge.bookSlots(date, startSlot, duration, scheduleId);
                                room.bookSlots(date, startSlot, duration, scheduleId);
                                caseObj.setStatus(CaseStatus.SCHEDULED);
                                caseObj.setAssignedScheduleId(scheduleId);

                                existingSchedules.add(schedule);
                                newlyScheduled.add(schedule);
                                break dateLoop;
                            } catch (SchedulingConflictException ignored) {
                                // Try next slot
                            }
                        }
                    }
                }
            }
        }

        return newlyScheduled;
    }

    public void setScheduleIdCounter(int counter) {
        this.scheduleIdCounter = counter;
    }
}
