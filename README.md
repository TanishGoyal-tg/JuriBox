# Court Case Scheduling Simulator (JuriBox)

A complete console-based Java application simulating automated court case scheduling, judge and courtroom allocation, multi-constraint conflict checking, rescheduling, and utilization analytics.

---

## 🏛️ Project Overview

In busy judiciary systems, manual case scheduling frequently causes:
- Double-booking judges or courtrooms across overlapping time slots.
- Scheduling hearing sessions when a judge or courtroom is unavailable.
- Assigning cases to judges outside their domain of expertise/specialization.
- Housing high-risk criminal undertrials in standard, unreinforced courtrooms.
- Neglecting urgent constitutional or high-stake matters in the docket backlog.

**JuriBox** solves these challenges using an automated, priority-driven greedy scheduling algorithm with multi-constraint validation and conflict detection.

---

## 📁 Project Structure

```
JuriBox/
├── bin/                             # Compiled Java bytecode
├── lib/                             # JDBC Driver (mysql-connector-j)
├── data/                            # Persistent text export
│   └── schedule_export.txt          # Exported court schedule
└── src/
    └── juribox/
        ├── app/
        │   └── Main.java            # Console interactive UI & menu loops
        ├── exception/
        │   └── CourtSchedulingException.java # Exception hierarchy with nested subtypes
        ├── model/
        │   ├── Case.java            # Abstract base class + nested enums (CaseType, Priority, CaseStatus)
        │   ├── CivilCase.java       # Concrete subclass (Civil domain)
        │   ├── CriminalCase.java    # Concrete subclass (Criminal & Security)
        │   ├── PropertyCase.java    # Concrete subclass (Property & Injunctions)
        │   ├── Judge.java           # Judge model & availability
        │   ├── Courtroom.java       # Courtroom model & security level
        │   └── Schedule.java        # Booking record binding case, judge, room, slots
        ├── service/
        │   ├── Scheduler.java       # Multi-criteria auto-scheduling algorithm
        │   ├── ConflictChecker.java # Detection of overlaps, hours, specialization, security
        │   ├── CourtSimulation.java # Concurrent courtroom threads, wait/notifyAll & simulation
        │   ├── DatabaseManager.java # MySQL JDBC connectivity, PreparedStatements & ResultSets
        │   └── CourtManager.java    # Central coordinator, file stream persistence & statistics
        └── test/
            └── SimulatorTest.java   # Automated end-to-end integration test suite
```

---

## 🚀 How to Compile and Run

### Prerequisites
- Java Development Kit (JDK 8 or higher, tested on OpenJDK 21)

### Compilation
From the project root directory (`d:\Study\2nd year\3rd sem\OOPS\JuriBox`):
```bash
# Compile all source files into bin directory
javac -cp "lib/*" -d bin src/juribox/model/*.java src/juribox/exception/*.java src/juribox/service/*.java src/juribox/app/*.java src/juribox/test/*.java
```

### Running the Interactive Application
```bash
# Windows
java -cp "bin;lib/*" juribox.app.Main

# macOS / Linux
java -cp "bin:lib/*" juribox.app.Main
```

### Running the Automated Test Suite
```bash
# Windows
java -cp "bin;lib/*" -ea juribox.test.SimulatorTest

# macOS / Linux
java -cp "bin:lib/*" -ea juribox.test.SimulatorTest
```


