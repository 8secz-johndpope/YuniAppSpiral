package com.android.yuniapp.model;

public class EntitiesModel implements Comparable<EntitiesModel>{
    private float x;
    private float y;
    private String entityName;
    private String entityType;
    private int entityId;
    private int priority;
    private int daysDiff;

    public EntitiesModel(float x, float y, String entityName, String entityType, int entityId, int priority, int daysDiff) {
        this.x = x;
        this.y = y;
        this.entityName = entityName;
        this.entityType = entityType;
        this.entityId = entityId;
        this.priority = priority;
        this.daysDiff = daysDiff;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getDaysDiff() {
        return daysDiff;
    }

    public void setDaysDiff(int daysDiff) {
        this.daysDiff = daysDiff;
    }

    @Override
    public int compareTo(EntitiesModel o) {
        int var=daysDiff-o.daysDiff;
        return var;
    }
}
