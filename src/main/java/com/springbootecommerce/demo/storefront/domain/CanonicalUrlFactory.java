package com.springbootecommerce.demo.storefront.domain;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class CanonicalUrlFactory {

    public String forRequest(HttpServletRequest request, String canonicalPath) {
        SeoMetadata.validateCanonicalPath(canonicalPath);

        var builder =
                new StringBuilder()
                        .append(request.getScheme())
                        .append("://")
                        .append(request.getServerName());

        if (shouldIncludePort(request.getScheme(), request.getServerPort())) {
            builder.append(':').append(request.getServerPort());
        }

        builder.append(request.getContextPath()).append(canonicalPath);
        return builder.toString();
    }

    private boolean shouldIncludePort(String scheme, int port) {
        return !("http".equalsIgnoreCase(scheme) && port == 80
                || "https".equalsIgnoreCase(scheme) && port == 443);
    }
}
