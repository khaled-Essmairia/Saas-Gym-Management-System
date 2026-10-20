package com.gym.web;

import com.gym.config.GymProperties;
import com.gym.service.GymClock;
import com.gym.service.LicenseService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Values available in every template. */
@ControllerAdvice
public class GlobalModelAdvice {

    private final GymProperties gym;
    private final GymClock clock;
    private final LicenseService licenses;

    public GlobalModelAdvice(GymProperties gym, GymClock clock, LicenseService licenses) {
        this.gym = gym;
        this.clock = clock;
        this.licenses = licenses;
    }

    @ModelAttribute("gym")
    public GymProperties gym() {
        return gym;
    }

    @ModelAttribute("currentYear")
    public int currentYear() {
        return clock.today().getYear();
    }

    /** License state, read from the database on every page. The sidebar uses ${license.valid}. */
    @ModelAttribute("license")
    public LicenseService.Status license() {
        return licenses.status();
    }
}
