package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.util.DataTransferObject;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Customer implements DataTransferObject {

    private Long id;
    private String firstName;
    private String LastName;
    private String email;
    private BigDecimal balance = BigDecimal.ZERO;

    private List<Transaction> transactions;

    public long getId() {
        return id;
    }
}
