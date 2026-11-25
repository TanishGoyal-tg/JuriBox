package juribox.app;

import juribox.model.*;
import juribox.model.Case.*;
import juribox.service.CourtManager;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Rich ANSI Terminal UI helper for JuriBox.
 * Provides colorized output, Unicode box-drawn tables, ASCII Gantt timelines,
 * and utilization progress bars with zero external dependencies.
 */
public final class ConsoleUI {

    private static final boolean COLOR_ENABLED = System.getenv("NO_COLOR") == null;
    private static final Pattern ANSI_PATTERN = Pattern.compile("\u001B\\[[;\\d]*m");

    // ANSI Foreground & Style Codes
    public static final String RESET          = c("\u001B[0m");
    public static final String BOLD           = c("\u001B[1m");
    public static final String DIM            = c("\u001B[2m");

    public static final String RED            = c("\u001B[31m");
    public static final String GREEN          = c("\u001B[32m");
    public static final String YELLOW         = c("\u001B[33m");
    public static final String BLUE           = c("\u001B[34m");
    public static final String MAGENTA        = c("\u001B[35m");
    public static final String CYAN           = c("\u001B[36m");
    public static final String WHITE          = c("\u001B[37m");
    public static final String GRAY           = c("\u001B[90m");

    public static final String BRIGHT_RED     = c("\u001B[91m");
    public static final String BRIGHT_GREEN   = c("\u001B[92m");
    public static final String BRIGHT_YELLOW  = c("\u001B[93m");
    public static final String BRIGHT_BLUE    = c("\u001B[94m");
    public static final String BRIGHT_MAGENTA = c("\u001B[95m");
    public static final String BRIGHT_CYAN    = c("\u001B[96m");
    public static final String BRIGHT_WHITE   = c("\u001B[97m");

    private static String c(String code) {
        return COLOR_ENABLED ? code : "";
    }

    private ConsoleUI() {}

