package juribox.service;

import juribox.model.*;
import juribox.model.Case.*;

import java.sql.*;
import java.util.*;

/*
  Module: Relational Persistence Engine & MySQL JDBC Connectivity
  Member: Lakshit
  Date: 20 November 2026 - 21 November 2026
  Topic: MySQL JDBC (Driver, PreparedStatement, ResultSet, Batch Transactions)
 */
public final class DatabaseManager {
    // [Lakshit - 20 Nov 2026] Topic: Standard MySQL JDBC Connection Configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/juribox_db?createDatabaseIfNotExist=true";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "your_password";

    public DatabaseManager(String dataDirectory) {
        initializeSchema();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private void initializeSchema() {
        String createCasesSql = "CREATE TABLE IF NOT EXISTS cases (" +
                "case_id VARCHAR(50) PRIMARY KEY, " +
                "title VARCHAR(255) NOT NULL, " +
                "case_type VARCHAR(50) NOT NULL, " +
                "priority VARCHAR(50) NOT NULL, " +
                "duration_slots INT NOT NULL, " +
                "required_specialization VARCHAR(50) NOT NULL, " +
                "status VARCHAR(50) NOT NULL, " +
                "assigned_schedule_id VARCHAR(50), " +
                "dispute_amount DOUBLE, " +
                "mediation_attempted INT, " +
                "in_custody INT, " +
                "violent_offense INT, " +
                "property_location VARCHAR(255), " +
                "injunction_requested INT" +
                ");";

        String createJudgesSql = "CREATE TABLE IF NOT EXISTS judges (" +
                "judge_id VARCHAR(50) PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "specialization VARCHAR(50) NOT NULL" +
                ");";

        String createCourtroomsSql = "CREATE TABLE IF NOT EXISTS courtrooms (" +
                "courtroom_id VARCHAR(50) PRIMARY KEY, " +
                "room_name VARCHAR(255) NOT NULL, " +
                "high_security INT NOT NULL" +
                ");";

        String createSchedulesSql = "CREATE TABLE IF NOT EXISTS schedules (" +
                "schedule_id VARCHAR(50) PRIMARY KEY, " +
                "case_id VARCHAR(50) NOT NULL, " +
                "judge_id VARCHAR(50) NOT NULL, " +
                "courtroom_id VARCHAR(50) NOT NULL, " +
                "hearing_date VARCHAR(50) NOT NULL, " +
                "start_slot INT NOT NULL, " +
                "duration_slots INT NOT NULL" +
                ");";

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(createCasesSql);
            stmt.execute(createJudgesSql);
            stmt.execute(createCourtroomsSql);
            stmt.execute(createSchedulesSql);
        } catch (SQLException e) {
            System.err.println("[JDBC NOTICE] MySQL connection pending (" + e.getMessage() + ")");
        }
    }

