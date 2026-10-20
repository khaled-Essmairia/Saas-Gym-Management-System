package com.gym.controller;

import com.gym.entity.Member;
import com.gym.service.AttendanceService;
import com.gym.service.EquipmentService;
import com.gym.service.GymClock;
import com.gym.service.MemberService;
import com.gym.web.form.EquipmentForm;
import com.gym.web.form.MemberForm;
import com.gym.web.form.PaymentForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Pages shared by the admin and the staff areas (the PHP project had two
 * copies of these files: admin/*.php and staff/staff-pages/*.php).
 * {area} is "admin" or "staff"; access is restricted by SecurityConfig.
 */
@Controller
@RequestMapping("/{area:admin|staff}")
public class ManagementController {

    static final List<Integer> PLANS = List.of(1, 3, 6, 12);
    static final List<Integer> PAYMENT_PLANS = List.of(1, 3, 6, 12, 0);

    private final MemberService memberService;
    private final EquipmentService equipmentService;
    private final AttendanceService attendanceService;
    private final GymClock clock;

    public ManagementController(MemberService memberService, EquipmentService equipmentService,
                                AttendanceService attendanceService, GymClock clock) {
        this.memberService = memberService;
        this.equipmentService = equipmentService;
        this.attendanceService = attendanceService;
        this.clock = clock;
    }

    @ModelAttribute("area")
    public String area(@PathVariable String area) {
        return area;
    }

    // ------------------------------------------------------------------
    // Members: members.php, remove-member.php, edit-member.php
    // ------------------------------------------------------------------

    @GetMapping("/members")
    public String members(Model model) {
        return memberList(model, "list", "members");
    }

    @GetMapping("/members/remove")
    public String membersRemove(Model model) {
        return memberList(model, "remove", "members-remove");
    }

    @GetMapping("/members/edit")
    public String membersEdit(Model model) {
        return memberList(model, "edit", "members-update");
    }

    private String memberList(Model model, String mode, String page) {
        model.addAttribute("members", memberService.all());
        model.addAttribute("mode", mode);
        model.addAttribute("page", page);
        return "members/list";
    }

    @GetMapping("/members/new")
    public String newMember(Model model) {
        MemberForm form = new MemberForm();
        form.setDor(clock.today());
        model.addAttribute("form", form);
        return memberForm(model, null);
    }

    @PostMapping("/members/new")
    public String createMember(@PathVariable String area, @Valid @ModelAttribute("form") MemberForm form,
                               BindingResult result, Model model, RedirectAttributes redirect) {
        if (!StringUtils.hasText(form.getPassword())) {
            result.rejectValue("password", "required", "Password is required");
        }
        validateMember(form, null, result);
        if (result.hasErrors()) {
            return memberForm(model, null);
        }
        memberService.create(form);
        redirect.addFlashAttribute("success", "Member details have been added!");
        return "redirect:/" + area + "/members";
    }

    @GetMapping("/members/{id}/edit")
    public String editMember(@PathVariable Integer id, Model model) {
        Member m = memberService.get(id);
        MemberForm form = new MemberForm();
        form.setFullname(m.getFullname());
        form.setUsername(m.getUsername());
        form.setGender(m.getGender());
        form.setDor(m.getDor());
        form.setServices(m.getServices());
        int months = m.getPlanMonths();
        form.setPlan(PLANS.contains(months) ? months : 1);
        form.setAmount(months > 0 ? m.getAmount() / months : memberService.monthlyRate(m.getServices()));
        form.setAddress(m.getAddress());
        form.setContact(m.getContact());
        model.addAttribute("form", form);
        return memberForm(model, m);
    }

    @PostMapping("/members/{id}/edit")
    public String updateMember(@PathVariable String area, @PathVariable Integer id,
                               @Valid @ModelAttribute("form") MemberForm form, BindingResult result,
                               Model model, RedirectAttributes redirect) {
        validateMember(form, id, result);
        if (result.hasErrors()) {
            return memberForm(model, memberService.get(id));
        }
        memberService.update(id, form);
        redirect.addFlashAttribute("success", "Member details have been updated!");
        return "redirect:/" + area + "/members/edit";
    }

    @PostMapping("/members/{id}/delete")
    public String deleteMember(@PathVariable String area, @PathVariable Integer id, RedirectAttributes redirect) {
        memberService.delete(id);
        redirect.addFlashAttribute("success", "Member removed.");
        return "redirect:/" + area + "/members/remove";
    }

    private void validateMember(MemberForm form, Integer id, BindingResult result) {
        if (StringUtils.hasText(form.getUsername()) && memberService.usernameTaken(form.getUsername().trim(), id)) {
            result.rejectValue("username", "taken", "This username is already taken");
        }
        if (form.getPlan() != null && !PLANS.contains(form.getPlan())) {
            result.rejectValue("plan", "invalid", "Please choose a plan");
        }
    }

    private String memberForm(Model model, Member member) {
        model.addAttribute("member", member);
        model.addAttribute("rates", memberService.rates());
        model.addAttribute("plans", PLANS);
        model.addAttribute("page", member == null ? "members-entry" : "members-update");
        return "members/form";
    }

    // ------------------------------------------------------------------
    // Equipment: equipment.php, equipment-entry.php, remove-equipment.php, edit-equipment.php
    // ------------------------------------------------------------------

    @GetMapping("/equipment")
    public String equipment(Model model) {
        return equipmentList(model, "list", "list-equip");
    }

    @GetMapping("/equipment/remove")
    public String equipmentRemove(Model model) {
        return equipmentList(model, "remove", "remove-equip");
    }

    @GetMapping("/equipment/edit")
    public String equipmentEdit(Model model) {
        return equipmentList(model, "edit", "update-equip");
    }

    private String equipmentList(Model model, String mode, String page) {
        model.addAttribute("equipment", equipmentService.all());
        model.addAttribute("mode", mode);
        model.addAttribute("page", page);
        return "equipment/list";
    }

    @GetMapping("/equipment/new")
    public String newEquipment(Model model) {
        model.addAttribute("form", new EquipmentForm());
        return equipmentForm(model, null);
    }

    @PostMapping("/equipment/new")
    public String createEquipment(@PathVariable String area, @Valid @ModelAttribute("form") EquipmentForm form,
                                  BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return equipmentForm(model, null);
        }
        equipmentService.create(form);
        redirect.addFlashAttribute("success", "Equipment details have been added!");
        return "redirect:/" + area + "/equipment";
    }

    @GetMapping("/equipment/{id}/edit")
    public String editEquipment(@PathVariable Integer id, Model model) {
        model.addAttribute("form", equipmentService.toForm(equipmentService.get(id)));
        return equipmentForm(model, id);
    }

    @PostMapping("/equipment/{id}/edit")
    public String updateEquipment(@PathVariable String area, @PathVariable Integer id,
                                  @Valid @ModelAttribute("form") EquipmentForm form, BindingResult result,
                                  Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return equipmentForm(model, id);
        }
        equipmentService.update(id, form);
        redirect.addFlashAttribute("success", "Equipment details have been updated!");
        return "redirect:/" + area + "/equipment/edit";
    }

    @PostMapping("/equipment/{id}/delete")
    public String deleteEquipment(@PathVariable String area, @PathVariable Integer id, RedirectAttributes redirect) {
        equipmentService.delete(id);
        redirect.addFlashAttribute("success", "Equipment removed.");
        return "redirect:/" + area + "/equipment/remove";
    }

    private String equipmentForm(Model model, Integer id) {
        model.addAttribute("equipmentId", id);
        model.addAttribute("page", id == null ? "add-equip" : "update-equip");
        return "equipment/form";
    }

    // ------------------------------------------------------------------
    // Attendance: attendance.php + actions/check-attendance.php, delete-attendance.php
    // ------------------------------------------------------------------

    @GetMapping("/attendance")
    public String attendance(Model model) {
        model.addAttribute("members", memberService.active());
        model.addAttribute("today", attendanceService.todayByMember());
        model.addAttribute("page", "attendance");
        return "attendance/checkin";
    }

    @PostMapping("/attendance/{id}/check-in")
    public String checkIn(@PathVariable String area, @PathVariable Integer id, RedirectAttributes redirect) {
        if (!attendanceService.checkIn(id)) {
            redirect.addFlashAttribute("error", "Already checked in today.");
        }
        return "redirect:/" + area + "/attendance";
    }

    @PostMapping("/attendance/{id}/check-out")
    public String checkOut(@PathVariable String area, @PathVariable Integer id) {
        attendanceService.checkOut(id);
        return "redirect:/" + area + "/attendance";
    }

    // ------------------------------------------------------------------
    // Member status: member-status.php
    // ------------------------------------------------------------------

    @GetMapping("/member-status")
    public String memberStatus(Model model) {
        model.addAttribute("members", memberService.all());
        model.addAttribute("page", "member-status");
        return "members/status";
    }

    // ------------------------------------------------------------------
    // Payments: payment.php, search-result.php, user-payment.php, userpay.php, sendReminder.php
    // ------------------------------------------------------------------

    @GetMapping("/payments")
    public String payments(@RequestParam(required = false) String search, Model model) {
        boolean searching = StringUtils.hasText(search);
        model.addAttribute("members", searching ? memberService.search(search) : memberService.all());
        model.addAttribute("search", search);
        model.addAttribute("searching", searching);
        model.addAttribute("page", "payment");
        return "payments/list";
    }

    @GetMapping("/payments/{id}")
    public String paymentForm(@PathVariable Integer id, Model model) {
        Member m = memberService.get(id);
        PaymentForm form = new PaymentForm();
        form.setAmount(memberService.monthlyRate(m.getServices()));
        model.addAttribute("form", form);
        return paymentForm(model, m);
    }

    @PostMapping("/payments/{id}")
    public String pay(@PathVariable Integer id, @Valid @ModelAttribute("form") PaymentForm form,
                      BindingResult result, Model model) {
        if (form.getPlan() != null && !PAYMENT_PLANS.contains(form.getPlan())) {
            result.rejectValue("plan", "invalid", "Please choose a plan");
        }
        if (!List.of(Member.ACTIVE, Member.EXPIRED).contains(form.getStatus())) {
            result.rejectValue("status", "invalid", "Please choose a status");
        }
        if (result.hasErrors()) {
            return paymentForm(model, memberService.get(id));
        }
        model.addAttribute("payment", memberService.pay(id, form));
        model.addAttribute("page", "payment");
        return Member.ACTIVE.equals(form.getStatus()) ? "payments/receipt" : "payments/deactivated";
    }

    @PostMapping("/payments/{id}/remind")
    public String remind(@PathVariable String area, @PathVariable Integer id, RedirectAttributes redirect) {
        memberService.sendReminder(id);
        redirect.addFlashAttribute("success", "Notification sent to selected customer!");
        return "redirect:/" + area + "/payments";
    }

    private String paymentForm(Model model, Member member) {
        model.addAttribute("member", member);
        model.addAttribute("plans", PAYMENT_PLANS);
        model.addAttribute("page", "payment");
        return "payments/form";
    }
}
