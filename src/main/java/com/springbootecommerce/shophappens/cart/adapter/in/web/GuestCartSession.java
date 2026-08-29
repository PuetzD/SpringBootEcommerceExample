package com.springbootecommerce.shophappens.cart.adapter.in.web;

import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartReference;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public final class GuestCartSession {

    public GuestCartReference getOrCreate(HttpSession session) {
        return find(session)
                .orElseGet(
                        () -> {
                            GuestCartReference guest = new GuestCartReference(UUID.randomUUID());
                            session.setAttribute(
                                    GuestCartReference.SESSION_ATTRIBUTE, guest.value().toString());
                            return guest;
                        });
    }

    public Optional<GuestCartReference> find(HttpSession session) {
        Object stored = session.getAttribute(GuestCartReference.SESSION_ATTRIBUTE);
        if (!(stored instanceof String raw)) {
            return Optional.empty();
        }
        try {
            return Optional.of(new GuestCartReference(UUID.fromString(raw)));
        } catch (IllegalArgumentException malformed) {
            return Optional.empty();
        }
    }

    public void remove(HttpSession session) {
        session.removeAttribute(GuestCartReference.SESSION_ATTRIBUTE);
    }
}
