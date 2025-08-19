package ylz.example.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ylz.example.application.dto.CreateUserRequest;
import com.ylz.example.application.dto.UpdateNickanmeRequest;
import com.ylz.example.application.dto.UserResponse;
import com.ylz.example.application.impl.UserAppServiceImpl;
import com.ylz.example.domain.user.User;
import com.ylz.example.domain.user.UserId;
import com.ylz.example.domain.user.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAppServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserAppServiceImpl userAppService;

    /**
     * 测试根据ID查询用户
     */
    @Test
    void testGetUserById() {
        // 准备测试数据
        String userId = "USER_123";
        UserId domainUserId = new UserId(userId);
        User mockUser = new User(domainUserId, "testNickname", "testAvatar.png");
        
        // 模拟仓储行为
        when(userRepository.findById(domainUserId)).thenReturn(mockUser);
        
        // 执行查询
        UserResponse response = userAppService.getUserById(userId);
        
        // 验证结果
        assertNotNull(response);
        assertEquals(userId, response.userId());
        assertEquals("testNickname", response.nickname());
        assertEquals("testAvatar.png", response.avatar());
        verify(userRepository).findById(domainUserId);
    }

    /**
     * 测试创建用户
     */
    @Test
    void testCreateUser() {
        // 准备测试数据
        CreateUserRequest request = new CreateUserRequest("newUser", "newAvatar.png");
        UserId generatedId = UserId.generate();
        User savedUser = new User(generatedId, request.nickname(), request.avatar());
        
        // 模拟仓储保存行为
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // 执行创建
        UserResponse response = userAppService.createUser(request);
        
        // 验证结果
        assertNotNull(response);
        assertEquals(generatedId.value(), response.userId());
        assertEquals(request.nickname(), response.nickname());
        assertEquals(request.avatar(), response.avatar());
        verify(userRepository).save(any(User.class));
    }

    /**
     * 测试更新用户昵称
     */
    @Test
    void testUpdateUserNickname() {
        // 准备测试数据
        String userId = "USER_456";
        UserId domainUserId = new UserId(userId);
        User existingUser = new User(domainUserId, "oldNickname", "avatar.png");
        String newNickname = "updatedNickname";
        UpdateNickanmeRequest request = new UpdateNickanmeRequest(userId, newNickname);
        
        // 模拟仓储查询和保存行为
        when(userRepository.findById(domainUserId)).thenReturn(existingUser);
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        
        // 执行更新
        userAppService.updateUserNickname(request);
        
        // 验证领域对象已更新
        assertEquals(newNickname, existingUser.getNickname());
        // 验证仓储交互
        verify(userRepository).findById(domainUserId);
        verify(userRepository).save(existingUser);
    }

    /**
     * 测试更新不存在的用户时抛出异常
     */
    @Test
    void testUpdateUserNickname_UserNotFound() {
        // 准备测试数据
        String nonExistentUserId = "USER_999";
        UpdateNickanmeRequest request = new UpdateNickanmeRequest(nonExistentUserId, "newName");
        
        // 模拟仓储查询不到用户
        when(userRepository.findById(new UserId(nonExistentUserId))).thenThrow(new RuntimeException("用户不存在"));
        
        // 验证抛出异常
        assertThrows(RuntimeException.class, () -> {
            userAppService.updateUserNickname(request);
        });
        // 验证未调用保存方法
        verify(userRepository, never()).save(any(User.class));
    }
}
    
