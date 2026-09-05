package com.springbootecommerce.shophappens.cart.adapter.in.web;

import com.springbootecommerce.shophappens.cart.application.port.in.CartItemSnapshot;
import com.springbootecommerce.shophappens.cart.application.port.in.CustomerCartUseCase;
import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartSnapshot;
import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.BrowseCatalogUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductSummary;
import com.springbootecommerce.shophappens.customer.application.port.in.CurrentCustomerIdentity;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.shared.web.CanonicalUrlFactory;
import com.springbootecommerce.shophappens.shared.web.SeoMetadata;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
                                        catalog.findActiveById(
                                                        new ProductReference(
                                                                item.product().value()))
                                                .map(p -> new CartLine(item, p)))
                        .flatMap(Optional::stream)
                        .toList();

        addSeo(model);
        model.addAttribute("lines", lines);
        model.addAttribute("cartEmpty", lines.isEmpty());
        model.addAttribute("customer", customer.orElse(null));
        return "cart/detail";
    }

    @PostMapping("/items")
    public String addItem(
            HttpSession session,
            @RequestParam("product") long productId,
            @RequestParam("quantity") String rawQuantity) {
        int quantity = parseQuantity(rawQuantity);
        Optional<CustomerReference> customer = currentCustomer.current();
        if (customer.isPresent()) {
            customerCart.changeQuantity(
                    new CustomerId(customer.get().value()), new ProductId(productId), quantity);
        } else {
            guestCart.changeQuantity(
                    guestSessions.getOrCreate(session), new ProductId(productId), quantity);
        }
        return "redirect:/cart";
    }

    @PostMapping("/items/{productId}/remove")
    public String remove(HttpSession session, @PathVariable long productId) {
        Optional<CustomerReference> customer = currentCustomer.current();
        if (customer.isPresent()) {
            customerCart.remove(new CustomerId(customer.get().value()), new ProductId(productId));
        } else {
            guestCart.remove(guestSessions.getOrCreate(session), new ProductId(productId));
        }
        return "redirect:/cart";
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

    public record CartLine(CartItemSnapshot item, ProductSummary product) {}
}
