package com.springbootecommerce.shophappens.ordering.adapter.out.gateway;

import com.springbootecommerce.shophappens.cart.application.port.in.ClearCustomerCartUseCase;
import com.springbootecommerce.shophappens.cart.application.port.in.CustomerCartQuery;
import com.springbootecommerce.shophappens.cart.application.port.in.CustomerCartSnapshot;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.ordering.application.port.out.CheckoutCart;
import com.springbootecommerce.shophappens.ordering.application.port.out.CustomerCartGateway;
import com.springbootecommerce.shophappens.ordering.application.port.out.RequestedProduct;
import com.springbootecommerce.shophappens.ordering.domain.model.CustomerId;
import com.springbootecommerce.shophappens.ordering.domain.model.ProductId;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
class CustomerCartGatewayAdapter implements CustomerCartGateway {
    private final CustomerCartQuery cartQuery;
    private final ClearCustomerCartUseCase cartClear;

    CustomerCartGatewayAdapter(CustomerCartQuery cartQuery, ClearCustomerCartUseCase cartClear) {
        this.cartQuery = cartQuery;
        this.cartClear = cartClear;
    }

    @Override
    public CheckoutCart load(CustomerId customerId) {
        CustomerCartSnapshot snapshot = cartQuery.get(toCustomer(customerId));
        List<RequestedProduct> products =
                snapshot.items().stream()
                        .map(
                                item ->
                                        new RequestedProduct(
                                                new ProductId(item.product().value()),
                                                item.quantity()))
                        .toList();
        return new CheckoutCart(products);
    }

    @Override
    public void clear(CustomerId customerId) {
        cartClear.clear(toCustomer(customerId));
    }

    private CustomerReference toCustomer(CustomerId customerId) {
        return new CustomerReference(customerId.value());
    }
}
