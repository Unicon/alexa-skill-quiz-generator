package org.unicon.lex.intents.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LexIntent {

    private String name;
    private Map<String, String> slots;
    private Map<String, Object> slotDetails;
    private String confirmationStatus;

    public LexIntent() {
        slots = new HashMap<>();
        slotDetails = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getSlots() {
        return slots;
    }

    public void setSlots(Map<String, String> slots) {
        this.slots = slots;
    }

    public Map<String, Object> getSlotDetails() {
        return slotDetails;
    }

    public void setSlotDetails(Map<String, Object> slotDetails) {
        this.slotDetails = slotDetails;
    }

    public String getConfirmationStatus() {
        return confirmationStatus;
    }

    public void setConfirmationStatus(String confirmationStatus) {
        this.confirmationStatus = confirmationStatus;
    }

    @Override
    public String toString() {
        return "LexIntent{" +
                "name='" + name + '\'' +
                ", slots=" + slots +
                ", slotDetails=" + slotDetails +
                ", confirmationStatus=" + confirmationStatus +
                '}';
    }
}
