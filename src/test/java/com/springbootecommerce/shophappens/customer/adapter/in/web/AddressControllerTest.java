package com.springbootecommerce.shophappens.customer.adapter.in.web;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.springbootecommerce.shophappens.customer.application.port.in.AddressReference;
import com.springbootecommerce.shophappens.customer.application.port.in.AddressSnapshot;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.customer.application.port.in.ManageCustomerAddressesUseCase;
import com.springbootecommerce.shophappens.customer.application.port.in.OwnedAddressQuery;
import com.springbootecommerce.shophappens.customer.domain.exception.AddressNotOwnedException;
import com.springbootecommerce.shophappens.security.SecurityConfiguration;
import com.springbootecommerce.shophappens.web.support.CanonicalUrlFactory;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AddressController.class)
@Import({CanonicalUrlFactory.class, SecurityConfiguration.class})
class AddressControllerTest {
    @Autowired MockMvc mvc;

    @MockitoBean AuthenticatedCustomerResolver authenticator;
    @MockitoBean OwnedAddressQuery addresses;
    @MockitoBean ManageCustomerAddressesUseCase manager;

    private static final CustomerReference CUSTOMER = new CustomerReference(7L);

    @Test
    void redirectsAnonymousUsersToLogin() throws Exception {
        mvc.perform(get("/account/addresses")).andExpect(status().is3xxRedirection());
    }

    @Test
    void rendersTheAddressList() throws Exception {
        when(authenticator.resolve()).thenReturn(Optional.of(CUSTOMER));
        when(addresses.findForCustomer(CUSTOMER)).thenReturn(List.of(snapshot()));

        mvc.perform(get("/account/addresses").with(user("alex").roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/address-list"));
    }

    @Test
    void createsAnAddressAndRedirects() throws Exception {
        when(authenticator.resolve()).thenReturn(Optional.of(CUSTOMER));

        mvc.perform(
                        post("/account/addresses/new")
                                .with(user("alex").roles("CUSTOMER"))
                                .with(csrf())
                                .param("recipientName", "Alex Example")
                                .param("addressLine1", "1 Main Street")
                                .param("city", "Testcity")
                                .param("postalCode", "35037")
                                .param("countryCode", "DE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/addresses"));
    }

    @Test
    void editsAnOwnedAddressAndRedirects() throws Exception {
        when(authenticator.resolve()).thenReturn(Optional.of(CUSTOMER));
        when(addresses.getOwned(CUSTOMER, new AddressReference(11L))).thenReturn(snapshot(11L));

        mvc.perform(
                        post("/account/addresses/11/edit")
                                .with(user("alex").roles("CUSTOMER"))
                                .with(csrf())
                                .param("recipientName", "Alex Example")
                                .param("addressLine1", "1 Main Street")
                                .param("city", "Testcity")
                                .param("postalCode", "35037")
                                .param("countryCode", "DE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/addresses"));
    }

    @Test
    void setsDefaultShippingAndRedirects() throws Exception {
        when(authenticator.resolve()).thenReturn(Optional.of(CUSTOMER));
        when(addresses.getOwned(CUSTOMER, new AddressReference(11L))).thenReturn(snapshot(11L));

        mvc.perform(
                        post("/account/addresses/11/default-shipping")
                                .with(user("alex").roles("CUSTOMER"))
                                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/addresses"));
    }

    @Test
    void setsDefaultBillingAndRedirects() throws Exception {
        when(authenticator.resolve()).thenReturn(Optional.of(CUSTOMER));
        when(addresses.getOwned(CUSTOMER, new AddressReference(11L))).thenReturn(snapshot(11L));

        mvc.perform(
                        post("/account/addresses/11/default-billing")
                                .with(user("alex").roles("CUSTOMER"))
                                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/addresses"));
    }

    @Test
    void deletesAnOwnedAddressAndRedirects() throws Exception {
        when(authenticator.resolve()).thenReturn(Optional.of(CUSTOMER));

        mvc.perform(
                        post("/account/addresses/11/delete")
                                .with(user("alex").roles("CUSTOMER"))
                                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/addresses"));

        verify(manager).remove(CUSTOMER, new AddressReference(11L));
    }

    @Test
    void redisplaysTheFormOnValidationErrors() throws Exception {
        when(authenticator.resolve()).thenReturn(Optional.of(CUSTOMER));

        mvc.perform(
                        post("/account/addresses/new")
                                .with(user("alex").roles("CUSTOMER"))
                                .with(csrf())
                                .param("countryCode", "DEU"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/address-form"));
    }

    @Test
    void rejectsMutationsWithoutCsrfToken() throws Exception {
        mvc.perform(post("/account/addresses/new").with(user("alex").roles("CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void returnsNotFoundForForeignAddressLikeMissingAddress() throws Exception {
        when(authenticator.resolve()).thenReturn(Optional.of(CUSTOMER));
        when(addresses.getOwned(CUSTOMER, new AddressReference(999L)))
                .thenThrow(new AddressNotOwnedException("Address 999 is not owned"));

        mvc.perform(get("/account/addresses/999/edit").with(user("alex").roles("CUSTOMER")))
                .andExpect(status().isNotFound());
    }

    private static AddressSnapshot snapshot() {
        return snapshot(11L);
    }

    private static AddressSnapshot snapshot(long id) {
        return new AddressSnapshot(
                CUSTOMER,
                new AddressReference(id),
                "Alex Example",
                null,
                "1 Main Street",
                null,
                "Testcity",
                null,
                "35037",
                "DE",
                null,
                false,
                false);
    }
}
