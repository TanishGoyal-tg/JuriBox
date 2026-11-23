package juribox.exception;

/**
 * Consolidated custom exception hierarchy for court scheduling operations.
 * Houses specific exception subtypes as static nested classes to minimize file clutter.
 */
public class CourtSchedulingException extends Exception {
    private static final long serialVersionUID = 1L;

    public CourtSchedulingException(String message) {
        super(message);
    }

    public CourtSchedulingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Thrown when an entity (Case, Judge, Courtroom) with the same ID already exists.
     */
    public static class DuplicateEntityException extends CourtSchedulingException {
        private static final long serialVersionUID = 1L;
        public DuplicateEntityException(String message) {
            super(message);
        }
    }

    /**
     * Thrown when a requested entity ID does not exist in the system.
     */
    public static class EntityNotFoundException extends CourtSchedulingException {
        private static final long serialVersionUID = 1L;
        public EntityNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * Thrown when a proposed schedule conflicts with judge, courtroom, or slot constraints.
     */
    public static class SchedulingConflictException extends CourtSchedulingException {
        private static final long serialVersionUID = 1L;
        public SchedulingConflictException(String message) {
            super(message);
        }
    }
}
