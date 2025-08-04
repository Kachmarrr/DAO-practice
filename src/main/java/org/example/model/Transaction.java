package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.util.DataTransferObject;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction implements DataTransferObject {

    private Long id;
    private BigDecimal amount;
    private Long sender_id; // чи краще зберігати private Customer ???
    private Long recipient_id; // чи краще зберігати private Customer ???

    public long getId() {
        return id != null ? id : 0L;
    }

}
