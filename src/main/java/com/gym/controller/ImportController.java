package com.gym.controller;

import com.gym.service.ImportService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * "Import Members (CSV)" and "Import Equipment (CSV)" pages of the admin area: download the
 * template, fill it, upload it. All rules are in ImportService.
 */
@Controller
@RequestMapping("/admin")
public class ImportController {

    private final ImportService imports;

    public ImportController(ImportService imports) {
        this.imports = imports;
    }

    @ModelAttribute("area")
    public String area() {
        return "admin";
    }

    // ------------------------------------------------------------------ members

    @GetMapping("/members/import")
    public String membersPage(Model model) {
        return form(model, true, null);
    }

    @GetMapping("/members/import/template")
    public ResponseEntity<byte[]> membersTemplate() {
        return csv("members-template.csv", imports.memberTemplate());
    }

    @PostMapping("/members/import")
    public String membersUpload(@RequestParam("file") MultipartFile file, Model model, RedirectAttributes redirect) {
        if (file.isEmpty()) {
            model.addAttribute("error", "Please choose a CSV file first.");
            return form(model, true, null);
        }
        ImportService.Result result;
        try (InputStream in = file.getInputStream()) {
            result = imports.importMembers(in);
        } catch (IOException e) {
            model.addAttribute("error", "The file could not be read: " + e.getMessage());
            return form(model, true, null);
        }
        if (result.isOk()) {
            redirect.addFlashAttribute("success", result.getImported() + " member(s) imported.");
            return "redirect:/admin/members";
        }
        return form(model, true, result);
    }

    // ------------------------------------------------------------------ equipment

    @GetMapping("/equipment/import")
    public String equipmentPage(Model model) {
        return form(model, false, null);
    }

    @GetMapping("/equipment/import/template")
    public ResponseEntity<byte[]> equipmentTemplate() {
        return csv("equipment-template.csv", imports.equipmentTemplate());
    }

    @PostMapping("/equipment/import")
    public String equipmentUpload(@RequestParam("file") MultipartFile file, Model model, RedirectAttributes redirect) {
        if (file.isEmpty()) {
            model.addAttribute("error", "Please choose a CSV file first.");
            return form(model, false, null);
        }
        ImportService.Result result;
        try (InputStream in = file.getInputStream()) {
            result = imports.importEquipment(in);
        } catch (IOException e) {
            model.addAttribute("error", "The file could not be read: " + e.getMessage());
            return form(model, false, null);
        }
        if (result.isOk()) {
            redirect.addFlashAttribute("success", result.getImported() + " equipment item(s) imported.");
            return "redirect:/admin/equipment";
        }
        return form(model, false, result);
    }

    // ------------------------------------------------------------------ shared

    private String form(Model model, boolean members, ImportService.Result result) {
        String base = members ? "/admin/members/import" : "/admin/equipment/import";
        model.addAttribute("page", members ? "import-members" : "import-equipment");
        model.addAttribute("title", members ? "Import Members (CSV)" : "Import Equipment (CSV)");
        model.addAttribute("what", members ? "members" : "equipment");
        model.addAttribute("icon", members ? "fas fa-users" : "fas fa-dumbbell");
        model.addAttribute("columns", members ? ImportService.MEMBER_COLUMNS : ImportService.EQUIPMENT_COLUMNS);
        model.addAttribute("services", members ? imports.serviceNames() : null);
        model.addAttribute("maxRows", ImportService.MAX_ROWS);
        model.addAttribute("actionUrl", base);
        model.addAttribute("templateUrl", base + "/template");
        model.addAttribute("result", result);
        return "admin/import";
    }

    private static ResponseEntity<byte[]> csv(String filename, String content) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(content.getBytes(StandardCharsets.UTF_8));
    }
}
