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
        ConsoleUI.initConsole();
        ConsoleUI.printBanner();

        ConsoleUI.prompt("Load pre-configured demo cases, judges, and rooms? (Y/N, default Y): ");
        if (!scanner.hasNextLine()) return;
        String initChoice = scanner.nextLine().trim();
        if (initChoice.isEmpty() || initChoice.equalsIgnoreCase("Y")) {
            courtManager.seedDemoData();
            ConsoleUI.ok("Demo dataset loaded (3 Judges, 3 Rooms, 6 Cases).");
        }

        // [Lakshit - 21 Nov 2026] Interactive Command Dispatcher Loop
        boolean running = true;
        while (running) {
            ConsoleUI.printMenu(courtManager);
            ConsoleUI.prompt("Enter choice (1-11): ");
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
                    case 10: handleAnalytics(); break;
                    case 11:
                        courtManager.saveData();
                        ConsoleUI.ok("Schedule exported to file and synchronized with MySQL database. Exiting. Goodbye!");
                        running = false;
                        break;
                    default:
                        ConsoleUI.warn("Please enter a number between 1 and 11.");
                }
            } catch (NumberFormatException e) {
                ConsoleUI.err("Invalid input. Enter a valid number.");
            } catch (Exception e) {
                ConsoleUI.err(e.getMessage());
            }

            if (running) {
                System.out.println(ConsoleUI.GRAY + "\n  Press ENTER to continue..." + ConsoleUI.RESET);
                if (scanner.hasNextLine()) scanner.nextLine();
                else break;
            }
        }
    }

    private static void handleAddCase() throws DuplicateEntityException {
        ConsoleUI.sectionHeader("REGISTER NEW CASE");
        ConsoleUI.prompt("Enter Case ID (e.g. C107): ");
        String id = scanner.nextLine().trim().toUpperCase();
        ConsoleUI.prompt("Enter Case Title: ");
        String title = scanner.nextLine().trim();
        ConsoleUI.prompt("Case Type (1. Civil, 2. Criminal, 3. Property): ");
        int t = Integer.parseInt(scanner.nextLine().trim());
        CaseType type = (t == 1) ? CaseType.CIVIL : (t == 2) ? CaseType.CRIMINAL : CaseType.PROPERTY;

        ConsoleUI.prompt("Priority (1. High, 2. Medium, 3. Low): ");
        int p = Integer.parseInt(scanner.nextLine().trim());
        Priority priority = (p == 1) ? Priority.HIGH : (p == 2) ? Priority.MEDIUM : Priority.LOW;

        ConsoleUI.prompt("Duration in slots (1-6): ");
        int dur = Integer.parseInt(scanner.nextLine().trim());

        Case c;
        if (type == CaseType.CIVIL) {
            ConsoleUI.prompt("Claim amount ($): ");
            double claim = Double.parseDouble(scanner.nextLine().trim());
            c = new CivilCase(id, title, priority, dur, claim, false);
        } else if (type == CaseType.CRIMINAL) {
            ConsoleUI.prompt("Is accused in custody? (Y/N): ");
            boolean inCustody = scanner.nextLine().trim().equalsIgnoreCase("Y");
            c = new CriminalCase(id, title, priority, dur, inCustody, inCustody);
        } else {
            ConsoleUI.prompt("Property location: ");
            String loc = scanner.nextLine().trim();
            c = new PropertyCase(id, title, priority, dur, loc, false);
        }

        courtManager.addCase(c);
        ConsoleUI.ok("Case " + id + " registered as PENDING.");
    }

    private static void handleViewCases() {
        ConsoleUI.printCasesTable(courtManager.getAllCases());
    }

    private static void handleAddJudge() throws DuplicateEntityException {
        ConsoleUI.sectionHeader("REGISTER NEW JUDGE");
        ConsoleUI.prompt("Enter Judge ID (e.g. J04): ");
        String id = scanner.nextLine().trim().toUpperCase();
        ConsoleUI.prompt("Enter Judge Full Name: ");
        String name = scanner.nextLine().trim();
        ConsoleUI.prompt("Specialization (1. Civil, 2. Criminal, 3. Property): ");
        int specChoice = Integer.parseInt(scanner.nextLine().trim());
        CaseType spec = (specChoice == 1) ? CaseType.CIVIL :
                        (specChoice == 2) ? CaseType.CRIMINAL : CaseType.PROPERTY;

        courtManager.addJudge(new Judge(id, name, spec));
        ConsoleUI.ok("Judge " + name + " (" + id + ") registered in " + spec.getDisplayName() + ".");
    }

    private static void handleAddCourtroom() throws DuplicateEntityException {
        ConsoleUI.sectionHeader("REGISTER NEW COURTROOM");
        ConsoleUI.prompt("Enter Courtroom ID (e.g. CR04): ");
        String id = scanner.nextLine().trim().toUpperCase();
        ConsoleUI.prompt("Enter Room Name/Number: ");
        String name = scanner.nextLine().trim();
        ConsoleUI.prompt("Is this a High-Security courtroom? (Y/N): ");
        boolean highSec = scanner.nextLine().trim().equalsIgnoreCase("Y");

        courtManager.addCourtroom(new Courtroom(id, name, highSec));
        ConsoleUI.ok("Courtroom " + id + " registered.");
    }

    private static void handleViewResources() {
        ConsoleUI.printResourcesTables(courtManager.getAllJudges(), courtManager.getAllCourtrooms());
    }

    private static void handleAutoSchedule() {
        ConsoleUI.sectionHeader("AUTOMATIC DOCKET SCHEDULER");
        ConsoleUI.info("Running multi-constraint priority scheduler...");
        List<Schedule> scheduled = courtManager.schedulePendingCases();
        ConsoleUI.ok("Successfully scheduled " + scheduled.size() + " case(s)!");
        for (Schedule s : scheduled) {
            System.out.println(ConsoleUI.GRAY + "    • " + ConsoleUI.BRIGHT_WHITE + s + ConsoleUI.RESET);
        }

        List<String> conflicts = courtManager.detectConflicts();
        if (conflicts.isEmpty()) {
            ConsoleUI.ok("[Internal Check] 0 scheduling conflicts detected. All assignments are verified safe.");
        } else {
            ConsoleUI.warn("[Internal Check Warning] " + conflicts.size() + " conflict(s) detected:");
            for (String err : conflicts) {
                ConsoleUI.err("  - " + err);
            }
        }
    }

    private static void handleDailySchedule() {
        ConsoleUI.prompt("Enter Date (YYYY-MM-DD, default 2026-11-10): ");
        String date = scanner.nextLine().trim();
        if (date.isEmpty()) date = "2026-11-10";
        ConsoleUI.printDailyScheduleWithTimeline(courtManager, date);
    }

    private static void handleReschedule() throws EntityNotFoundException, CourtSchedulingException {
        ConsoleUI.sectionHeader("RESCHEDULE CASE");
        ConsoleUI.prompt("Enter Case ID to reschedule: ");
        String caseId = scanner.nextLine().trim().toUpperCase();
        ConsoleUI.prompt("Enter Target Date (YYYY-MM-DD): ");
        String date = scanner.nextLine().trim();
        ConsoleUI.prompt("Enter New Start Slot (1-6): ");
        int slot = Integer.parseInt(scanner.nextLine().trim());

        Schedule updated = courtManager.rescheduleCase(caseId, date, slot, null, null);
        ConsoleUI.ok("Case rescheduled: " + updated);
    }

    private static void handleSimulation() {
        ConsoleUI.sectionHeader("LIVE CONCURRENT COURT SIMULATION");
        ConsoleUI.prompt("Enter date to simulate (default 2026-11-10): ");
        String date = scanner.nextLine().trim();
        if (date.isEmpty()) date = "2026-11-10";
        courtManager.simulateCourtDay(date, 400);
    }

    private static void handleAnalytics() {
        ConsoleUI.printAnalyticsDashboard(courtManager);
    }
}
