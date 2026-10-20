package com.gym.controller;

import com.gym.entity.Todo;
import com.gym.repository.AnnouncementRepository;
import com.gym.repository.TodoRepository;
import com.gym.security.GymUser;
import com.gym.service.MemberService;
import com.gym.service.NotFoundException;
import com.gym.web.form.TodoForm;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** Customer area (customer/pages/*.php). A customer only sees his own data. */
@Controller
@RequestMapping("/customer")
public class CustomerController {

    static final List<String> TASK_STATUSES = List.of("In Progress", "Pending");

    private final MemberService memberService;
    private final TodoRepository todos;
    private final AnnouncementRepository announcements;

    public CustomerController(MemberService memberService, TodoRepository todos,
                              AnnouncementRepository announcements) {
        this.memberService = memberService;
        this.todos = todos;
        this.announcements = announcements;
    }

    @ModelAttribute("area")
    public String area() {
        return "customer";
    }

    @ModelAttribute("statuses")
    public List<String> statuses() {
        return TASK_STATUSES;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal GymUser user, Model model) {
        model.addAttribute("todos", todos.findByUserIdOrderById(user.getId()));
        model.addAttribute("announcements", announcements.findAll());
        model.addAttribute("page", "dashboard");
        return "customer/dashboard";
    }

    // ---------------- To-do (to-do.php, add-to-do.php, update-todo.php, actions/*) ----------------

    @GetMapping("/todo")
    public String todo(@AuthenticationPrincipal GymUser user, Model model) {
        model.addAttribute("form", new TodoForm());
        return todoPage(user, model);
    }

    @PostMapping("/todo")
    public String addTodo(@AuthenticationPrincipal GymUser user, @Valid @ModelAttribute("form") TodoForm form,
                          BindingResult result, Model model, RedirectAttributes redirect) {
        checkStatus(form, result);
        if (result.hasErrors()) {
            return todoPage(user, model);
        }
        Todo t = new Todo();
        t.setUserId(user.getId());
        t.setTaskDesc(form.getTaskDesc().trim());
        t.setTaskStatus(form.getTaskStatus());
        todos.save(t);
        redirect.addFlashAttribute("success", "Your task has been added!");
        return "redirect:/customer/todo";
    }

    @GetMapping("/todo/{id}/edit")
    public String editTodo(@AuthenticationPrincipal GymUser user, @PathVariable Integer id, Model model) {
        Todo t = ownTodo(user, id);
        TodoForm form = new TodoForm();
        form.setTaskDesc(t.getTaskDesc());
        form.setTaskStatus(t.getTaskStatus());
        model.addAttribute("form", form);
        model.addAttribute("todoId", id);
        model.addAttribute("page", "todo");
        return "customer/todo-edit";
    }

    @PostMapping("/todo/{id}/edit")
    public String updateTodo(@AuthenticationPrincipal GymUser user, @PathVariable Integer id,
                             @Valid @ModelAttribute("form") TodoForm form, BindingResult result,
                             Model model, RedirectAttributes redirect) {
        Todo t = ownTodo(user, id);
        checkStatus(form, result);
        if (result.hasErrors()) {
            model.addAttribute("todoId", id);
            model.addAttribute("page", "todo");
            return "customer/todo-edit";
        }
        t.setTaskDesc(form.getTaskDesc().trim());
        t.setTaskStatus(form.getTaskStatus());
        todos.save(t);
        redirect.addFlashAttribute("success", "Task updated.");
        return "redirect:/customer/todo";
    }

    /** The "Done" tick removes the task (actions/remove-todo.php). */
    @PostMapping("/todo/{id}/done")
    public String doneTodo(@AuthenticationPrincipal GymUser user, @PathVariable Integer id,
                           @RequestParam(defaultValue = "todo") String from, RedirectAttributes redirect) {
        todos.delete(ownTodo(user, id));
        redirect.addFlashAttribute("success", "Task completed and removed from your list.");
        return "dashboard".equals(from) ? "redirect:/customer/dashboard" : "redirect:/customer/todo";
    }

    private String todoPage(GymUser user, Model model) {
        model.addAttribute("todos", todos.findByUserIdOrderById(user.getId()));
        model.addAttribute("page", "todo");
        return "customer/todo";
    }

    private Todo ownTodo(GymUser user, Integer id) {
        return todos.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new NotFoundException("Task not found"));
    }

    private void checkStatus(TodoForm form, BindingResult result) {
        if (!TASK_STATUSES.contains(form.getTaskStatus())) {
            result.rejectValue("taskStatus", "invalid", "Please select a status");
        }
    }

    // ---------------- Reminders, announcements, report ----------------

    @GetMapping("/reminders")
    public String reminders(@AuthenticationPrincipal GymUser user, Model model) {
        model.addAttribute("member", memberService.get(user.getId()));
        model.addAttribute("page", "reminder");
        return "customer/reminders";
    }

    @GetMapping("/announcements")
    public String announcements(Model model) {
        model.addAttribute("announcements", announcements.findAll());
        model.addAttribute("page", "announcement");
        return "customer/announcements";
    }

    @GetMapping("/report")
    public String report(@AuthenticationPrincipal GymUser user, Model model) {
        model.addAttribute("member", memberService.get(user.getId()));
        model.addAttribute("page", "report");
        return "customer/report";
    }
}
