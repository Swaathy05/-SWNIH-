package com.swnih.entity;

/**
 * Enumeration representing the priority levels for message classification.
 * Used by both keyword-based and ML classification systems.
 */
public enum PriorityLevel {
    /**
     * High priority messages - urgent items requiring immediate attention.
     * Examples: interview invitations, job offers, exam notifications, deadlines
     */
    HIGH("High Priority", 3),
    
    /**
     * Medium priority messages - important but not urgent items.
     * Examples: meeting invitations, reminders, schedule updates
     */
    MEDIUM("Medium Priority", 2),
    
    /**
     * Low priority messages - informational or promotional content.
     * Examples: sales notifications, discounts, newsletters, marketing emails
     */
    LOW("Low Priority", 1);

    private final String displayName;
    private final int numericValue;

    PriorityLevel(String displayName, int numericValue) {
        this.displayName = displayName;
        this.numericValue = numericValue;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getNumericValue() {
        return numericValue;
    }

    /**
     * Get priority level from numeric value.
     * @param value numeric value (1=LOW, 2=MEDIUM, 3=HIGH)
     * @return corresponding PriorityLevel
     * @throws IllegalArgumentException if value is not valid
     */
    public static PriorityLevel fromNumericValue(int value) {
        for (PriorityLevel level : values()) {
            if (level.numericValue == value) {
                return level;
            }
        }
        throw new IllegalArgumentException("Invalid priority level value: " + value);
    }

    /**
     * Get priority level from string (case-insensitive).
     * @param value string representation of priority level
     * @return corresponding PriorityLevel
     * @throws IllegalArgumentException if value is not valid
     */
    public static PriorityLevel fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Priority level value cannot be null or empty");
        }
        
        try {
            return PriorityLevel.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid priority level: " + value);
        }
    }

    /**
     * Check if this priority level is higher than another.
     * @param other the other priority level to compare
     * @return true if this priority is higher than the other
     */
    public boolean isHigherThan(PriorityLevel other) {
        return this.numericValue > other.numericValue;
    }

    /**
     * Check if this priority level is lower than another.
     * @param other the other priority level to compare
     * @return true if this priority is lower than the other
     */
    public boolean isLowerThan(PriorityLevel other) {
        return this.numericValue < other.numericValue;
    }

    @Override
    public String toString() {
        return displayName;
    }
}