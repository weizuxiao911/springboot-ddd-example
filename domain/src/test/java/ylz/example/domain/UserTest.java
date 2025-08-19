package ylz.example.domain;

import org.junit.jupiter.api.Test;

import com.ylz.example.domain.user.User;
import com.ylz.example.domain.user.UserId;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    /**
     * 测试用户创建功能
     */
    @Test
    void testCreateUser() {
        // 准备测试数据
        UserId userId = new UserId("1");
        
        // 执行创建操作
        User user = User.create(userId);
        
        // 验证结果
        assertNotNull(user, "用户对象不应为null");
        assertEquals(userId, user.getId(), "用户ID应与创建时一致");
        assertNull(user.getNickname(), "新创建用户的昵称为null");
        assertNull(user.getAvatar(), "新创建用户的头像为null");
    }

    /**
     * 测试通过构造函数创建用户
     */
    @Test
    void testUserConstructor() {
        // 准备测试数据
        UserId userId = new UserId("2");
        String nickname = "testNickname";
        String avatar = "testAvatarUrl";
        
        // 执行构造函数
        User user = new User(userId, nickname, avatar);
        
        // 验证结果
        assertEquals(userId, user.getId(), "用户ID不正确");
        assertEquals(nickname, user.getNickname(), "用户昵称不正确");
        assertEquals(avatar, user.getAvatar(), "用户头像不正确");
    }

    /**
     * 测试正常编辑用户属性
     */
    @Test
    void testEditUserFields() {
        // 准备测试数据
        User user = User.create(new UserId("3"));
        String newNickname = "updatedNickname";
        String newAvatar = "updatedAvatarUrl";
        
        // 执行编辑操作
        user.update(User::getNickname, newNickname);
        user.update(User::getAvatar, newAvatar);
        
        // 验证结果
        assertEquals(newNickname, user.getNickname(), "昵称更新失败");
        assertEquals(newAvatar, user.getAvatar(), "头像更新失败");
    }

    /**
     * 测试编辑不存在的字段时抛出异常
     */
    @Test
    void testEditNonExistentField() {
        // 准备测试数据
        User user = User.create(new UserId("4"));
        
        // 执行编辑操作并验证异常
        assertThrows(RuntimeException.class, () -> {
            // 尝试编辑不存在的字段（假设User没有age字段）
            user.update(u -> "dummy", "value");
        }, "编辑不存在的字段应抛出异常");
    }

    /**
     * 测试用户ID不可修改（通过构造函数初始化后保持不变）
     */
    @Test
    void testUserIdImmutability() {
        // 准备测试数据
        UserId originalId = new UserId("5");
        User user = User.create(originalId);
        UserId newId = new UserId("6");
        
        // 尝试编辑ID（应失败，因为ID是final字段）
        assertThrows(RuntimeException.class, () -> {
            user.update(User::getId, newId);
        }, "用户ID是不可变的，编辑应抛出异常");
        
        // 验证ID未被修改
        assertEquals(originalId, user.getId(), "用户ID不应被修改");
    }
}
