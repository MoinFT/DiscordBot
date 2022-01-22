package de.moinFT.utils;

public enum CommandRequestType {

    BOT_PERMISSION(0),
    UNKNOWN(-1);

    private final int value;

    CommandRequestType(int value) {
        this.value = value;
    }

    /**
     * Gets integer value that represents this type.
     *
     * @return The integer value that represents this type.
     */
    public int getValue() {
        return value;
    }

    /**
     * Gets an {@code CommandType} by its value.
     *
     * @param value The value of the command type.
     * @return The command type for the given value,
     *         or {@link CommandRequestType#UNKNOWN} if there's none with the given value.
     */
    public static CommandRequestType fromValue(int value) {
        for (CommandRequestType type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
