package com.springbootecommerce.shophappens.ordering.adapter.out.cart;

import com.springbootecommerce.shophappens.cart.application.port.in.ClearCustomerCartUseCase;
import com.springbootecommerce.shophappens.cart.application.port.in.CustomerCartQuery;
import com.springbootecommerce.shophappens.cart.application.port.in.CustomerCartSnapshot;
import com.springbootecommerce.shophappens.ordering.application.port.out.CheckoutCart;
import com.springbootecommerce.shophappens.ordering.application.port.out.CustomerCartGateway;
import com.springbootecommerce.shophappens.ordering.application.port.out.RequestedProduct;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerCartGatewayAdapter implements CustomerCartGateway {
    private final CustomerCartQuery cartQuery;
    private final ClearCustomerCartUseCase cartClear;

    @Override
    public CheckoutCart load(CustomerId customerId) {
        CustomerCartSnapshot snapshot = cartQuery.get(customerId);
        List<RequestedProduct> products =
                snapshot.items().stream()
                        .map(item -> new RequestedProduct(item.product(), item.quantity()))
                        .toList();
        return new CheckoutCart(products);
    }

    @Override
    public void clear(CustomerId customerId) {
        cartClear.clear(customerId);
    }
}
