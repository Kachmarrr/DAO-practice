package org.example.model;

import lombok.*;
import org.example.util.DataTransferObject;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@Builder
public class Customer implements DataTransferObject {

    private Long id;
    @NonNull
    private String firstName;
    @NonNull
    private String lastName;
    @NonNull
    private String email;

    private Long bankId;
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    private List<Transaction> transactions;

    public long getId() {
        return id != null ? id : 0L;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", balance=" + balance +
                ", transactions=" + transactions +
                '}';
    }
}
