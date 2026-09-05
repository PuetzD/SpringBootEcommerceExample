package com.springbootecommerce.shophappens.ordering.adapter.in.web;

import com.springbootecommerce.shophappens.customer.application.port.in.CurrentCustomerIdentity;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderDetail;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderQuery;
import com.springbootecommerce.shophappens.shared.web.CanonicalUrlFactory;
import com.springbootecommerce.shophappens.shared.web.SeoMetadata;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {
    private final OrderQuery orders;
    private final CurrentCustomerIdentity currentCustomer;
    private final CanonicalUrlFactory canonicalUrlFactory;

    @GetMapping
    public String list(Model model) {
        CustomerReference customer = currentCustomerOrThrow();
        model.addAttribute("orders", orders.findAll(new CustomerId(customer.value())));
        addSeo(model, "Your orders", "/orders");
        return "ordering/order-list";
    }

    @GetMapping("/{orderNumber}")
    public String detail(@PathVariable String orderNumber, Model model) {
        CustomerReference customer = currentCustomerOrThrow();
        OrderDetail order =
                orders.findOwned(new CustomerId(customer.value()), orderNumber)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Order not found"));
        model.addAttribute("order", order);
        addSeo(model, "Order " + order.orderNumber(), "/orders/" + order.orderNumber());
        return "ordering/order-detail";
    }

    private CustomerReference currentCustomerOrThrow() {
        return currentCustomer
                .current()
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND, "Customer not found"));
    }

    private void addSeo(Model model, String title, String path) {
        var seo =
                new SeoMetadata(title, "View your Shop Happens orders.", path, "noindex,nofollow");
        model.addAttribute("seo", seo);
        model.addAttribute("canonicalUrl", canonicalUrlFactory.forPath(path));
    }
}