    public void saveCase(Case c) throws SQLException {
        String sql;
        if (c instanceof CivilCase) {
            sql = "REPLACE INTO cases (" +
                    "case_id, title, case_type, priority, duration_slots, required_specialization, " +
                    "status, assigned_schedule_id, dispute_amount, mediation_attempted" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        } else if (c instanceof CriminalCase) {
            sql = "REPLACE INTO cases (" +
                    "case_id, title, case_type, priority, duration_slots, required_specialization, " +
                    "status, assigned_schedule_id, in_custody, violent_offense" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        } else { // PropertyCase
            sql = "REPLACE INTO cases (" +
                    "case_id, title, case_type, priority, duration_slots, required_specialization, " +
                    "status, assigned_schedule_id, property_location, injunction_requested" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        }

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            // Common Case attributes (Slots 1 - 8)
            ps.setString(1, c.getCaseId());
            ps.setString(2, c.getTitle());
            ps.setString(3, c.getCaseType().name());
            ps.setString(4, c.getPriority().name());
            ps.setInt(5, c.getDurationSlots());
            ps.setString(6, c.getRequiredSpecialization().name());
            ps.setString(7, c.getStatus().name());
            ps.setString(8, c.getAssignedScheduleId());

            // Type-specific attributes (Slots 9 & 10)
            if (c instanceof CivilCase) {
                CivilCase civil = (CivilCase) c;
                ps.setDouble(9, civil.getDisputeAmount());
                ps.setInt(10, civil.isMediationAttempted() ? 1 : 0);
            } else if (c instanceof CriminalCase) {
                CriminalCase crim = (CriminalCase) c;
                ps.setInt(9, crim.isInCustody() ? 1 : 0);
                ps.setInt(10, crim.isViolentOffense() ? 1 : 0);
            } else if (c instanceof PropertyCase) {
                PropertyCase prop = (PropertyCase) c;
                ps.setString(9, prop.getPropertyLocation());
                ps.setInt(10, prop.isInjunctionRequested() ? 1 : 0);
            }

            ps.executeUpdate();
        }
    }

    public void saveJudge(Judge judge) throws SQLException {
        String sql = "REPLACE INTO judges (judge_id, name, specialization) VALUES (?, ?, ?);";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, judge.getJudgeId());
            ps.setString(2, judge.getName());
            ps.setString(3, judge.getSpecialization().name());
            ps.executeUpdate();
        }
    }

    public void saveCourtroom(Courtroom cr) throws SQLException {
        String sql = "REPLACE INTO courtrooms (courtroom_id, room_name, high_security) VALUES (?, ?, ?);";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cr.getCourtroomId());
            ps.setString(2, cr.getRoomName());
            ps.setInt(3, cr.isHighSecurity() ? 1 : 0);
            ps.executeUpdate();
        }
    }

    public void saveSchedule(Schedule s) throws SQLException {
        String sql = "REPLACE INTO schedules (schedule_id, case_id, judge_id, courtroom_id, hearing_date, start_slot, duration_slots) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getScheduleId());
            ps.setString(2, s.getCaseId());
            ps.setString(3, s.getJudgeId());
            ps.setString(4, s.getCourtroomId());
            ps.setString(5, s.getDate());
            ps.setInt(6, s.getStartSlot());
            ps.setInt(7, s.getDurationSlots());
            ps.executeUpdate();
        }
    }

    public List<Case> loadCases() throws SQLException {
        List<Case> cases = new ArrayList<>();
        String sql = "SELECT * FROM cases;";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("case_id");
                String title = rs.getString("title");
                CaseType type = CaseType.fromString(rs.getString("case_type"));
                Priority priority = Priority.fromString(rs.getString("priority"));
                int duration = rs.getInt("duration_slots");
                CaseType reqSpec = CaseType.fromString(rs.getString("required_specialization"));
                CaseStatus status = CaseStatus.fromString(rs.getString("status"));
                String scheduleId = rs.getString("assigned_schedule_id");

                Case c;
                switch (type) {
                    case CIVIL:
                        double claim = rs.getDouble("dispute_amount");
                        boolean med = rs.getInt("mediation_attempted") == 1;
                        c = new CivilCase(id, title, priority, duration, claim, med);
                        break;
                    case CRIMINAL:
                        boolean inCustody = rs.getInt("in_custody") == 1;
                        boolean violent = rs.getInt("violent_offense") == 1;
                        c = new CriminalCase(id, title, priority, duration, inCustody, violent);
                        break;
                    case PROPERTY:
                        String loc = rs.getString("property_location");
                        boolean inj = rs.getInt("injunction_requested") == 1;
                        c = new PropertyCase(id, title, priority, duration, loc, inj);
                        break;
                    default:
                        continue;
                }

                c.setRequiredSpecialization(reqSpec);
                c.setStatus(status);
                c.setAssignedScheduleId(scheduleId);
                cases.add(c);
            }
        }
        return cases;
    }

    public List<Judge> loadJudges() throws SQLException {
        List<Judge> judges = new ArrayList<>();
        String sql = "SELECT * FROM judges;";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString("judge_id");
                String name = rs.getString("name");
                CaseType spec = CaseType.fromString(rs.getString("specialization"));
                judges.add(new Judge(id, name, spec));
            }
        }
        return judges;
    }

    public List<Courtroom> loadCourtrooms() throws SQLException {
        List<Courtroom> courtrooms = new ArrayList<>();
        String sql = "SELECT * FROM courtrooms;";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString("courtroom_id");
                String name = rs.getString("room_name");
                boolean highSec = rs.getInt("high_security") == 1;
                courtrooms.add(new Courtroom(id, name, highSec));
            }
        }
        return courtrooms;
    }

    public List<Schedule> loadSchedules() throws SQLException {
        List<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT * FROM schedules;";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String sId = rs.getString("schedule_id");
                String cId = rs.getString("case_id");
                String jId = rs.getString("judge_id");
                String crId = rs.getString("courtroom_id");
                String date = rs.getString("hearing_date");
                int startSlot = rs.getInt("start_slot");
                int duration = rs.getInt("duration_slots");

                schedules.add(new Schedule(sId, cId, jId, crId, date, startSlot, duration));
            }
        }
        return schedules;
    }

    public void updateCaseStatus(String caseId, CaseStatus newStatus, String scheduleId) throws SQLException {
        String sql = "UPDATE cases SET status = ?, assigned_schedule_id = ? WHERE case_id = ?;";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setString(2, scheduleId);
            ps.setString(3, caseId);
            ps.executeUpdate();
        }
    }

    public void deleteCase(String caseId) throws SQLException {
        String sql = "DELETE FROM cases WHERE case_id = ?;";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, caseId);
            ps.executeUpdate();
        }
    }

    public void saveAllToDatabase(List<Case> cases,
            Collection<Judge> judges,
            Collection<Courtroom> courtrooms,
            List<Schedule> schedules) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (Judge j : judges)
                    saveJudge(j);
                for (Courtroom cr : courtrooms)
                    saveCourtroom(cr);
                for (Case c : cases)
                    saveCase(c);
                for (Schedule s : schedules)
                    saveSchedule(s);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
