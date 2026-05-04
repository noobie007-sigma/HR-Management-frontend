package com.example.HR_Management_Frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationDTO {

    // ===== CORE ID (from backend SDR) =====
    private BigDecimal id;

    // ===== LEGACY/UI ID (used in your code) =====
    private Long locationId;

    private String streetAddress;
    private String postalCode;
    private String city;
    private String stateProvince;

    // ===== NESTED COUNTRY (from projection JSON) =====
    private CountryDTO country;

    // ===== HAL LINKS =====
    @JsonProperty("_links")
    private Links links;

    public LocationDTO() {}

    // =================================================
    // 🔹 SAFE ID HANDLING (VERY IMPORTANT)
    // =================================================

    public Long getLocationId() {
        if (locationId != null) return locationId;
        if (id != null) return id.longValue();

        Long extracted = extractId();
        return extracted != null ? extracted : null;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public BigDecimal getId() {
        return id;
    }

    public void setId(BigDecimal id) {
        this.id = id;
    }

    // =================================================
    // BASIC FIELDS
    // =================================================

    public String getStreetAddress() { return streetAddress; }
    public void setStreetAddress(String v) { this.streetAddress = v; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String v) { this.postalCode = v; }

    public String getCity() { return city; }
    public void setCity(String v) { this.city = v; }

    public String getStateProvince() { return stateProvince; }
    public void setStateProvince(String v) { this.stateProvince = v; }

    // =================================================
    // COUNTRY (NULL SAFE)
    // =================================================

    public CountryDTO getCountry() { return country; }
    public void setCountry(CountryDTO country) { this.country = country; }

    public String getCountryId() {
        return (country != null) ? country.getCountryId() : "—";
    }

    public String getCountryName() {
        return (country != null) ? country.getCountryName() : "—";
    }

    // =================================================
    // HAL LINKS SUPPORT
    // =================================================

    public Links getLinks() { return links; }
    public void setLinks(Links links) { this.links = links; }

    public Long extractId() {
        if (links == null || links.getSelf() == null) return null;

        try {
            String href = links.getSelf().getHref().replaceAll("\\{.*}", "");
            String[] parts = href.split("/");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (Exception e) {
            return null;
        }
    }

    // =================================================
    // NESTED CLASSES
    // =================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CountryDTO {
        private String countryId;
        private String countryName;

        public String getCountryId() { return countryId; }
        public void setCountryId(String v) { this.countryId = v; }

        public String getCountryName() { return countryName; }
        public void setCountryName(String v) { this.countryName = v; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Links {
        private HRef self;

        public HRef getSelf() { return self; }
        public void setSelf(HRef self) { this.self = self; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HRef {
        private String href;

        public String getHref() { return href; }
        public void setHref(String href) { this.href = href; }
    }
}