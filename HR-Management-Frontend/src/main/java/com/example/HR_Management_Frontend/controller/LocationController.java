package com.example.HR_Management_Frontend.controller;

import com.example.HR_Management_Frontend.dto.DepartmentDTO;
import com.example.HR_Management_Frontend.dto.LocationDTO;
import com.example.HR_Management_Frontend.service.LocationService;
import com.example.HR_Management_Frontend.service.LocationService.LocationPageResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    // ============================================================
    // PAGE 1 — Office Locations table with search + pagination
    // URL: /locations?search=&page=0&size=10
    // ============================================================
    @GetMapping
    public String listLocations(
            @RequestParam(defaultValue = "")  String search,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        List<LocationDTO> locations;
        long totalElements;
        int totalPages;
        boolean isSearch = search != null && !search.isBlank();

        if (isSearch) {
            // Search by city first, fall back to state if empty
            locations = locationService.searchByCity(search.trim());
            if (locations.isEmpty()) {
                locations = locationService.searchByState(search.trim());
            }
            totalElements = locations.size();
            totalPages = 1;
        } else {
            LocationPageResult result = locationService.getAllLocations(page, size);
            locations   = result.locations();
            totalElements = result.totalElements();
            totalPages  = result.totalPages();
        }

        model.addAttribute("locations",     locations);
        model.addAttribute("search",        search);
        model.addAttribute("currentPage",   page);
        model.addAttribute("totalPages",    totalPages);
        model.addAttribute("totalElements", totalElements);
        model.addAttribute("pageSize",      size);
        model.addAttribute("isSearch",      isSearch);

        return "locations";
    }

    // ============================================================
    // PAGE 2 — Location detail: departments + headcount
    // URL: /locations/{id}
    // ============================================================
    @GetMapping("/{id}")
    public String locationDetail(@PathVariable Long id, Model model) {

        Optional<LocationDTO> opt = locationService.getLocationById(id);
        if (opt.isEmpty()) return "redirect:/locations";

        LocationDTO location = opt.get();

        List<DepartmentDTO> departments = locationService.getDepartmentsForLocation(id);
        if (departments == null) departments = new ArrayList<>();

        long totalEmployees = locationService.getEmployeeCount(id);

        model.addAttribute("location",         location);
        model.addAttribute("departments",      departments);
        model.addAttribute("totalEmployees",   totalEmployees);
        model.addAttribute("totalDepartments", departments.size());

        return "location-detail";
    }
}