package org.example.model;

import lombok.*;
import org.example.util.DataTransferObject;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class Bank implements DataTransferObject {

    private long id;
    @NonNull
    private String name;

    List<Customer> users = new ArrayList<>();
}
