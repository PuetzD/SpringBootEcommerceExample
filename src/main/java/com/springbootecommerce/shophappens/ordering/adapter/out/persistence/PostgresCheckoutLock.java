package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import com.springbootecommerce.shophappens.ordering.application.port.out.CheckoutLock;
import com.springbootecommerce.shophappens.ordering.domain.model.CheckoutId;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class PostgresCheckoutLock implements CheckoutLock {
    private final DataSource dataSource;

    @Override
    public void acquire(CustomerId customerId, CheckoutId checkoutId) {
        int first = (int) (customerId.value() ^ (customerId.value() >>> 32));
        int second = checkoutId.value().hashCode();
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement =
                connection.prepareStatement("SELECT pg_advisory_xact_lock(?, ?)")) {
            statement.setInt(1, first);
            statement.setInt(2, second);
            ResultSet resultSet = statement.executeQuery();
            resultSet.close();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to acquire checkout lock", e);
        }
    }
}
