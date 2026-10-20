package com.gym.service;

import com.gym.entity.Staff;
import com.gym.repository.StaffRepository;
import com.gym.web.form.StaffForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** added-staffs.php, edit-staff-req.php, remove-staff.php */
@Service
@Transactional
public class StaffService {

    private final StaffRepository staffs;
    private final PasswordEncoder passwordEncoder;

    public StaffService(StaffRepository staffs, PasswordEncoder passwordEncoder) {
        this.staffs = staffs;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Staff> all() {
        return staffs.findAll();
    }

    @Transactional(readOnly = true)
    public Staff get(Integer id) {
        return staffs.findById(id).orElseThrow(() -> new NotFoundException("Staff not found: " + id));
    }

    @Transactional(readOnly = true)
    public boolean usernameTaken(String username, Integer exceptId) {
        return exceptId == null ? staffs.existsByUsername(username)
                : staffs.existsByUsernameAndUserIdNot(username, exceptId);
    }

    public Staff create(StaffForm form) {
        Staff s = new Staff();
        apply(s, form);
        s.setPassword(passwordEncoder.encode(form.getPassword()));
        return staffs.save(s);
    }

    public Staff update(Integer id, StaffForm form) {
        Staff s = get(id);
        apply(s, form);
        return staffs.save(s);
    }

    public void delete(Integer id) {
        staffs.delete(get(id));
    }

    public StaffForm toForm(Staff s) {
        StaffForm f = new StaffForm();
        f.setFullname(s.getFullname());
        f.setUsername(s.getUsername());
        f.setEmail(s.getEmail());
        f.setAddress(s.getAddress());
        f.setDesignation(s.getDesignation());
        f.setGender(s.getGender());
        f.setContact(s.getContact());
        return f;
    }

    private void apply(Staff s, StaffForm f) {
        s.setFullname(f.getFullname().trim());
        s.setUsername(f.getUsername().trim());
        s.setEmail(f.getEmail().trim());
        s.setAddress(f.getAddress().trim());
        s.setDesignation(f.getDesignation());
        s.setGender(f.getGender());
        s.setContact(f.getContact().trim());
    }
}
