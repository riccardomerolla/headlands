package com.airhacks.headlands.maintenance.entity;

import java.util.Date;

/**
 * @author Riccardo Merolla
 *         Created on 03/02/15.
 */
public class Maintenance {

    private String uuid;

    private Date date;

    private String asset;

    private String eventType;

    private String description;

    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
