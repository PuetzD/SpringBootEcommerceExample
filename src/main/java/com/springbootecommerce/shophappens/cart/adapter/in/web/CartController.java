package com.springbootecommerce.shophappens.cart.adapter.in.web;

import com.springbootecommerce.shophappens.cart.application.port.in.CartItemSnapshot;
import com.springbootecommerce.shophappens.cart.application.port.in.CustomerCartUseCase;
import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartConsumedException;
import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartSnapshot;
import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.BrowseCatalogUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductSummary;
import com.springbootecommerce.shophappens.customer.application.port.in.CurrentCustomerIdentity;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.shared.web.CanonicalUrlFactory;
import com.springbootecommerce.shophappens.shared.web.SeoMetadata;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Controller
@RequestMapping("/cart")
public class CartController {

    private final CurrentCustomerIdentity currentCustomer;
    private final GuestCartSession guestSessions;
    private final GuestCartUseCase guestCart;
    private final CustomerCartUseCase customerCart;
    private final BrowseCatalogUseCase catalog;
    private final CanonicalUrlFactory canonicalUrlFactory;

    @GetMapping
    public String view(HttpSession session, Model model) {
        Optional<CustomerReference> customer = currentCustomer.current();
        List<CartItemSnapshot> items =
                customer.map(c -> customerCart.getSnapshot(new CustomerId(c.value())))
                        .map(snapshot -> snapshot.items())
                        .orElseGet(
                                () ->
                                        guestSessions
                                                .find(session)
                                                .map(guestCart::getSnapshot)
                                                .map(GuestCartSnapshot::items)
                                                .orElse(List.of()));
        List<CartLine> lines =
                items.stream()
                        .map(
                                item ->
                                        new CartLine(
                                                item,
                                                catalog.findActiveByVariantId(item.variant())
                                                        .orElse(null)))
                        .toList();

        addSeo(model);
        model.addAttribute("lines", lines);
        model.addAttribute("cartEmpty", items.isEmpty());
        model.addAttribute(
                "checkoutAllowed",
                !items.isEmpty() && lines.stream().allMatch(CartLine::available));
        model.addAttribute("customer", customer.orElse(null));
        return "cart/detail";
    }

    @PostMapping("/items")
    public String addItem(
            HttpSession session,
            @RequestParam("variant") long variantId,
            @RequestParam("quantity") String rawQuantity) {
        int quantity = parseQuantity(rawQuantity);
        ProductVariantId variant = requireVariantId(variantId);
        if (catalog.findActiveByVariantId(variant).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Optional<CustomerReference> customer = currentCustomer.current();
        try {
            if (customer.isPresent()) {
                customerCart.add(new CustomerId(customer.get().value()), variant, quantity);
            } else {
                guestCart.add(guestSessions.getOrCreate(session), variant, quantity);
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
        return "redirect:/cart";
    }

    @PostMapping("/items/{variantId}/quantity")
    public String changeQuantity(
            HttpSession session,
            @PathVariable long variantId,
            @RequestParam("quantity") String rawQuantity) {
        ProductVariantId variant = requireVariantId(variantId);
        int quantity = parseQuantity(rawQuantity);
        Optional<CustomerReference> customer = currentCustomer.current();
        if (customer.isPresent()) {
            customerCart.changeQuantity(new CustomerId(customer.get().value()), variant, quantity);
        } else {
            guestCart.changeQuantity(guestSessions.getOrCreate(session), variant, quantity);
        }
        return "redirect:/cart";
    }

    @PostMapping("/items/{variantId}/remove")
    public String remove(HttpSession session, @PathVariable long variantId) {
        ProductVariantId variant = requireVariantId(variantId);
        Optional<CustomerReference> customer = currentCustomer.current();
        if (customer.isPresent()) {
            customerCart.remove(new CustomerId(customer.get().value()), variant);
        } else {
            guestCart.remove(guestSessions.getOrCreate(session), variant);
        }
        return "redirect:/cart";
    }

    @ExceptionHandler(GuestCartConsumedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String consumedGuestCart(GuestCartConsumedException exception, Model model) {
        return conflict(exception.getMessage(), model);
    }

    @ExceptionHandler({DataAccessException.class, TransactionException.class})
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String cartStorageUnavailable(Model model) {
        return conflict("Cart could not be saved. Reload your cart to check its contents.", model);
    }

    private static ProductVariantId requireVariantId(long value) {
        if (value < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Variant ID must be positive");
        }
        return new ProductVariantId(value);
    }

    private static int parseQuantity(String rawQuantity) {
        final int quantity;
        try {
            quantity = Integer.parseInt(rawQuantity);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Quantity must be a number between 1 and 999");
        }
        if (quantity < 1 || quantity > 999) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Quantity must be between 1 and 999");
        }
        return quantity;
    }

    private void addSeo(Model model) {
        var seo =
                new SeoMetadata(
                        "Your cart",
                        "Review and update the items in your cart.",
                        "/cart",
                        "noindex,nofollow");
        model.addAttribute("seo", seo);
        model.addAttribute("canonicalUrl", canonicalUrlFactory.forPath(seo.canonicalPath()));
    }

    private String conflict(String message, Model model) {
        addSeo(model);
        model.addAttribute("message", message);
        return "cart/conflict";
    }

    public record CartLine(CartItemSnapshot item, ProductSummary product) {
        public boolean available() {
            return product != null;
        }
    }
}
