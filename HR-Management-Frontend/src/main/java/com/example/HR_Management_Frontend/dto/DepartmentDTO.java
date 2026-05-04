package com.example.HR_Management_Frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DepartmentDTO {

    // ===== COMMON =====
    private java.math.BigDecimal departmentId;
    private String departmentName;

    // ===== FROM EMPLOYEE RESPONSE =====
    private LocationDTO location;

    // ===== FROM LOCATION ASSOCIATION =====
    private Long managerId;
    private String locationCity;
    private String locationState;
    private Long locationId;

    @JsonProperty("_links")
    private Links links;

    public DepartmentDTO() {}

    // ===== GETTERS & SETTERS =====

    public java.math.BigDecimal getDepartmentId() { return departmentId; }
    public void setDepartmentId(java.math.BigDecimal v) { this.departmentId = v; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String v) { this.departmentName = v; }

    public LocationDTO getLocation() { return location; }
    public void setLocation(LocationDTO location) { this.location = location; }

    public Long getManagerId() { return managerId; }
    public void setManagerId(Long v) { this.managerId = v; }

    public String getLocationCity() { return locationCity; }
    public void setLocationCity(String v) { this.locationCity = v; }

    public String getLocationState() { return locationState; }
    public void setLocationState(String v) { this.locationState = v; }

    public Long getLocationId() { return locationId; }
    public void setLocationId(Long v) { this.locationId = v; }

    public Links getLinks() { return links; }
    public void setLinks(Links l) { this.links = l; }

    // ===== UTILITY METHODS =====

    /** Prefer nested location if available, else fallback */
    public String getLocationDisplay() {
        if (location != null && location.getCity() != null) {
            return location.getCity();
        }
        if (locationCity == null) return "—";
        if (locationState == null) return locationCity;
        return locationCity + ", " + locationState;
    }

    /** Extract ID from HAL link if needed */
    
    public Long extractId() {
        if (departmentId != null) return departmentId.longValue();

        if (links != null && links.getSelf() != null) {
            try {
                String href = links.getSelf().getHref().replaceAll("\\{.*}", "");
                String[] parts = href.split("/");
                return Long.parseLong(parts[parts.length - 1]);
            } catch (Exception ignored) {}
        }
        return null;
    }
    // ===== INNER CLASSES =====

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Links {
        private HRef self;

        public HRef getSelf() { return self; }
        public void setSelf(HRef s) { this.self = s; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HRef {
        private String href;

        public String getHref() { return href; }
        public void setHref(String h) { this.href = h; }
    }
}