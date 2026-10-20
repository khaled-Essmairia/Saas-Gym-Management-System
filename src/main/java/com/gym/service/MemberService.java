package com.gym.service;

import com.gym.entity.Member;
import com.gym.entity.Rate;
import com.gym.repository.AttendanceRepository;
import com.gym.repository.MemberRepository;
import com.gym.repository.RateRepository;
import com.gym.repository.TodoRepository;
import com.gym.web.form.MemberForm;
import com.gym.web.form.PaymentForm;
import com.gym.web.form.ProgressForm;
import com.gym.web.form.RegistrationForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Member logic from add-member-req.php, edit-member-req.php, userpay.php, userprogress-req.php ... */
@Service
@Transactional
public class MemberService {

    private final MemberRepository members;
    private final RateRepository rates;
    private final TodoRepository todos;
    private final AttendanceRepository attendance;
    private final PasswordEncoder passwordEncoder;
    private final GymClock clock;

    public MemberService(MemberRepository members, RateRepository rates, TodoRepository todos,
                         AttendanceRepository attendance, PasswordEncoder passwordEncoder, GymClock clock) {
        this.members = members;
        this.rates = rates;
        this.todos = todos;
        this.attendance = attendance;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Member get(Integer id) {
        return members.findById(id).orElseThrow(() -> new NotFoundException("Member not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Member> all() {
        return members.findAll();
    }

    @Transactional(readOnly = true)
    public List<Member> active() {
        return members.findByStatusOrderByUserId(Member.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<Member> search(String q) {
        return members.search(q.trim());
    }

    @Transactional(readOnly = true)
    public boolean usernameTaken(String username, Integer exceptId) {
        return exceptId == null ? members.existsByUsername(username)
                : members.existsByUsernameAndUserIdNot(username, exceptId);
    }

    /** Monthly price of a service, from the "rates" table. */
    @Transactional(readOnly = true)
    public int monthlyRate(String service) {
        if (service == null) {
            return 0;
        }
        return rates.findFirstByNameIgnoreCase(service).map(Rate::getChargeValue).orElse(0);
    }

    @Transactional(readOnly = true)
    public List<Rate> rates() {
        return rates.findAll();
    }

    /** Member entry form (admin/staff). */
    public Member create(MemberForm form) {
        Member m = new Member();
        apply(m, form);
        m.setPassword(passwordEncoder.encode(form.getPassword()));
        m.setPaidDate(clock.today());
        m.setPYear(clock.today().getYear());
        m.setStatus(Member.ACTIVE);
        return members.save(m);
    }

    /** Update member details (password is not changed, as in PHP). */
    public Member update(Integer id, MemberForm form) {
        Member m = get(id);
        apply(m, form);
        return members.save(m);
    }

    private void apply(Member m, MemberForm form) {
        m.setFullname(form.getFullname().trim());
        m.setUsername(form.getUsername().trim());
        m.setGender(form.getGender());
        m.setDor(form.getDor());
        m.setServices(form.getServices());
        m.setPlan(String.valueOf(form.getPlan()));
        m.setAmount(form.getAmount() * form.getPlan());
        m.setAddress(form.getAddress().trim());
        m.setContact(form.getContact().trim());
    }

    /** Self registration from the customer login page. Status "Pending" until the first payment. */
    public Member register(RegistrationForm form) {
        Member m = new Member();
        m.setFullname(form.getFullname().trim());
        m.setUsername(form.getUsername().trim());
        m.setPassword(passwordEncoder.encode(form.getPassword()));
        m.setDor(clock.today());
        m.setGender(form.getGender());
        m.setServices(form.getServices());
        m.setAmount(0);
        m.setPlan(String.valueOf(form.getPlan()));
        m.setAddress(form.getAddress().trim());
        m.setContact(form.getContact().trim());
        m.setStatus(Member.PENDING);
        return members.save(m);
    }

    public void delete(Integer id) {
        Member m = get(id);
        todos.deleteAll(todos.findByUserIdOrderById(m.getUserId()));
        attendance.deleteByUserId(String.valueOf(m.getUserId()));
        members.delete(m);
    }

    /**
     * Payment (userpay.php): amount = monthly amount x plan, paid today,
     * reminder cleared. Returns the previous payment date for the receipt.
     */
    public Payment pay(Integer id, PaymentForm form) {
        Member m = get(id);
        var previousPaidDate = m.getPaidDate();
        int total = form.getAmount() * form.getPlan();
        m.setAmount(total);
        m.setPlan(String.valueOf(form.getPlan()));
        m.setStatus(form.getStatus());
        m.setPaidDate(clock.today());
        m.setReminder(0);
        members.save(m);
        return new Payment(m, form.getAmount(), form.getPlan(), total, previousPaidDate, clock.now());
    }

    /** "Alert" button on the payments page (sendReminder.php). */
    public void sendReminder(Integer id) {
        Member m = get(id);
        m.setReminder(1);
        members.save(m);
    }

    /** userprogress-req.php */
    public void updateProgress(Integer id, ProgressForm form) {
        Member m = get(id);
        m.setIniWeight(form.getIniWeight());
        m.setCurrWeight(form.getCurrWeight());
        m.setIniBodytype(form.getIniBodytype() == null ? "" : form.getIniBodytype().trim());
        m.setCurrBodytype(form.getCurrBodytype() == null ? "" : form.getCurrBodytype().trim());
        m.setProgressDate(clock.today());
        members.save(m);
    }

    /** Data shown on the payment receipt. */
    public static class Payment {
        private final Member member;
        private final int monthlyAmount;
        private final int plan;
        private final int total;
        private final java.time.LocalDate previousPaidDate;
        private final java.time.LocalDateTime paidOn;
        private final String invoiceNumber;

        public Payment(Member member, int monthlyAmount, int plan, int total,
                       java.time.LocalDate previousPaidDate, java.time.LocalDateTime paidOn) {
            this.member = member;
            this.monthlyAmount = monthlyAmount;
            this.plan = plan;
            this.total = total;
            this.previousPaidDate = previousPaidDate;
            this.paidOn = paidOn;
            // Same idea as the PHP rand(100000, 10000000) invoice number
            this.invoiceNumber = "GMS_" + java.util.concurrent.ThreadLocalRandom.current().nextInt(100000, 10000000);
        }

        public Member getMember() { return member; }
        public int getMonthlyAmount() { return monthlyAmount; }
        public int getPlan() { return plan; }
        public int getTotal() { return total; }
        public java.time.LocalDate getPreviousPaidDate() { return previousPaidDate; }
        public java.time.LocalDateTime getPaidOn() { return paidOn; }
        public String getInvoiceNumber() { return invoiceNumber; }
    }
}
