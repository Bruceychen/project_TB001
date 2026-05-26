package com.bruceychen.tb001.service;

import com.bruceychen.tb001.dto.UserCreateRequest;
import com.bruceychen.tb001.dto.UserResponse;
import com.bruceychen.tb001.entity.User;
import com.bruceychen.tb001.repository.UserRepository;
import com.bruceychen.tb001.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;

    @InjectMocks UserService userService;

    private static User user(Long id, String name) {
        User u = new User(name);
        ReflectionTestUtils.setField(u, "userId", id);
        return u;
    }

    @Test
    void findAll_mapsAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user(1L, "alice"), user(2L, "bob")));

        List<UserResponse> all = userService.findAll();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(UserResponse::username).containsExactly("alice", "bob");
    }

    @Test
    void findById_returnsMappedUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, "alice")));

        UserResponse resp = userService.findById(1L);

        assertThat(resp.userId()).isEqualTo(1L);
        assertThat(resp.username()).isEqualTo("alice");
    }

    @Test
    void findById_missingUser_throwsEntityNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_savesAndReturnsMappedUser() {
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            ReflectionTestUtils.setField(u, "userId", 7L);
            return u;
        });

        UserResponse resp = userService.create(new UserCreateRequest("david"));

        assertThat(resp.userId()).isEqualTo(7L);
        assertThat(resp.username()).isEqualTo("david");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void delete_existingUser_callsDeleteById() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void delete_missingUser_throwsAndDoesNotDelete() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(userRepository, never()).deleteById(any());
    }
}
