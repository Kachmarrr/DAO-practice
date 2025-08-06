package org.example.model;

import lombok.*;
import lombok.experimental.Accessors;
import org.example.util.DataTransferObject;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Bank implements DataTransferObject {

    private long id;
    private String name;

    List<Customer> users = new ArrayList<>();
}
