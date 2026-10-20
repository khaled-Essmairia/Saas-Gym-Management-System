package com.gym.service;

import com.gym.entity.Equipment;
import com.gym.repository.EquipmentRepository;
import com.gym.web.form.EquipmentForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** add-equipment-req.php, edit-equipment-req.php, actions/delete-equipment.php */
@Service
@Transactional
public class EquipmentService {

    private final EquipmentRepository equipment;

    public EquipmentService(EquipmentRepository equipment) {
        this.equipment = equipment;
    }

    @Transactional(readOnly = true)
    public List<Equipment> all() {
        return equipment.findAll();
    }

    @Transactional(readOnly = true)
    public Equipment get(Integer id) {
        return equipment.findById(id).orElseThrow(() -> new NotFoundException("Equipment not found: " + id));
    }

    public Equipment create(EquipmentForm form) {
        Equipment e = new Equipment();
        apply(e, form);
        return equipment.save(e);
    }

    public Equipment update(Integer id, EquipmentForm form) {
        Equipment e = get(id);
        apply(e, form);
        return equipment.save(e);
    }

    public void delete(Integer id) {
        equipment.delete(get(id));
    }

    public EquipmentForm toForm(Equipment e) {
        EquipmentForm f = new EquipmentForm();
        f.setName(e.getName());
        f.setDescription(e.getDescription());
        f.setDate(e.getDate());
        f.setQuantity(e.getQuantity());
        f.setVendor(e.getVendor());
        f.setAddress(e.getAddress());
        f.setContact(e.getContact());
        f.setAmount(e.getUnitCost());
        return f;
    }

    private void apply(Equipment e, EquipmentForm f) {
        e.setName(f.getName().trim());
        e.setDescription(f.getDescription().trim());
        e.setDate(f.getDate());
        e.setQuantity(f.getQuantity());
        e.setVendor(f.getVendor().trim());
        e.setAddress(f.getAddress().trim());
        e.setContact(f.getContact().trim());
        e.setAmount(f.getAmount() * f.getQuantity()); // total cost, as in PHP
    }
}
