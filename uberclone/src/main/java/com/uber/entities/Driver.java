package com.uber.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@SuperBuilder
public class Driver extends BaseUser {

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "driver_roles", // Changed from user_roles to avoid conflict
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Column(nullable = false)
    private Double rating;

    @Column(nullable = false, unique = true)
    private String licensePlate;

    @Column(nullable = false)
    private String carModel;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isAvailable = false;

    @OneToMany(mappedBy = "driver" , cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    Set<Ride> rides = new HashSet<>();

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }
}

