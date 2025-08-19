package com.ylz.example.infrastructure.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {
    

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(name = "modify_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime modifyTime;

    /**
     * 逻辑删除标识（0-未删除，1-已删除）
     */
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    /**
     * 记录创建时自动设置时间
     */
    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        this.modifyTime = LocalDateTime.now();
    }

    /**
     * 记录更新时自动更新时间
     */
    @PreUpdate
    public void preUpdate() {
        this.modifyTime = LocalDateTime.now();
    }

}
