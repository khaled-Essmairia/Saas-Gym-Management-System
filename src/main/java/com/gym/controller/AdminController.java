package com.gym.controller;

import com.gym.entity.Announcement;
import com.gym.repository.AnnouncementRepository;
import com.gym.repository.TodoRepository;
import com.gym.service.GymClock;
import com.gym.service.MemberService;
import com.gym.service.StaffService;
import com.gym.web.form.AnnouncementForm;
import com.gym.web.form.ProgressForm;
import com.gym.web.form.StaffForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** Admin-only pages (admin/*.php that the staff area does not have). */
@Controller
@RequestMapping("/admin")
public class AdminController {

    static final List<String> DESIGNATIONS =
            List.of("Cashier", "Trainer", "GYM Assistant", "Front Desk Staff", "Manager");

    private final MemberService memberService;
    private final StaffService staffService;
    private final AnnouncementRepository announcements;
    private final TodoRepository todos;
    private final GymClock clock;

    public AdminController(MemberService memberService, StaffService staffService,
                           AnnouncementRepository announcements, TodoRepository todos, GymClock clock) {
        this.memberService = memberService;
        this.staffService = staffService;
        this.announcements = announcements;
        this.todos = todos;
        this.clock = clock;
    }

    @ModelAttribute("area")
    public String area() {
        return "admin";
    }

    // ---------------- Dashboard (admin/index.php) ----------------

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("announcements", announcements.findAll());
        model.addAttribute("todos", todos.findAll());
        model.addAttribute("page", "dashboard");
        return "admin/dashboard";
    }

    // ---------------- Attendance view (view-attendance.php) ----------------

    @GetMapping("/attendance/view")
    public String attendanceView(Model model) {
        model.addAttribute("members", memberService.active());
        model.addAttribute("page", "view-attendance");
        return "admin/attendance-view";
    }

    // ---------------- Customer progress (customer-progress.php, update-progress.php) ----------------

    @GetMapping("/progress")
    public String progress(@RequestParam(required = false) String search, Model model) {
        boolean searching = StringUtils.hasText(search);
        model.addAttribute("members", searching ? memberService.search(search) : memberService.all());
        model.addAttribute("search", search);
        model.addAttribute("searching", searching);
        model.addAttribute("page", "manage-customer-progress");
        return "admin/progress-list";
    }

    @GetMapping("/progress/{id}")
    public String progressForm(@PathVariable Integer id, Model model) {
        var m = memberService.get(id);
        ProgressForm form = new ProgressForm();
        form.setIniWeight(m.getIniWeight());
        form.setCurrWeight(m.getCurrWeight());
        form.setIniBodytype(m.getIniBodytype());
        form.setCurrBodytype(m.getCurrBodytype());
        model.addAttribute("form", form);
        model.addAttribute("member", m);
        model.addAttribute("page", "manage-customer-progress");
        return "admin/progress-form";
    }

    @PostMapping("/progress/{id}")
    public String updateProgress(@PathVariable Integer id, @Valid @ModelAttribute("form") ProgressForm form,
                                 BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("member", memberService.get(id));
            model.addAttribute("page", "manage-customer-progress");
            return "admin/progress-form";
        }
        memberService.updateProgress(id, form);
        redirect.addFlashAttribute("success", "Customer progress has been updated!");
        return "redirect:/admin/progress";
    }

    // ---------------- Announcements (announcement.php, manage-announcement.php) ----------------

    @GetMapping("/announcements")
    public String announcementForm(Model model) {
        AnnouncementForm form = new AnnouncementForm();
        form.setDate(clock.today());
        model.addAttribute("form", form);
        model.addAttribute("page", "announcement");
        return "admin/announcement-form";
    }

    @PostMapping("/announcements")
    public String postAnnouncement(@Valid @ModelAttribute("form") AnnouncementForm form, BindingResult result,
                                   Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("page", "announcement");
            return "admin/announcement-form";
        }
        Announcement a = new Announcement();
        a.setMessage(form.getMessage().trim());
        a.setDate(form.getDate());
        announcements.save(a);
        redirect.addFlashAttribute("success", "Announcement published.");
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/announcements/manage")
    public String manageAnnouncements(Model model) {
        model.addAttribute("announcements", announcements.findAll());
        model.addAttribute("page", "announcement");
        return "admin/announcement-manage";
    }

    @PostMapping("/announcements/{id}/delete")
    public String deleteAnnouncement(@PathVariable Integer id, RedirectAttributes redirect) {
        announcements.deleteById(id);
        redirect.addFlashAttribute("success", "Announcement removed.");
        return "redirect:/admin/announcements/manage";
    }

    // ---------------- Staff management (staffs.php, staffs-entry.php, edit-staff-form.php) ----------------

    @GetMapping("/staff")
    public String staffList(Model model) {
        model.addAttribute("staffs", staffService.all());
        model.addAttribute("page", "staff-management");
        return "admin/staff-list";
    }

    @GetMapping("/staff/new")
    public String newStaff(Model model) {
        model.addAttribute("form", new StaffForm());
        return staffForm(model, null);
    }

    @PostMapping("/staff/new")
    public String createStaff(@Valid @ModelAttribute("form") StaffForm form, BindingResult result,
                              Model model, RedirectAttributes redirect) {
        if (!StringUtils.hasText(form.getPassword())) {
            result.rejectValue("password", "required", "Password is required");
        } else if (!form.getPassword().equals(form.getPassword2())) {
            result.rejectValue("password2", "mismatch", "Passwords do not match");
        }
        validateStaff(form, null, result);
        if (result.hasErrors()) {
            return staffForm(model, null);
        }
        staffService.create(form);
        redirect.addFlashAttribute("success", "Staff member has been added!");
        return "redirect:/admin/staff";
    }

    @GetMapping("/staff/{id}/edit")
    public String editStaff(@PathVariable Integer id, Model model) {
        model.addAttribute("form", staffService.toForm(staffService.get(id)));
        return staffForm(model, id);
    }

    @PostMapping("/staff/{id}/edit")
    public String updateStaff(@PathVariable Integer id, @Valid @ModelAttribute("form") StaffForm form,
                              BindingResult result, Model model, RedirectAttributes redirect) {
        validateStaff(form, id, result);
        if (result.hasErrors()) {
            return staffForm(model, id);
        }
        staffService.update(id, form);
        redirect.addFlashAttribute("success", "Staff details have been updated!");
        return "redirect:/admin/staff";
    }

    @PostMapping("/staff/{id}/delete")
    public String deleteStaff(@PathVariable Integer id, RedirectAttributes redirect) {
        staffService.delete(id);
        redirect.addFlashAttribute("success", "Staff member removed.");
        return "redirect:/admin/staff";
    }

    private void validateStaff(StaffForm form, Integer id, BindingResult result) {
        if (StringUtils.hasText(form.getUsername()) && staffService.usernameTaken(form.getUsername().trim(), id)) {
            result.rejectValue("username", "taken", "This username is already taken");
        }
        if (!DESIGNATIONS.contains(form.getDesignation())) {
            result.rejectValue("designation", "invalid", "Please choose a designation");
        }
    }

    private String staffForm(Model model, Integer id) {
        model.addAttribute("staffId", id);
        model.addAttribute("designations", DESIGNATIONS);
        model.addAttribute("page", "staff-management");
        return "admin/staff-form";
    }

    // ---------------- Reports (reports.php, members-report.php, progress-report.php ...) ----------------

    @GetMapping("/reports/charts")
    public String charts(Model model) {
        model.addAttribute("page", "chart");
        return "admin/reports-charts";
    }

    @GetMapping("/reports/members")
    public String membersReport(Model model) {
        model.addAttribute("members", memberService.all());
        model.addAttribute("page", "member-repo");
        return "admin/reports-members";
    }

    @GetMapping("/reports/members/{id}")
    public String memberReport(@PathVariable Integer id, Model model) {
        model.addAttribute("member", memberService.get(id));
        model.addAttribute("page", "member-repo");
        return "admin/report-member";
    }

    @GetMapping("/reports/progress")
    public String progressReports(Model model) {
        model.addAttribute("members", memberService.all());
        model.addAttribute("page", "c-p-r");
        return "admin/reports-progress";
    }

    @GetMapping("/reports/progress/{id}")
    public String progressReport(@PathVariable Integer id, Model model) {
        model.addAttribute("member", memberService.get(id));
        model.addAttribute("page", "c-p-r");
        return "admin/report-progress";
    }
}
