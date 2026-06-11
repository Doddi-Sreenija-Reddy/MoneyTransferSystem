package com.banking.transfer.security;

import com.banking.transfer.entity.Account;
import com.banking.transfer.entity.AccountStatus;
import com.banking.transfer.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        String username = "testuser";
        Account account = Account.builder()
                .username(username)
                .password("encodedPassword")
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountRepository.findByUsername(username)).thenReturn(Optional.of(account));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.isAccountNonLocked());
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserNotFound() {
        String username = "nonexistent";
        when(accountRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(username);
        });
    }

    @Test
    void loadUserByUsername_ShouldReturnLockedUser_WhenAccountNotActive() {
        String username = "lockeduser";
        Account account = Account.builder()
                .username(username)
                .password("encodedPassword")
                .status(AccountStatus.LOCKED)
                .build();

        when(accountRepository.findByUsername(username)).thenReturn(Optional.of(account));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertFalse(userDetails.isAccountNonLocked());
    }
}
