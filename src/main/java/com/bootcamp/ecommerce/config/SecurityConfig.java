package com.bootcamp.ecommerce.config;

//    import com.bootcamp.ecommerce.service.impl.CustomUserDetailsService;
import com.bootcamp.ecommerce.CustomAuthenticationFailureHandler;
import com.bootcamp.ecommerce.JwtAuthenticationEntryPoint;
import com.bootcamp.ecommerce.filters.CustomAuthenticationFilter;
import com.bootcamp.ecommerce.filters.JwtAuthenticationFilter;
import com.bootcamp.ecommerce.repository.UserRepository;
import com.bootcamp.ecommerce.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;


    private  final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CustomAuthenticationFilter customFilter) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .csrf(AbstractHttpConfigurer::disable)

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterAt(
                        customFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/logout",
                                "/api/register/seller",
                                "/api/register/customer",
                                "/api/register/customer/activate",
                                "/api/register/resend-activation",
                                "/api/auth/refresh",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/customer/addresses",
                                "/api/customer/profile",
                                "/api/metadata/add/metadata-fields",
                                "/api/metadata/get/metadata-fields",
                                "/api/category/add/category"
                        ).permitAll()

                        .anyRequest().authenticated()
                );

        return http.build();
    }
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public CustomAuthenticationFilter customAuthenticationFilter(
            AuthenticationManager authenticationManager,
            JwtTokenService jwtTokenService,
            UserRepository userRepository) {

        CustomAuthenticationFilter filter =
                new CustomAuthenticationFilter(userRepository, jwtTokenService);

        filter.setAuthenticationManager(authenticationManager);
        filter.setAuthenticationFailureHandler(customAuthenticationFailureHandler);
        filter.setFilterProcessesUrl("/api/auth/login");

        return filter;
    }



    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
