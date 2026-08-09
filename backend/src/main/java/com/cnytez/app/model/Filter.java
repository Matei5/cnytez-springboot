package com.cnytez.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "filters")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Filter {
    @Id
    private Integer id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String label;
}
