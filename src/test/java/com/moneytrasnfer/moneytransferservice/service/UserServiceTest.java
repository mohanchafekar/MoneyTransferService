package com.moneytrasnfer.moneytransferservice.service;

import com.moneytrasnfer.moneytransferservice.entity.UserEntity;
import com.moneytrasnfer.moneytransferservice.exception.FunctionalErrorCode;
import com.moneytrasnfer.moneytransferservice.exception.FunctionalException;
import com.moneytrasnfer.moneytransferservice.model.UserRequest;
import com.moneytrasnfer.moneytransferservice.model.UserResponse;
import com.moneytrasnfer.moneytransferservice.repository.UserRepository;
import com.moneytrasnfer.moneytransferservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_Success() {
        UserRequest request = new UserRequest();
        request.setUniqueId("UID-1001");
        request.setFirstName("John");
        request.setLastName("Doe");

        UserEntity savedEntity = new UserEntity();
        savedEntity.setUserId(1L);
        savedEntity.setUniqueId("UID-1001");
        savedEntity.setFirstName("John");
        savedEntity.setLastName("Doe");

        when(userRepository.existsByUniqueId("UID-1001")).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

        UserResponse response = userService.createUser(request);

        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("UID-1001", response.getUniqueId());
        verify(userRepository, times(1)).existsByUniqueId("UID-1001");
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void createUser_ThrowsFunctionalException_WhenUserExists() {
        UserRequest request = new UserRequest();
        request.setUniqueId("UID-1001");

        when(userRepository.existsByUniqueId("UID-1001")).thenReturn(true);

        FunctionalException exception = assertThrows(FunctionalException.class, () -> {
            userService.createUser(request);
        });

        assertEquals(FunctionalErrorCode.USER_ALREADY_EXISTS, exception.getErrorCode());
        assertEquals("User with this unique ID already exists", exception.getMessage());
        verify(userRepository, times(1)).existsByUniqueId("UID-1001");
        verify(userRepository, never()).save(any(UserEntity.class));
    }
}
