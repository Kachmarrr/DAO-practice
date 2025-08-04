package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.util.DataTransferObject;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction implements DataTransferObject {

    private Long id;
    private BigDecimal amount;
    private Long customer_id; // чи краще зберігати private Customer ???

    public long getId() {
        return id != null ? id : 0L;
    }

}
