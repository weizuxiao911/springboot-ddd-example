package com.ylz.example.infrastructure.entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user")
@DynamicInsert
@DynamicUpdate
public class UserEntity extends BaseEntity {

    public UserEntity(String userId, String nickname, String avatar) {
        this.userId = userId;
        this.nickname = nickname;
        this.avatar = avatar;
    }

    @Column(name = "user_id")
    private String userId;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "avatar")
    private String avatar;

}
