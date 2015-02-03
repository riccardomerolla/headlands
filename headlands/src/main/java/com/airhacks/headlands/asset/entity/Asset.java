package com.airhacks.headlands.asset.entity;

/**
 * @author Riccardo Merolla
 *         Created on 31/01/15.
 */
public class Asset {

    private String id;

    private String code;

    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
