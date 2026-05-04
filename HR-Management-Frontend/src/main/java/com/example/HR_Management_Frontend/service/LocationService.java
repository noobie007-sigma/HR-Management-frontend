package com.example.HR_Management_Frontend.service;

import com.example.HR_Management_Frontend.dto.DepartmentDTO;
import com.example.HR_Management_Frontend.dto.LocationDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class LocationService {

    private final RestTemplate restTemplate;
    private final String backendUrl;

    public LocationService(RestTemplate restTemplate,
                           @Value("${backend.base-url}") String backendUrl) {
        this.restTemplate = restTemplate;
        this.backendUrl = backendUrl;
    }

    // ============================================================
    // GET ALL LOCATIONS — paginated
    // GET /api/v1/locations?page={page}&size={size}
    // ============================================================
    public LocationPageResult getAllLocations(int page, int size) {
        try {
            String url = backendUrl + "/locations?page=" + page + "&size=" + size;
            LocationHalPage result = restTemplate.getForObject(url, LocationHalPage.class);
            if (result == null) return new LocationPageResult(new ArrayList<>(), 0, 1);

            List<LocationDTO> locations = result.getLocations();
            locations.forEach(loc -> {
                if (loc.getLocationId() == null) loc.setLocationId(loc.extractId());
            });

            int totalPages = result.getPage() != null ? result.getPage().totalPages : 1;
            long totalElements = result.getPage() != null ? result.getPage().totalElements : 0;
            return new LocationPageResult(locations, totalElements, totalPages);
        } catch (Exception e) {
            System.err.println("getAllLocations error: " + e.getMessage());
            return new LocationPageResult(new ArrayList<>(), 0, 1);
        }
    }

    // ============================================================
    // SEARCH BY CITY
    // GET /api/v1/locations/search/findByCity?city={city}
    // ============================================================
    public List<LocationDTO> searchByCity(String city) {
        try {
            String url = backendUrl + "/locations/search/findByCity?city=" + city;
            LocationSearchHal result = restTemplate.getForObject(url, LocationSearchHal.class);
            if (result == null) return new ArrayList<>();
            List<LocationDTO> locations = result.getLocations();
            locations.forEach(loc -> {
                if (loc.getLocationId() == null) loc.setLocationId(loc.extractId());
            });
            return locations;
        } catch (Exception e) {
            System.err.println("searchByCity error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ============================================================
    // SEARCH BY STATE
    // GET /api/v1/locations/search/findByStateProvince?stateProvince={state}
    // ============================================================
    public List<LocationDTO> searchByState(String state) {
        try {
            String url = backendUrl + "/locations/search/findByStateProvince?stateProvince=" + state;
            LocationSearchHal result = restTemplate.getForObject(url, LocationSearchHal.class);
            if (result == null) return new ArrayList<>();
            List<LocationDTO> locations = result.getLocations();
            locations.forEach(loc -> {
                if (loc.getLocationId() == null) loc.setLocationId(loc.extractId());
            });
            return locations;
        } catch (Exception e) {
            System.err.println("searchByState error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ============================================================
    // GET SINGLE LOCATION BY ID
    // GET /api/v1/locations/{id}
    // Also fetches countryId separately via association endpoint
    // ============================================================
    public Optional<LocationDTO> getLocationById(Long id) {
        try {
            String url = backendUrl + "/locations/" + id;
            LocationDTO loc = restTemplate.getForObject(url, LocationDTO.class);
            if (loc == null) return Optional.empty();
            if (loc.getLocationId() == null) {
                loc.setLocationId(loc.extractId());
                if (loc.getLocationId() == null) loc.setLocationId(id);
            }
            
            return Optional.of(loc);
        } catch (Exception e) {
            System.err.println("getLocationById error: " + e.getMessage());
            return Optional.empty();
        }
    }

    // ============================================================
    // FETCH COUNTRY ID FOR A LOCATION
    // GET /api/v1/locations/{id}/country
    // ============================================================
    @SuppressWarnings("unchecked")
    private String fetchCountryId(Long locationId) {
        try {
            String url = backendUrl + "/locations/" + locationId + "/country";
            Map<String, Object> result = restTemplate.getForObject(url, Map.class);
            if (result == null) return null;
            return (String) result.get("countryId");
        } catch (Exception e) {
            return null;
        }
    }

    // ============================================================
    // GET EMPLOYEE COUNT FOR A LOCATION
    // GET /api/v1/locations/{id}/employee-count  (your custom endpoint)
    // ============================================================
    @SuppressWarnings("unchecked")
    public long getEmployeeCount(Long locationId) {
        try {
            String url = backendUrl + "/locations/" + locationId + "/employee-count";
            Map<String, Object> result = restTemplate.getForObject(url, Map.class);
            if (result == null) return 0L;
            Object count = result.get("employeeCount");
            if (count instanceof Number n) return n.longValue();
            return 0L;
        } catch (Exception e) {
            System.err.println("getEmployeeCount error: " + e.getMessage());
            return 0L;
        }
    }

    // ============================================================
    // GET DEPARTMENTS FOR A LOCATION
    // GET /api/v1/departments/search/findByLocation_Id?locationId={id}
    // NOTE: Must add findByLocation_Id to DepartmentRepository in backend
    // ============================================================
    public List<DepartmentDTO> getDepartmentsForLocation(Long locationId) {
        try {
            // Updated URL to match your screenshot: /department/search/byLocation
            String url = backendUrl + "/department/search/byLocation?locationId=" + locationId;
            DepartmentSearchHal result = restTemplate.getForObject(url, DepartmentSearchHal.class);
            
            if (result == null) return new ArrayList<>();
            
            List<DepartmentDTO> depts = result.getDepartments();
            if (depts == null) return new ArrayList<>();
            
            depts.forEach(dept -> {
                if (dept.getDepartmentId() == null && dept.extractId() != null) {
                    dept.setDepartmentId(java.math.BigDecimal.valueOf(dept.extractId()));
                }
            });
            return depts;
        } catch (Exception e) {
            System.err.println("getDepartmentsForLocation error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ============================================================
    // INNER HAL WRAPPER CLASSES
    // ============================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LocationHalPage {
        @JsonProperty("_embedded") private EmbeddedLocations embedded;
        @JsonProperty("page")      private PageMeta page;

        public List<LocationDTO> getLocations() {
            return (embedded != null && embedded.locations != null)
                    ? embedded.locations : new ArrayList<>();
        }
        public void setEmbedded(EmbeddedLocations e) { this.embedded = e; }
        public PageMeta getPage() { return page; }
        public void setPage(PageMeta p) { this.page = p; }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class EmbeddedLocations {
            private List<LocationDTO> locations;
            public List<LocationDTO> getLocations() { return locations; }
            public void setLocations(List<LocationDTO> l) { this.locations = l; }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class PageMeta {
            public int totalPages;
            public long totalElements;
            public int size;
            public int number;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LocationSearchHal {
        @JsonProperty("_embedded") private EmbeddedLocations embedded;

        public List<LocationDTO> getLocations() {
            return (embedded != null && embedded.locations != null)
                    ? embedded.locations : new ArrayList<>();
        }
        public void setEmbedded(EmbeddedLocations e) { this.embedded = e; }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class EmbeddedLocations {
            private List<LocationDTO> locations;
            public List<LocationDTO> getLocations() { return locations; }
            public void setLocations(List<LocationDTO> l) { this.locations = l; }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DepartmentSearchHal {
        @JsonProperty("_embedded") private EmbeddedDepts embedded;

        public List<DepartmentDTO> getDepartments() {
            // Updated to call getDepartmentList()
            return (embedded != null && embedded.getDepartmentList() != null)
                    ? embedded.getDepartmentList() : new ArrayList<>();
        }
        public void setEmbedded(EmbeddedDepts e) { this.embedded = e; }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class EmbeddedDepts {
            // CRITICAL: Changed from "departments" to "department" to match your JSON
            @JsonProperty("department") 
            private List<DepartmentDTO> departmentList;

            public List<DepartmentDTO> getDepartmentList() { return departmentList; }
            public void setDepartmentList(List<DepartmentDTO> d) { this.departmentList = d; }
        }
    }
    // Simple result wrapper for paginated response
    public record LocationPageResult(List<LocationDTO> locations,
                                     long totalElements,
                                     int totalPages) {}
}