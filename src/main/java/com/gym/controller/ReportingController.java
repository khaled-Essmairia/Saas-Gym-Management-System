package com.gym.controller;

import com.gym.service.ExportService;
import com.gym.service.GymClock;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.sql.SQLException;

/**
 * "Reporting" page of the admin area: export the members / staff data and every table of the
 * database as CSV (admin login required, see SecurityConfig).
 */
@Controller
@RequestMapping("/admin/reporting")
public class ReportingController {

    private final ExportService exports;
    private final GymClock clock;

    public ReportingController(ExportService exports, GymClock clock) {
        this.exports = exports;
        this.clock = clock;
    }

    @ModelAttribute("area")
    public String area() {
        return "admin";
    }

    @GetMapping
    public String page(Model model) throws SQLException {
        model.addAttribute("tables", exports.tables());
        model.addAttribute("page", "reporting");
        return "admin/reporting";
    }

    @GetMapping("/table/{name}")
    public void table(@PathVariable String name, HttpServletResponse response) throws IOException, SQLException {
        if (!exports.exists(name)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        download(response, "text/csv; charset=UTF-8", name + "-" + clock.today() + ".csv");
        exports.exportTable(name, response.getOutputStream());
    }

    @GetMapping("/all")
    public void all(HttpServletResponse response) throws IOException, SQLException {
        download(response, "application/zip", "gym-database-" + clock.today() + ".zip");
        exports.exportAll(response.getOutputStream());
    }

    private static void download(HttpServletResponse response, String contentType, String filename) {
        response.setContentType(contentType);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString());
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
    }
}
