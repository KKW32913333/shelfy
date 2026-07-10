package com.example.shelfy.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
    private Long id;
    private String username;
    private String password;
    private boolean approved = false;
    private boolean admin = false;
    private boolean rejected = false;
    private Integer colorIndex;
    private String displayName;
}
