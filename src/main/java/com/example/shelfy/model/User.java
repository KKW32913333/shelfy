package com.example.shelfy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Linkle共用ユーザー（Linkle DBのapp_userテーブルを参照）
 * Shelfy側では読み取り専用で使用
 */
@Entity
@Table(name = "app_user")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean approved = false;

    @Column(nullable = false)
    private boolean admin = false;

    @Column(nullable = false)
    private boolean rejected = false;

    /** 表示色（グループ内のバッジ色） */
    private String color;
}
