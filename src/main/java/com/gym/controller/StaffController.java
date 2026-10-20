package com.gym.controller;

import com.gym.repository.AnnouncementRepository;
import com.gym.repository.TodoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Staff dashboard (staff/staff-pages/index.php). Other staff pages are in ManagementController. */
@Controller
@RequestMapping("/staff")
public class StaffController {

    private final AnnouncementRepository announcements;
    private final TodoRepository todos;

    public StaffController(AnnouncementRepository announcements, TodoRepository todos) {
        this.announcements = announcements;
        this.todos = todos;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("area", "staff");
        model.addAttribute("announcements", announcements.findAll());
        model.addAttribute("todos", todos.findAll());
        model.addAttribute("page", "dashboard");
        return "staff/dashboard";
    }
}
