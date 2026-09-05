package com.springbootecommerce.shophappens.administration.web.api;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminCsrfController {

    @GetMapping("/csrf")
    public CsrfTokenResponse csrf(HttpServletRequest request) {
        Object token = request.getAttribute("_csrf");
        if (token == null) {
            token = request.getAttribute("org.springframework.security.web.csrf.CsrfToken");
        }
        String headerName = invokeIfPresent(token, "getHeaderName");
        if (headerName == null) {
            headerName = "X-CSRF-TOKEN";
        }
        String value = invokeIfPresent(token, "getToken");
        return new CsrfTokenResponse(headerName, value);
    }

    private String invokeIfPresent(Object value, String methodName) {
        if (value == null) {
            return null;
        }
        try {
            Method method = value.getClass().getMethod(methodName);
            if (!method.trySetAccessible()) {
                return null;
            }
            Object result = method.invoke(value);
            return result == null ? null : result.toString();
        } catch (ReflectiveOperationException ex) {
            return null;
        }
    }
}
