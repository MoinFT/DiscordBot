package de.moinFT.utils;

public enum BotRoleType {

    EVERYONE(0),
    COLOR(1),
    USER(2),
    UNKNOWN(-1);

    private final int value;

    BotRoleType(int value) {
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
     * Gets an {@code BotRoleType} by its value.
     *
     * @param value The value of the bot role type.
     * @return The bot role type for the given value,
     *         or {@link BotRoleType#UNKNOWN} if there's none with the given value.
     */
    public static BotRoleType fromValue(int value) {
        for (BotRoleType type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public static String getString(BotRoleType value) {
        switch (value) {
            case EVERYONE:
                return "everyone";
            case COLOR:
                return "color";
            case USER:
                return "user";
            default:
                return "unknown";
        }
    }
}