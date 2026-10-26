package com.gym.service;

import com.gym.entity.Rate;
import com.gym.repository.MemberRepository;
import com.gym.repository.RateRepository;
import com.gym.web.form.ServiceForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Add / update / delete the services of the gym (Fitness, Sauna, Cardio ...) and their monthly price.
 *
 * Members keep the NAME of their service, so renaming a service renames it for its members too,
 * and a service that still has members cannot be deleted. Changing a price only affects members
 * registered or renewed afterwards: the amount already paid by a member is never changed.
 */
@Service
@Transactional
public class ServiceCatalog {

    private final RateRepository rates;
    private final MemberRepository members;

    public ServiceCatalog(RateRepository rates, MemberRepository members) {
        this.rates = rates;
        this.members = members;
    }

    /** One line of the services table. */
    public static final class Row {
        private final Rate rate;
        private final long members;

        Row(Rate rate, long members) {
            this.rate = rate;
            this.members = members;
        }

        public Integer getId() { return rate.getId(); }
        public String getName() { return rate.getName(); }
        public int getCharge() { return rate.getChargeValue(); }
        public long getMembers() { return members; }
    }

    @Transactional(readOnly = true)
    public List<Row> list() {
        List<Row> rows = new ArrayList<>();
        for (Rate rate : rates.findAll()) {
            rows.add(new Row(rate, members.countByServicesIgnoreCase(rate.getName())));
        }
        rows.sort(Comparator.comparing(r -> r.getName().toLowerCase()));
        return rows;
    }

    @Transactional(readOnly = true)
    public Rate get(Integer id) {
        return rates.findById(id).orElseThrow(() -> new NotFoundException("Service not found: " + id));
    }

    @Transactional(readOnly = true)
    public ServiceForm toForm(Rate rate) {
        ServiceForm form = new ServiceForm();
        form.setName(rate.getName());
        form.setCharge(rate.getChargeValue());
        return form;
    }

    /** @return null when saved, otherwise the message to show */
    public String create(ServiceForm form) {
        String name = form.getName().trim();
        if (rates.findFirstByNameIgnoreCase(name).isPresent()) {
            return "A service named '" + name + "' already exists.";
        }
        Rate rate = new Rate();
        rate.setName(name);
        rate.setCharge(String.valueOf(form.getCharge()));
        rates.save(rate);
        return null;
    }

    /** @return null when saved, otherwise the message to show */
    public String update(Integer id, ServiceForm form) {
        Rate rate = get(id);
        String name = form.getName().trim();
        if (rates.findFirstByNameIgnoreCase(name).filter(other -> !other.getId().equals(id)).isPresent()) {
            return "Another service is already named '" + name + "'.";
        }
        String oldName = rate.getName();
        if (!oldName.equals(name)) {
            members.renameService(oldName, name);
        }
        rate.setName(name);
        rate.setCharge(String.valueOf(form.getCharge()));
        rates.save(rate);
        return null;
    }

    /** @return null when deleted, otherwise the message to show */
    public String delete(Integer id) {
        Rate rate = get(id);
        long used = members.countByServicesIgnoreCase(rate.getName());
        if (used > 0) {
            return "'" + rate.getName() + "' cannot be removed: " + used
                    + " member(s) use it. Change their service first.";
        }
        rates.delete(rate);
        return null;
    }
}
