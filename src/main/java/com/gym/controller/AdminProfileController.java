package com.gym.controller;

import com.gym.security.GymUser;
import com.gym.service.AdminAccountService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** "System Administration > Admin Profile": the logged-in admin changes his own password. */
@Controller
@RequestMapping("/admin/profile")
public class AdminProfileController {

    private final AdminAccountService accounts;

    public AdminProfileController(AdminAccountService accounts) {
        this.accounts = accounts;
    }

    @ModelAttribute("area")
    public String area() {
        return "admin";
    }

    @GetMapping
    public String page(@AuthenticationPrincipal GymUser user, Model model) {
        return form(user, model);
    }

    @PostMapping
    public String changePassword(@AuthenticationPrincipal GymUser user,
                                 @RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Model model, RedirectAttributes redirect) {
        String problem = accounts.changePassword(user.getId(), oldPassword, newPassword, confirmPassword);
        if (problem != null) {
            model.addAttribute("error", problem);
            return form(user, model); // the typed passwords are not sent back to the page
        }
        redirect.addFlashAttribute("success", "Password changed. Use the new password the next time you log in.");
        return "redirect:/admin/profile";
    }

    private String form(GymUser user, Model model) {
        model.addAttribute("adminUsername", user.getUsername());
        model.addAttribute("adminName", user.getDisplayName());
        model.addAttribute("minLength", AdminAccountService.MIN_LENGTH);
        model.addAttribute("maxLength", AdminAccountService.MAX_LENGTH);
        model.addAttribute("page", "admin-profile");
        return "admin/profile";
    }
}
