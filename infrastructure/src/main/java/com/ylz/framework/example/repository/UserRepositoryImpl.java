package com.ylz.framework.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ylz.framework.domain.user.User;
import com.ylz.framework.domain.user.UserId;
import com.ylz.framework.domain.user.UserRepository;
import com.ylz.framework.example.entity.UserEntity;
import com.ylz.framework.example.exception.UserNotFoundException;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final JpaUserRepository jpaRepo; // Spring Data JPA接口

    public UserRepositoryImpl(JpaUserRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public User findById(UserId userId) {
        return jpaRepo.findById(userId.value())
            .map(this::toDomain) // 数据库实体 → 领域对象
            .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    public User save(User user) {
        return toDomain(jpaRepo.save(toEntity(user))); // 领域对象 → 数据库实体
    }

    // 数据库实体与领域对象的转换（仅在基础设施层处理）
    private User toDomain(UserEntity entity) {
        User user = new User(new UserId(entity.getUserId()), entity.getNickname(), entity.getAvatar());
        return user;
    }

    private UserEntity toEntity(User user) {
        return new UserEntity(
            user.getUserId().value(),
            user.getNickname(),
            user.getAvatar()
        );
    }
}

// Spring Data JPA接口（仅在基础设施层存在）
interface JpaUserRepository extends JpaRepository<UserEntity, String> {
}
