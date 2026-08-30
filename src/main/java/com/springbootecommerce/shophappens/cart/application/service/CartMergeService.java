package com.springbootecommerce.shophappens.cart.application.service;

import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartReference;
import com.springbootecommerce.shophappens.cart.application.port.in.MergeGuestCartUseCase;
import com.springbootecommerce.shophappens.cart.application.port.out.AfterCommitExecutor;
import com.springbootecommerce.shophappens.cart.application.port.out.CartMergeLedger;
import com.springbootecommerce.shophappens.cart.application.port.out.CustomerCartRepository;
import com.springbootecommerce.shophappens.cart.application.port.out.GuestCartRepository;
import com.springbootecommerce.shophappens.cart.domain.model.Cart;
import com.springbootecommerce.shophappens.cart.domain.model.GuestCartId;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CartMergeService implements MergeGuestCartUseCase {
    private final GuestCartRepository guests;
    private final CustomerCartRepository customers;
    private final CartMergeLedger ledger;
    private final AfterCommitExecutor afterCommit;

    @Override
    @Transactional
    public void merge(GuestCartReference guest, CustomerReference customer) {
        GuestCartId guestId = new GuestCartId(guest.value());
        Optional<Cart> guestCart = guests.find(guestId);
        if (guestCart.isEmpty()) {
            return;
        }
        CustomerId customerId = new CustomerId(customer.value());
        if (!ledger.claim(guestId, customerId)) {
            return;
        }
        Cart customerCart = customers.findOrCreate(customerId);
        customerCart.merge(guestCart.get());
        customers.save(customerCart);
        afterCommit.execute(() -> guests.delete(guestId));
    }
}
