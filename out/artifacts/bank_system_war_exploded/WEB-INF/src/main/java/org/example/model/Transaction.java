package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.example.util.DataTransferObject;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
public class Transaction implements DataTransferObject {

    private Long id;
    private BigDecimal amount;
    private Long sender_id;
    private Long recipient_id; // чи краще зберігати private Customer ???

    public long getId() {
        return id != null ? id : 0L;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
