package com.example.shelfy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "app_user")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(name = "approved", nullable = false)
    private boolean approved = false;

    @Column(name = "admin", nullable = false)
    private boolean admin = false;

    @Column(name = "rejected", nullable = false)
    private boolean rejected = false;

    @Column(name = "color_index")
    private Integer colorIndex;

    @Column(name = "display_name")
    private String displayName;
}
