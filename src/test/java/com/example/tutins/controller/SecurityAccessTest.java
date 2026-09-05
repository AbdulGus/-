package com.example.tutins.controller;

import com.example.tutins.config.SecurityConfig;
import com.example.tutins.dto.AuthDtos;
import com.example.tutins.security.CustomUserDetailsService;
import com.example.tutins.security.JwtAuthFilter;
import com.example.tutins.security.JwtService;
import com.example.tutins.service.AuthService;
import com.example.tutins.service.CourseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AuthController.class, CourseController.class})
@Import({SecurityConfig.class, JwtAuthFilter.class})
class SecurityAccessTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean AuthService authService;
    @MockitoBean CourseService courseService;
    @MockitoBean JwtService jwtService;
    @MockitoBean CustomUserDetailsService userDetailsService;

    @Test
    void authIsPublicButCoursesRequireJwt() throws Exception {
        when(authService.login(any())).thenReturn(
                new AuthDtos.AuthResponse("token", "Ivan", "ivan@example.com", "USER"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ivan@example.com\",\"password\":\"secret12\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"));

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Требуется корректный JWT-токен"));
    }
}
