package com.example.HR_Management_Frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeDTO {

    // ===== CORE FIELDS =====
    private BigDecimal employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

    private LocalDate hireDate;
    private BigDecimal salary;
    private BigDecimal commissionPct;

    // ===== FLAT UI FIELDS (from HEAD) =====
    private String jobId;
    private String jobTitle;

    // ===== NESTED OBJECTS (from SDR response) =====
    private JobDTO job;
    private DepartmentDTO department;
    private EmployeeDTO manager;

    // ===== HAL LINKS =====
    @JsonProperty("_links")
    private Links links;

    public EmployeeDTO() {}

    // =================================================
    // 🔹 DERIVED METHODS (UI SAFE)
    // =================================================

    public String getFullName() {
        String f = firstName != null ? firstName : "";
        String l = lastName != null ? lastName : "";
        return (f + " " + l).trim();
    }

    public String getInitials() {
        String f = (firstName != null && !firstName.isEmpty()) ? "" + firstName.charAt(0) : "";
        String l = (lastName != null && !lastName.isEmpty()) ? "" + lastName.charAt(0) : "";
        return (f + l).toUpperCase();
    }

    public String getSalaryFormatted() {
        if (salary == null) return "—";
        return "$" + String.format("%,d", salary.longValue());
    }

    /** Prefer nested job → fallback to flat jobTitle */
    public String getJobDisplay() {
        if (job != null && job.getJobTitle() != null) return job.getJobTitle();
        return jobTitle != null ? jobTitle : "—";
    }

    /** Extract numeric ID safely */
    public Long extractId() {
        if (employeeId != null) return employeeId.longValue();

        if (links != null && links.self != null) {
            try {
                String href = links.self.href.replaceAll("\\{.*}", "");
                String[] parts = href.split("/");
                return Long.parseLong(parts[parts.length - 1]);
            } catch (Exception ignored) {}
        }
        return null;
    }

    /** Manager link (used if you later need hierarchy) */
    public String getManagerHref() {
        if (links == null || links.manager == null) return null;
        return links.manager.href != null
                ? links.manager.href.replaceAll("\\{.*}", "")
                : null;
    }

    // =================================================
    // GETTERS / SETTERS
    // =================================================

    public BigDecimal getEmployeeId() { return employeeId; }
    public void setEmployeeId(BigDecimal v) { this.employeeId = v; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { this.firstName = v; }

    public String getLastName() { return lastName; }
    public void setLastName(String v) { this.lastName = v; }

    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String v) { this.phoneNumber = v; }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate v) { this.hireDate = v; }

    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal v) { this.salary = v; }

    public BigDecimal getCommissionPct() { return commissionPct; }
    public void setCommissionPct(BigDecimal v) { this.commissionPct = v; }

    public String getJobId() { return jobId; }
    public void setJobId(String v) { this.jobId = v; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String v) { this.jobTitle = v; }

    public JobDTO getJob() { return job; }
    public void setJob(JobDTO v) { this.job = v; }

    public DepartmentDTO getDepartment() { return department; }
    public void setDepartment(DepartmentDTO v) { this.department = v; }

    public EmployeeDTO getManager() { return manager; }
    public void setManager(EmployeeDTO v) { this.manager = v; }

    public Links getLinks() { return links; }
    public void setLinks(Links v) { this.links = v; }

    // =================================================
    // HAL LINK CLASSES (merged safely)
    // =================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Links {
        private HRef self;
        private HRef manager;

        public HRef getSelf() { return self; }
        public void setSelf(HRef s) { this.self = s; }

        public HRef getManager() { return manager; }
        public void setManager(HRef m) { this.manager = m; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HRef {
        private String href;

        public String getHref() { return href; }
        public void setHref(String h) { this.href = h; }
    }
}