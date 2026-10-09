package com.submate.backend.member;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(nullable = false, length = 20)
    private String status;

    public Member(String email, String password) {
        this.email = email;
        this.password = password;
        this.role = "USER";
        this.status = "ACTIVE";
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void changeEmail(String email) {
        this.email = email;
    }

    public void withdraw() {
        this.status = "WITHDRAWN";
    }
}