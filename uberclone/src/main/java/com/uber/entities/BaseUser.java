package com.uber.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@MappedSuperclass
public class BaseUser {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private int id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(unique = true , nullable = false , length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private  String password;

    private boolean isEnabled;


    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(unique = true,  nullable = false)
    private String phoneNumber;

    @Column(nullable = true)
    @Builder.Default
    private Integer ratingCount = 0;



}
