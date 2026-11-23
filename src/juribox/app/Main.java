package juribox.app;

import juribox.exception.CourtSchedulingException;
import juribox.exception.CourtSchedulingException.*;
import juribox.model.*;
import juribox.model.Case.*;
import juribox.service.*;

import java.util.List;
import java.util.Scanner;

/*
  Module: Presentation Layer & Console Interactive Menu Interface
  Member: Lakshit
  Date: 21 November 2026 - 22 November 2026
  Topic: Console User Interface, Input Parsing, Exception Handling & Application Entry Point
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final CourtManager courtManager = new CourtManager("data");

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("      COURT CASE SCHEDULING SIMULATOR (JuriBox)             ");
        System.out.println("============================================================");
        System.out.print("Load pre-configured demo cases, judges, and rooms? (Y/N, default Y): ");
        if (!scanner.hasNextLine()) return;
        String initChoice = scanner.nextLine().trim();
        if (initChoice.isEmpty() || initChoice.equalsIgnoreCase("Y")) {
            courtManager.seedDemoData();
            System.out.println("[OK] Demo dataset loaded (3 Judges, 3 Rooms, 6 Cases).");
        }

        // [Lakshit - 21 Nov 2026] Interactive 10-Option Command Dispatcher Loop
        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Enter choice (1-10): ");
            if (!scanner.hasNextLine()) break;

            String choiceStr = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(choiceStr);
                switch (choice) {
                    case 1: handleAddCase(); break;
                    case 2: handleViewCases(); break;
                    case 3: handleAddJudge(); break;
                    case 4: handleAddCourtroom(); break;
                    case 5: handleViewResources(); break;
                    case 6: handleAutoSchedule(); break;
                    case 7: handleDailySchedule(); break;
                    case 8: handleReschedule(); break;
                    case 9: handleSimulation(); break;
                    case 10:
                        courtManager.saveData();
                        System.out.println("[SAVED] Schedule exported to file and synchronized with MySQL database. Exiting. Goodbye!");
                        running = false;
                        break;
                    default:
                        System.out.println("Please enter a number between 1 and 10.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Enter a valid number.");
            } catch (Exception e) {
                System.out.println("[ERROR] " + e.getMessage());
            }

            if (running) {
                System.out.println("\nPress ENTER to continue...");
                if (scanner.hasNextLine()) scanner.nextLine();
                else break;
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n========== COURT SCHEDULER MENU ==========");
        System.out.println(" 1. Add New Case");
        System.out.println(" 2. View All Cases");
        System.out.println(" 3. Add Judge");
        System.out.println(" 4. Add Courtroom");
        System.out.println(" 5. View Judges & Courtrooms");
        System.out.println(" 6. Schedule Cases Automatically");
        System.out.println(" 7. View Daily Schedule");
        System.out.println(" 8. Reschedule a Case");
        System.out.println(" 9. Live Court Simulation");
        System.out.println("10. Save & Exit");
        System.out.println("==========================================");
    }

    private static void handleAddCase() throws DuplicateEntityException {
        System.out.print("Enter Case ID (e.g. C107): ");
        String id = scanner.nextLine().trim().toUpperCase();
        System.out.print("Enter Case Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Case Type (1. Civil, 2. Criminal, 3. Property): ");
        int t = Integer.parseInt(scanner.nextLine().trim());
        CaseType type = (t == 1) ? CaseType.CIVIL : (t == 2) ? CaseType.CRIMINAL : CaseType.PROPERTY;

        System.out.print("Priority (1. High, 2. Medium, 3. Low): ");
        int p = Integer.parseInt(scanner.nextLine().trim());
        Priority priority = (p == 1) ? Priority.HIGH : (p == 2) ? Priority.MEDIUM : Priority.LOW;

        System.out.print("Duration in slots (1-6): ");
        int dur = Integer.parseInt(scanner.nextLine().trim());

        Case c;
        if (type == CaseType.CIVIL) {
            System.out.print("Claim amount ($): ");
            double claim = Double.parseDouble(scanner.nextLine().trim());
            c = new CivilCase(id, title, priority, dur, claim, false);
        } else if (type == CaseType.CRIMINAL) {
            System.out.print("Is accused in custody? (Y/N): ");
            boolean inCustody = scanner.nextLine().trim().equalsIgnoreCase("Y");
            c = new CriminalCase(id, title, priority, dur, inCustody, inCustody);

        } else {
            System.out.print("Property location: ");
            String loc = scanner.nextLine().trim();
            c = new PropertyCase(id, title, priority, dur, loc, false);
        }

        courtManager.addCase(c);
        System.out.println("[SUCCESS] Case " + id + " registered as PENDING.");
    }

    private static void handleViewCases() {
        System.out.println("\n--- ALL REGISTERED CASES ---");
        List<Case> cases = courtManager.getAllCases();
        if (cases.isEmpty()) System.out.println("No cases registered.");
        else for (Case c : cases) System.out.println(c);
    }

    private static void handleAddJudge() throws DuplicateEntityException {
        System.out.print("Enter Judge ID (e.g. J04): ");
        String id = scanner.nextLine().trim().toUpperCase();
        System.out.print("Enter Judge Full Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Specialization (1. Civil, 2. Criminal, 3. Property): ");
        int specChoice = Integer.parseInt(scanner.nextLine().trim());
        CaseType spec = (specChoice == 1) ? CaseType.CIVIL :
                        (specChoice == 2) ? CaseType.CRIMINAL : CaseType.PROPERTY;

        courtManager.addJudge(new Judge(id, name, spec));
        System.out.println("[SUCCESS] Judge " + name + " (" + id + ") registered in " + spec.getDisplayName() + ".");
    }

    private static void handleAddCourtroom() throws DuplicateEntityException {
        System.out.print("Enter Courtroom ID (e.g. CR04): ");
        String id = scanner.nextLine().trim().toUpperCase();
        System.out.print("Enter Room Name/Number: ");
        String name = scanner.nextLine().trim();
        System.out.print("Is this a High-Security courtroom? (Y/N): ");
        boolean highSec = scanner.nextLine().trim().equalsIgnoreCase("Y");

        courtManager.addCourtroom(new Courtroom(id, name, highSec));
        System.out.println("[SUCCESS] Courtroom " + id + " registered.");
    }

    private static void handleViewResources() {
        System.out.println("\n--- JUDGES ---");
        for (Judge j : courtManager.getAllJudges()) System.out.println(j);
        System.out.println("\n--- COURTROOMS ---");
        for (Courtroom cr : courtManager.getAllCourtrooms()) System.out.println(cr);
    }

    private static void handleAutoSchedule() {
        System.out.println("\nRunning auto-scheduler...");
        List<Schedule> scheduled = courtManager.schedulePendingCases();
        System.out.printf("Successfully scheduled %d case(s)!\n", scheduled.size());
        for (Schedule s : scheduled) System.out.println("  " + s);

        List<String> conflicts = courtManager.detectConflicts();
        if (conflicts.isEmpty()) {
            System.out.println("[Internal Check] 0 scheduling conflicts detected. All assignments are verified safe.");
        } else {
            System.out.printf("[Internal Check Warning] %d conflict(s) detected:\n", conflicts.size());
            for (String err : conflicts) System.out.println("  - " + err);
        }
    }

    private static void handleDailySchedule() {
        System.out.print("Enter Date (YYYY-MM-DD, e.g. 2026-11-10): ");
        String date = scanner.nextLine().trim();
        if (date.isEmpty()) date = "2026-11-10";
        System.out.println(courtManager.formatDailySchedule(date));
    }

    private static void handleReschedule() throws EntityNotFoundException, CourtSchedulingException {
        System.out.print("Enter Case ID to reschedule: ");
        String caseId = scanner.nextLine().trim().toUpperCase();
        System.out.print("Enter Target Date (YYYY-MM-DD): ");
        String date = scanner.nextLine().trim();
        System.out.print("Enter New Start Slot (1-6): ");
        int slot = Integer.parseInt(scanner.nextLine().trim());

        Schedule updated = courtManager.rescheduleCase(caseId, date, slot, null, null);
        System.out.println("[SUCCESS] Case rescheduled: " + updated);
    }

    private static void handleSimulation() {
        System.out.print("Enter date to simulate (default 2026-11-10): ");
        String date = scanner.nextLine().trim();
        if (date.isEmpty()) date = "2026-11-10";
        courtManager.simulateCourtDay(date, 400);
    }
}
