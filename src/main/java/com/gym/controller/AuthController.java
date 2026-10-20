package com.gym.controller;

import com.gym.service.MemberService;
import com.gym.web.form.RegistrationForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * The three login pages (index.php, staff/index.php, customer/index.php)
 * and the customer "Join Now!" registration (customer/pages/register-cust.php).
 * The login POST and the logout are handled by Spring Security (see SecurityConfig).
 */
@Controller
public class AuthController {

    static final List<Integer> PLANS = List.of(1, 3, 6, 12);

    private final MemberService memberService;

    public AuthController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/admin/login";
    }

    @GetMapping({"/admin", "/admin/"})
    public String adminHome() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping({"/staff", "/staff/"})
    public String staffHome() {
        return "redirect:/staff/dashboard";
    }

    @GetMapping({"/customer", "/customer/"})
    public String customerHome() {
        return "redirect:/customer/dashboard";
    }

    @GetMapping("/admin/login")
    public String adminLogin(Model model) {
        model.addAttribute("area", "admin");
        return "auth/login";
    }

    @GetMapping("/staff/login")
    public String staffLogin(Model model) {
        model.addAttribute("area", "staff");
        return "auth/login";
    }

    @GetMapping("/customer/login")
    public String customerLogin(Model model) {
        model.addAttribute("area", "customer");
        if (!model.containsAttribute("registration")) {
            model.addAttribute("registration", new RegistrationForm());
        }
        model.addAttribute("rates", memberService.rates());
        model.addAttribute("plans", PLANS);
        return "auth/login";
    }

    @PostMapping("/customer/register")
    public String register(@Valid @ModelAttribute("registration") RegistrationForm form, BindingResult result,
                           Model model, RedirectAttributes redirect) {
        if (form.getUsername() != null && memberService.usernameTaken(form.getUsername().trim(), null)) {
            result.rejectValue("username", "taken", "This username is already taken");
        }
        if (form.getPlan() != null && !PLANS.contains(form.getPlan())) {
            result.rejectValue("plan", "invalid", "Please select a plan");
        }
        if (result.hasErrors()) {
            model.addAttribute("area", "customer");
            model.addAttribute("rates", memberService.rates());
            model.addAttribute("plans", PLANS);
            model.addAttribute("showRegistration", true);
            return "auth/login";
        }
        memberService.register(form);
        redirect.addFlashAttribute("success",
                "Your details have been sent. You can log in once the gym activates your account (after your first payment).");
        return "redirect:/customer/login";
    }
}
