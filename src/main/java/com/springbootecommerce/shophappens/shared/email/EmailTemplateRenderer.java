package com.springbootecommerce.shophappens.shared.email;

import java.util.Locale;
import java.util.Map;

@FunctionalInterface
public interface EmailTemplateRenderer {
    String render(String template, Locale locale, Map<String, Object> variables);
}
