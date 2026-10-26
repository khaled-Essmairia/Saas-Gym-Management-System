package com.gym.controller;

import com.gym.service.LicenseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** "License Management" page of the admin area (the admin login is required, see SecurityConfig). */
@Controller
@RequestMapping("/admin/license")
public class LicenseController {

    private final LicenseService licenses;

    public LicenseController(LicenseService licenses) {
        this.licenses = licenses;
    }

    @ModelAttribute("area")
    public String area() {
        return "admin";
    }

    @GetMapping
    public String page(Model model) {
        model.addAttribute("history", licenses.history());
        model.addAttribute("machineId", licenses.machineId());
        model.addAttribute("page", "license");
        return "admin/license";
    }

    @PostMapping
    public String activate(@RequestParam("key") String key, RedirectAttributes redirect) {
        LicenseService.Status result = licenses.activate(key);
        if (result.isValid()) {
            redirect.addFlashAttribute("success", "License saved. " + result.getMessage());
        } else {
            redirect.addFlashAttribute("error", result.getMessage());
        }
        return "redirect:/admin/license";
    }
}
