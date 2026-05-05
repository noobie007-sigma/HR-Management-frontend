package com.example.HR_Management_Frontend.controller;


import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.client.RestTemplate;

import com.example.HR_Management_Frontend.dto.CountryDTO;
import com.example.HR_Management_Frontend.service.CountryService;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/countries")
public class CountryController {

    @Autowired
    private CountryService service;
    @Autowired
    RestTemplate restTemplate;
    
    @Value("${backend.base-url}")
    private String BASE_URL;

    @GetMapping
    public String listCountries(
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Map body     = service.getCountries(page);
        Map embedded = (Map) body.get("_embedded");
        List<Map> countries = embedded != null
                ? (List<Map>) embedded.get("countries")
                : List.of();

        Map<String, Object> pageInfo = (Map<String, Object>) body.get("page");
        int totalPages = ((Number) pageInfo.get("totalPages")).intValue();

        model.addAttribute("countries",   countries);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages",  totalPages);
        

        return "country/list";
    }
    @GetMapping("/{id}")
    public String countryDetail(@PathVariable String id, Model model) {

        // 1. Fetch country info with region name
        ResponseEntity<Map> countryResp = restTemplate.getForEntity(
            BASE_URL + "/countries/" + id + "?projection=countryWithRegion", Map.class);
        Map<String, Object> country = countryResp.getBody();

        // 2. Fetch employees by country ID
        ResponseEntity<Map> empResp = restTemplate.getForEntity(
            BASE_URL + "/employees/search/findByDepartment_Location_Country_CountryId?countryId=" + id, Map.class);
        Map empBody = empResp.getBody();
        Map embedded = (Map) empBody.get("_embedded");
        List<Map> employees = embedded != null ? (List<Map>) embedded.get("employees") : List.of();

        model.addAttribute("country", country);
        model.addAttribute("employees", employees);
        model.addAttribute("employeeCount", employees.size());

        return "country/detail";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("country", new CountryDTO());
        model.addAttribute("regions", service.getRegions());
        model.addAttribute("isEdit",  false);
        return "country/form";
    }


    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable String id, Model model) {
        model.addAttribute("country", service.getCountryById(id));
        model.addAttribute("regions", service.getRegions());
        model.addAttribute("isEdit",  true);
        return "country/form";
    }

    @PostMapping("/save")
    public String saveCountry(
            @Valid @ModelAttribute("country") CountryDTO country,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("regions", service.getRegions());
            model.addAttribute("isEdit",  country.getCountryId() != null && !country.getCountryId().isBlank());
            return "country/form";
        }

        try {
            service.saveCountry(country);
            redirectAttributes.addFlashAttribute("message",
                    "Country '" + country.getCountryId() + "' saved successfully.");
            return "redirect:/countries";

        } catch (HttpClientErrorException ex) {
            model.addAttribute("apiError", extractMessage(ex));
            model.addAttribute("regions",  service.getRegions());
            model.addAttribute("isEdit",   country.getCountryId() != null && !country.getCountryId().isBlank());
            return "country/form";
        }
    }

    private String extractMessage(HttpClientErrorException ex) {
        try {
            String body = ex.getResponseBodyAsString();
            int idx     = body.indexOf("\"message\"");
            if (idx >= 0) {
                int start = body.indexOf("\"", idx + 9) + 1;
                int end   = body.indexOf("\"", start);
                return body.substring(start, end);
            }
        } catch (Exception ignored) {}
        return "An error occurred: " + ex.getStatusCode();
    }
}