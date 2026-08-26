package com.springbootecommerce.demo.customer.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "address")
public class Address {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", nullable = false)
  private Customer customer;

  @Column(name = "recipient_name", nullable = false, length = 200)
  private String recipientName;

  @Column(name = "company_name", length = 200)
  private String companyName;

  @Column(name = "address_line_1", nullable = false)
  private String addressLine1;

  @Column(name = "address_line_2")
  private String addressLine2;

  @Column(length = 100)
  private String city;

  @Column(length = 100)
  private String region;

  @Column(name = "postal_code", length = 32)
  private String postalCode;

  @Column(name = "country_code")
  @JdbcTypeCode(SqlTypes.CHAR)
  private String countryCode;

  @Column(name = "phone_number", length = 32)
  private String phoneNumber;

  @Column(name = "is_default_shipping", nullable = false)
  private boolean defaultShipping;

  @Column(name = "is_default_billing", nullable = false)
  private boolean defaultBilling;

  @Column(name = "created_at", insertable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", insertable = false, updatable = false)
  private OffsetDateTime updatedAt;
}
