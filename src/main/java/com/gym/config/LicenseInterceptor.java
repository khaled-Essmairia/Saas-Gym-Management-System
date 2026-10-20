package com.gym.config;

import com.gym.service.LicenseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * Optional hard lock. With gym.license.enforce=true every admin/staff/customer page
 * redirects to the License Management page until a valid license is stored
 * (hiding the sidebar alone does not stop someone typing a URL). Off by default.
 */
@Component
public class LicenseInterceptor implements HandlerInterceptor {

    private final LicenseService licenses;
    private final boolean enforce;

    public LicenseInterceptor(LicenseService licenses, @Value("${gym.license.enforce:false}") boolean enforce) {
        this.licenses = licenses;
        this.enforce = enforce;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!enforce || licenses.status().isValid()) {
            return true;
        }
        response.sendRedirect(request.getContextPath() + "/admin/license");
        return false;
    }
}