    /**
     * Ensures System.out uses UTF-8 so Unicode box-drawing characters render cleanly on Windows.
     */
    public static void initConsole() {
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException ignored) {
        }
    }

    // --- Status & Prompt Helpers ---

    public static void ok(String msg) {
        System.out.println(BRIGHT_GREEN + BOLD + "  [OK] " + RESET + GREEN + msg + RESET);
    }

    public static void info(String msg) {
        System.out.println(BRIGHT_CYAN + BOLD + "  [i]  " + RESET + CYAN + msg + RESET);
    }

    public static void warn(String msg) {
        System.out.println(BRIGHT_YELLOW + BOLD + "  [!]  " + RESET + YELLOW + msg + RESET);
    }

    public static void err(String msg) {
        System.out.println(BRIGHT_RED + BOLD + "  [X]  " + RESET + RED + msg + RESET);
    }

    public static void prompt(String label) {
        System.out.print(BRIGHT_CYAN + "  > " + RESET + BOLD + label + RESET);
    }

    public static void sectionHeader(String title) {
        String bar = "─".repeat(Math.max(4, 68 - visibleLength(title)));
        System.out.println();
        System.out.println(BRIGHT_CYAN + BOLD + "  ── " + BRIGHT_WHITE + title + BRIGHT_CYAN + " " + bar + RESET);
    }

    // --- Banner & Main Menu ---

    public static void printBanner() {
        int innerWidth = 70;
        String line1 = center("⚖  J U R I B O X   -   COURT CASE SCHEDULING SIMULATOR", innerWidth);
        String line2 = center("Automated Docket Allocation  |  Conflict Guard  |  Live Threads", innerWidth);
        String bar = "═".repeat(innerWidth);
        System.out.println();
        System.out.println(BRIGHT_CYAN + "  ╔" + bar + "╗" + RESET);
        System.out.println(BRIGHT_CYAN + "  ║" + RESET + BOLD + BRIGHT_WHITE + line1 + RESET + BRIGHT_CYAN + "║" + RESET);
        System.out.println(BRIGHT_CYAN + "  ║" + RESET + GRAY + line2 + RESET + BRIGHT_CYAN + "║" + RESET);
        System.out.println(BRIGHT_CYAN + "  ╚" + bar + "╝" + RESET);
    }

    public static void printMenu(CourtManager cm) {
        int totalCases = cm.getAllCases().size();
        int pending = cm.getPendingCases().size();
        int scheduled = totalCases - pending;
        int judges = cm.getAllJudges().size();
        int rooms = cm.getAllCourtrooms().size();
        int conflicts = cm.detectConflicts().size();

        String conflictBadge = (conflicts == 0)
                ? BRIGHT_GREEN + "0 Conflicts" + RESET
                : BRIGHT_RED + conflicts + " Conflict(s)" + RESET;

        int leftW = 35;
        int rightW = 37;
        int totalInner = leftW + 1 + rightW; // +1 for middle '│' -> 73

        String docketText = BOLD + BRIGHT_WHITE + "DOCKET " + RESET
                + CYAN + "Cases: " + BRIGHT_WHITE + totalCases + RESET
                + GRAY + " (" + BRIGHT_GREEN + scheduled + " sched" + GRAY + ", "
                + (pending > 0 ? BRIGHT_YELLOW : GRAY) + pending + " pend" + GRAY + ")"
                + " | " + CYAN + "Judges: " + BRIGHT_WHITE + judges
                + " | " + CYAN + "Rooms: " + BRIGHT_WHITE + rooms
                + " | " + conflictBadge;

        System.out.println();
        System.out.println(GRAY + "  ┌" + "─".repeat(totalInner) + "┐" + RESET);
        System.out.println(GRAY + "  │ " + RESET + padRight(docketText, totalInner - 2) + GRAY + " │" + RESET);
        System.out.println(GRAY + "  ├" + "─".repeat(leftW) + "┬" + "─".repeat(rightW) + "┤" + RESET);
        printMenuRow(" 1. Add New Case",            " 7. Daily Schedule & Timeline", leftW, rightW);
        printMenuRow(" 2. View All Cases",           " 8. Reschedule a Case",         leftW, rightW);
        printMenuRow(" 3. Add Judge",                " 9. Live Court Simulation",     leftW, rightW);
        printMenuRow(" 4. Add Courtroom",            "10. Analytics & Utilization",   leftW, rightW);
        printMenuRow(" 5. View Judges & Courtrooms", "11. Save & Exit",               leftW, rightW);
        printMenuRow(" 6. Auto-Schedule Pending",    "",                              leftW, rightW);
        System.out.println(GRAY + "  └" + "─".repeat(leftW) + "┴" + "─".repeat(rightW) + "┘" + RESET);
    }

    private static void printMenuRow(String left, String right, int leftW, int rightW) {
        String leftColored = formatMenuItem(left);
        String rightColored = formatMenuItem(right);
        System.out.println(GRAY + "  │ " + RESET + padRight(leftColored, leftW - 2)
                + GRAY + " │ " + RESET + padRight(rightColored, rightW - 2)
                + GRAY + " │" + RESET);
    }

    private static String center(String s, int width) {
        int vis = visibleLength(s);
        if (vis >= width) return s;
        int left = (width - vis) / 2;
        int right = width - vis - left;
        return " ".repeat(left) + s + " ".repeat(right);
    }

    private static String formatMenuItem(String item) {
        if (item == null || item.isEmpty()) return "";
        int dot = item.indexOf('.');
        if (dot > 0) {
            String num = item.substring(0, dot + 1);
            String label = item.substring(dot + 1);
            return BRIGHT_CYAN + BOLD + num + RESET + WHITE + label + RESET;
        }
        return item;
    }

    // --- Cases Table ---

    public static void printCasesTable(List<Case> cases) {
        sectionHeader("ALL REGISTERED CASES (" + cases.size() + ")");
        if (cases.isEmpty()) {
            warn("No cases registered yet.");
            return;
        }
        String[] headers = {"ID", "Case Title", "Type", "Priority", "Slots", "Security", "Status"};
        int[] widths = {6, 28, 10, 8, 5, 10, 12};
        List<String[]> rows = new ArrayList<>();
        for (Case c : cases) {
            rows.add(new String[]{
                    BRIGHT_WHITE + BOLD + c.getCaseId() + RESET,
                    truncate(c.getTitle(), 28),
                    colorCaseType(c.getCaseType()),
                    colorPriority(c.getPriority()),
                    BRIGHT_WHITE + String.valueOf(c.getDurationSlots()) + RESET,
                    c.requiresHighSecurity()
                            ? BRIGHT_MAGENTA + BOLD + "HIGH SEC" + RESET
                            : GRAY + "Standard" + RESET,
                    colorStatus(c.getStatus())
            });
        }
        printTable(headers, rows, widths);
    }

    // --- Judges & Courtrooms Tables ---

    public static void printResourcesTables(List<Judge> judges, List<Courtroom> rooms) {
        sectionHeader("JUDGES (" + judges.size() + ")");
        if (judges.isEmpty()) {
            warn("No judges registered.");
        } else {
            String[] jHeaders = {"ID", "Judge Name", "Specialization", "Assigned Hearings"};
            int[] jWidths = {6, 26, 14, 17};
            List<String[]> jRows = new ArrayList<>();
            for (Judge j : judges) {
                jRows.add(new String[]{
                        BRIGHT_WHITE + BOLD + j.getJudgeId() + RESET,
                        truncate(j.getName(), 26),
                        colorCaseType(j.getSpecialization()),
                        BRIGHT_CYAN + String.valueOf(j.getAssignedScheduleIds().size()) + RESET
                });
            }
            printTable(jHeaders, jRows, jWidths);
        }

        sectionHeader("COURTROOMS (" + rooms.size() + ")");
        if (rooms.isEmpty()) {
            warn("No courtrooms registered.");
        } else {
            String[] rHeaders = {"ID", "Room Name", "Security Level", "Assigned Hearings"};
            int[] rWidths = {6, 28, 15, 17};
            List<String[]> rRows = new ArrayList<>();
            for (Courtroom cr : rooms) {
                rRows.add(new String[]{
                        BRIGHT_WHITE + BOLD + cr.getCourtroomId() + RESET,
                        truncate(cr.getRoomName(), 28),
                        cr.isHighSecurity()
                                ? BRIGHT_MAGENTA + BOLD + "HIGH SECURITY" + RESET
                                : GRAY + "Standard" + RESET,
                        BRIGHT_CYAN + String.valueOf(cr.getAssignedScheduleIds().size()) + RESET
                });
            }
            printTable(rHeaders, rRows, rWidths);
        }
    }

    // --- Daily Schedule + ASCII Gantt Timeline ---

    public static void printDailyScheduleWithTimeline(CourtManager cm, String date) {
        List<Schedule> daily = cm.getDailySchedule(date);
        List<Courtroom> rooms = cm.getAllCourtrooms();
        Map<String, Case> caseMap = new HashMap<>();
        for (Case c : cm.getAllCases()) caseMap.put(c.getCaseId(), c);
        Map<String, Judge> judgeMap = new HashMap<>();
        for (Judge j : cm.getAllJudges()) judgeMap.put(j.getJudgeId(), j);
        Map<String, Courtroom> roomMap = new HashMap<>();
        for (Courtroom cr : rooms) roomMap.put(cr.getCourtroomId(), cr);

        sectionHeader("COURT TIMELINE — " + date);

        if (rooms.isEmpty()) {
            warn("No courtrooms available.");
        } else {
            // Timeline header: 6 slots (each 11 chars wide)
            System.out.println(GRAY + "  ┌──────────┬───────────┬───────────┬───────────┬───────────┬───────────┬───────────┐" + RESET);
            System.out.println(GRAY + "  │ " + BRIGHT_CYAN + BOLD + padRight("Room", 8) + RESET
                    + GRAY + " │ " + BRIGHT_WHITE + "S1 09:00 " + RESET
                    + GRAY + " │ " + BRIGHT_WHITE + "S2 10:00 " + RESET
                    + GRAY + " │ " + BRIGHT_WHITE + "S3 11:00 " + RESET
                    + GRAY + " │ " + BRIGHT_WHITE + "S4 13:00 " + RESET
                    + GRAY + " │ " + BRIGHT_WHITE + "S5 14:00 " + RESET
                    + GRAY + " │ " + BRIGHT_WHITE + "S6 15:00 " + RESET
                    + GRAY + " │" + RESET);
            System.out.println(GRAY + "  ├──────────┼───────────┼───────────┼───────────┼───────────┼───────────┼───────────┤" + RESET);

            for (Courtroom cr : rooms) {
                Schedule[] slotGrid = new Schedule[CourtManager.MAX_DAILY_SLOTS + 1];
                for (Schedule s : daily) {
                    if (s.getCourtroomId().equalsIgnoreCase(cr.getCourtroomId())) {
                        for (int sl = s.getStartSlot(); sl <= s.getEndSlot() && sl <= CourtManager.MAX_DAILY_SLOTS; sl++) {
                            slotGrid[sl] = s;
                        }
                    }
                }

                StringBuilder row = new StringBuilder();
                String roomTag = cr.isHighSecurity()
                        ? BRIGHT_MAGENTA + BOLD + padRight(cr.getCourtroomId() + "*", 8) + RESET
                        : BRIGHT_WHITE + BOLD + padRight(cr.getCourtroomId(), 8) + RESET;
                row.append(GRAY).append("  │ ").append(RESET).append(roomTag).append(GRAY).append(" │ ").append(RESET);

                for (int sl = 1; sl <= CourtManager.MAX_DAILY_SLOTS; sl++) {
                    Schedule s = slotGrid[sl];
                    if (s == null) {
                        row.append(GRAY).append("  · free · ").append(RESET);
                    } else {
                        Case c = caseMap.get(s.getCaseId());
                        String color = (c != null) ? caseTypeColor(c.getCaseType()) : BRIGHT_CYAN;
                        boolean isStart = (s.getStartSlot() == sl);
                        String cellText = isStart ? "[" + s.getCaseId() + "]" : "  └──▶   ";
                        row.append(color).append(BOLD).append(padRight(truncate(cellText, 9), 9)).append(RESET);
                    }
                    row.append(GRAY).append(" │ ").append(RESET);
                }
                System.out.println(row);
            }
            System.out.println(GRAY + "  └──────────┴───────────┴───────────┴───────────┴───────────┴───────────┴───────────┘" + RESET);
            System.out.println(GRAY + "  Legend: " + BRIGHT_BLUE + "■ Civil  " + BRIGHT_RED + "■ Criminal  "
                    + BRIGHT_YELLOW + "■ Property  " + BRIGHT_MAGENTA + "* High-Security Room" + RESET);
        }

        sectionHeader("DETAILED DAILY SCHEDULE — " + date);
        if (daily.isEmpty()) {
            warn("No hearings scheduled for " + date + ".");
            return;
        }
        String[] headers = {"Time Slot", "Case", "Type", "Judge", "Courtroom", "Priority"};
        int[] widths = {18, 8, 10, 18, 22, 8};
        List<String[]> rows = new ArrayList<>();
        for (Schedule s : daily) {
            Case c = caseMap.get(s.getCaseId());
            Judge j = judgeMap.get(s.getJudgeId());
            Courtroom cr = roomMap.get(s.getCourtroomId());

            rows.add(new String[]{
                    BRIGHT_WHITE + s.getSlotRangeDescription() + RESET,
                    BRIGHT_CYAN + BOLD + s.getCaseId() + RESET,
                    (c != null) ? colorCaseType(c.getCaseType()) : "N/A",
                    truncate((j != null) ? j.getName() : s.getJudgeId(), 18),
                    truncate((cr != null) ? cr.getRoomName() : s.getCourtroomId(), 22),
                    (c != null) ? colorPriority(c.getPriority()) : "N/A"
            });
        }
        printTable(headers, rows, widths);
    }

    // --- Analytics & Utilization Dashboard ---

    public static void printAnalyticsDashboard(CourtManager cm) {
        List<Case> cases = cm.getAllCases();
        List<Judge> judges = cm.getAllJudges();
        List<Courtroom> rooms = cm.getAllCourtrooms();
        List<String> dates = cm.getCourtDates();

        int total = cases.size();
        int scheduled = 0, pending = 0, rescheduled = 0, highTotal = 0, highDone = 0;
        for (Case c : cases) {
            if (c.getStatus() == CaseStatus.SCHEDULED) scheduled++;
            else if (c.getStatus() == CaseStatus.PENDING) pending++;
            else if (c.getStatus() == CaseStatus.RESCHEDULED) rescheduled++;

            if (c.getPriority() == Priority.HIGH) {
                highTotal++;
                if (c.getStatus() != CaseStatus.PENDING) highDone++;
            }
        }

        int totalPossibleJudge = judges.size() * dates.size() * CourtManager.MAX_DAILY_SLOTS;
        int bookedJudge = 0;
        for (Judge j : judges) {
            for (String d : dates) bookedJudge += j.getBookedSlotsForDate(d).size();
        }
        double judgeUtil = totalPossibleJudge > 0 ? (bookedJudge * 100.0 / totalPossibleJudge) : 0.0;

        int totalPossibleRoom = rooms.size() * dates.size() * CourtManager.MAX_DAILY_SLOTS;
        int bookedRoom = 0;
        for (Courtroom cr : rooms) {
            for (String d : dates) bookedRoom += cr.getBookedSlotsForDate(d).size();
        }
        double roomUtil = totalPossibleRoom > 0 ? (bookedRoom * 100.0 / totalPossibleRoom) : 0.0;

        int conflicts = cm.detectConflicts().size();

        sectionHeader("COURT ANALYTICS & RESOURCE UTILIZATION");
        System.out.println(GRAY + "  ┌──────────────────────────────────────────────────────────────────────┐" + RESET);
        printKvLine("Total Cases", BRIGHT_WHITE + BOLD + total + RESET
                + GRAY + "  (" + BRIGHT_GREEN + scheduled + " Scheduled" + GRAY + ", "
                + BRIGHT_YELLOW + pending + " Pending" + GRAY + ", "
                + BRIGHT_CYAN + rescheduled + " Rescheduled" + GRAY + ")" + RESET);
        printKvLine("High-Priority", (highDone == highTotal ? BRIGHT_GREEN : BRIGHT_YELLOW)
                + BOLD + highDone + " / " + highTotal + RESET + GRAY + " urgent cases assigned" + RESET);
        printKvLine("Conflict Guard", conflicts == 0
                ? BRIGHT_GREEN + BOLD + "0 CONFLICTS DETECTED" + RESET
                : BRIGHT_RED + BOLD + conflicts + " CONFLICT(S) FOUND" + RESET);
        printKvLine("Judge Util.", progressBar(judgeUtil, 24) + "  " + BRIGHT_WHITE + String.format("%.1f%%", judgeUtil) + RESET
                + GRAY + " (" + bookedJudge + "/" + totalPossibleJudge + " slots)" + RESET);
        printKvLine("Courtroom Util.", progressBar(roomUtil, 24) + "  " + BRIGHT_WHITE + String.format("%.1f%%", roomUtil) + RESET
                + GRAY + " (" + bookedRoom + "/" + totalPossibleRoom + " slots)" + RESET);
        System.out.println(GRAY + "  └──────────────────────────────────────────────────────────────────────┘" + RESET);
    }

    private static void printKvLine(String key, String value) {
        String content = BRIGHT_CYAN + padRight(key, 17) + RESET + GRAY + ": " + RESET + value;
        System.out.println(GRAY + "  │ " + RESET + padRight(content, 68) + GRAY + " │" + RESET);
    }

    public static String progressBar(double percent, int width) {
        double clamped = Math.max(0.0, Math.min(100.0, percent));
        int filled = (int) Math.round((clamped / 100.0) * width);
        String color = clamped >= 75.0 ? BRIGHT_GREEN : (clamped >= 40.0 ? BRIGHT_CYAN : BRIGHT_YELLOW);
        return GRAY + "[" + color + "█".repeat(filled) + GRAY + "░".repeat(width - filled) + "]" + RESET;
    }

    // --- Generic Box-Drawn Table ---

    public static void printTable(String[] headers, List<String[]> rows, int[] widths) {
        String top    = buildBorder("┌", "┬", "┐", widths);
        String mid    = buildBorder("├", "┼", "┤", widths);
        String bottom = buildBorder("└", "┴", "┘", widths);

        System.out.println(GRAY + "  " + top + RESET);
        StringBuilder hdr = new StringBuilder(GRAY + "  │ " + RESET);
        for (int i = 0; i < headers.length; i++) {
            hdr.append(BRIGHT_CYAN).append(BOLD).append(padRight(headers[i], widths[i])).append(RESET);
            hdr.append(GRAY).append(" │ ").append(RESET);
        }
        System.out.println(hdr);
        System.out.println(GRAY + "  " + mid + RESET);

        for (String[] row : rows) {
            StringBuilder line = new StringBuilder(GRAY + "  │ " + RESET);
            for (int i = 0; i < widths.length; i++) {
                String cell = (i < row.length && row[i] != null) ? row[i] : "";
                line.append(padRight(cell, widths[i]));
                line.append(GRAY).append(" │ ").append(RESET);
            }
            System.out.println(line);
        }
        System.out.println(GRAY + "  " + bottom + RESET);
    }

    private static String buildBorder(String left, String join, String right, int[] widths) {
        StringBuilder sb = new StringBuilder(left);
        for (int i = 0; i < widths.length; i++) {
            sb.append("─".repeat(widths[i] + 2));
            sb.append(i < widths.length - 1 ? join : right);
        }
        return sb.toString();
    }

    // --- Badge & Color Helpers ---

    private static String caseTypeColor(CaseType type) {
        switch (type) {
            case CIVIL:    return BRIGHT_BLUE;
            case CRIMINAL: return BRIGHT_RED;
            case PROPERTY: return BRIGHT_YELLOW;
            default:       return WHITE;
        }
    }

    public static String colorCaseType(CaseType type) {
        String name = type.name().charAt(0) + type.name().substring(1).toLowerCase();
        return caseTypeColor(type) + BOLD + name + RESET;
    }

    public static String colorPriority(Priority p) {
        switch (p) {
            case HIGH:   return BRIGHT_RED + BOLD + "HIGH" + RESET;
            case MEDIUM: return BRIGHT_YELLOW + BOLD + "MEDIUM" + RESET;
            case LOW:    return BRIGHT_GREEN + "LOW" + RESET;
            default:     return WHITE + p.name() + RESET;
        }
    }

    public static String colorStatus(CaseStatus s) {
        switch (s) {
            case SCHEDULED:   return BRIGHT_GREEN + BOLD + "SCHEDULED" + RESET;
            case RESCHEDULED: return BRIGHT_CYAN + BOLD + "RESCHEDULED" + RESET;
            case PENDING:     return BRIGHT_YELLOW + "PENDING" + RESET;
            default:          return WHITE + s.name() + RESET;
        }
    }

    // --- String & ANSI Width Helpers ---

    public static String stripAnsi(String s) {
        return (s == null) ? "" : ANSI_PATTERN.matcher(s).replaceAll("");
    }

    public static int visibleLength(String s) {
        return stripAnsi(s).length();
    }

    public static String padRight(String s, int width) {
        int vis = visibleLength(s);
        if (vis >= width) return s;
        return s + " ".repeat(width - vis);
    }

    public static String padSpaces(int count) {
        return count > 0 ? " ".repeat(count) : "";
    }

    public static String truncate(String s, int maxLen) {
        if (s == null) return "";
        if (s.length() <= maxLen) return s;
        if (maxLen <= 1) return s.substring(0, maxLen);
        return s.substring(0, maxLen - 1) + "…";
    }
}
