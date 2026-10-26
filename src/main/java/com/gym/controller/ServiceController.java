package com.gym.controller;

import com.gym.service.ServiceCatalog;
import com.gym.web.form.ServiceForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** "Manage Services" (admin area): add, update and remove the services members can choose, with their monthly price. */
@Controller
@RequestMapping("/admin/services")
public class ServiceController {

    private final ServiceCatalog catalog;

    public ServiceController(ServiceCatalog catalog) {
        this.catalog = catalog;
    }

    @ModelAttribute("area")
    public String area() {
        return "admin";
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("services", catalog.list());
        model.addAttribute("page", "services");
        return "admin/services";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new ServiceForm());
        return form(model, null);
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("form") ServiceForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return form(model, null);
        }
        String problem = catalog.create(form);
        if (problem != null) {
            model.addAttribute("error", problem);
            return form(model, null);
        }
        redirect.addFlashAttribute("success", "Service added.");
        return "redirect:/admin/services";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("form", catalog.toForm(catalog.get(id)));
        return form(model, id);
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Integer id, @Valid @ModelAttribute("form") ServiceForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return form(model, id);
        }
        String problem = catalog.update(id, form);
        if (problem != null) {
            model.addAttribute("error", problem);
            return form(model, id);
        }
        redirect.addFlashAttribute("success", "Service updated.");
        return "redirect:/admin/services";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
        String problem = catalog.delete(id);
        if (problem != null) {
            redirect.addFlashAttribute("error", problem);
        } else {
            redirect.addFlashAttribute("success", "Service removed.");
        }
        return "redirect:/admin/services";
    }

    private String form(Model model, Integer serviceId) {
        model.addAttribute("serviceId", serviceId);
        model.addAttribute("page", "services");
        return "admin/service-form";
    }
}
