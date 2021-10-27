package xyz.destiall.durableblocks.api;

import java.util.UUID;

public interface DurabilityBar {
    Color getColor();
    String getMessage();
    float getValue();
    void setColor(Color color);
    void setMessage(String message);
    void setValue(float value);
    UUID getUUID();

    enum Color {
        BLUE,
        GREEN,
        PINK,
        WHITE,
        PURPLE,
        RED,
        YELLOW
    }
}
