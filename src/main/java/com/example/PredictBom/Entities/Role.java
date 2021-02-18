package com.example.PredictBom.Entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "roles")
@Getter
@Setter
public class Role {

    @Id
    private String id;

    private ERole name;

    public Role(ERole name) {
        this.name = name;
    }


    @Override
    public boolean equals(Object o) {
        return id.equals(((Role) o).getId());
    }

}
