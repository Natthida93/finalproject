package com.project.concert.model;

import jakarta.persistence.*;

@Entity
@Table(name = "admin")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String password;

    // --- Constructors ---
    public Admin() {}

    public Admin(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // --- Getters & Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {   // 👈 This is the one that was missing!
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
