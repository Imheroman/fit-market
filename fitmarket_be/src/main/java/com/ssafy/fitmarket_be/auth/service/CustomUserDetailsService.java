package com.ssafy.fitmarket_be.auth.service;

import com.ssafy.fitmarket_be.auth.dto.CustomUserDetails;
import com.ssafy.fitmarket_be.entity.User;
import com.ssafy.fitmarket_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService { // Changed

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = this.userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    return new CustomUserDetails(user); // UserDetails 구현체
  }
}
