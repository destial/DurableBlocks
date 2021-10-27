package xyz.destiall.durableblocks.api;

import java.util.UUID;

class DurabilityBarImpl implements DurabilityBar {
    private Color color;
    private String message;
    private float value;
    private final UUID uuid;
    public DurabilityBarImpl(Color color, String message, float value) {
        this.color = color;
        this.message = message;
        this.value = value;
        uuid = UUID.randomUUID();
    }

    public Color getColor() {
        return color;
    }

    public String getMessage() {
        return message;
    }

    public float getValue() {
        return value;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }
}
