package com.example.tutins.service;

import com.example.tutins.dto.AuthDtos;
import com.example.tutins.entity.Role;
import com.example.tutins.entity.User;
import com.example.tutins.exception.BusinessException;
import com.example.tutins.repository.UserRepository;
import com.example.tutins.security.CustomUserDetailsService;
import com.example.tutins.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Пользователь с таким email уже существует");
        }
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        userRepository.save(user);
        log.info("Registered user id={} email={}", user.getId(), email);
        return response(user);
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BusinessException("Неверный email или пароль"));
        log.info("User logged in: {}", user.getEmail());
        return response(user);
    }

    private AuthDtos.AuthResponse response(User user) {
        UserDetails details = userDetailsService.loadUserByUsername(user.getEmail());
        return new AuthDtos.AuthResponse(jwtService.generateToken(details), user.getName(),
                user.getEmail(), user.getRole().name());
    }
}
