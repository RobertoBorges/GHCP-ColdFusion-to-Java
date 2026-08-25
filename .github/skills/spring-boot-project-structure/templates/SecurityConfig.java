// =============================================================================
// Spring Security Configuration
// Part of the PHP-to-Java Migration Framework
// =============================================================================
// Spring Security setup for migrated PHP applications.
// Replaces Laravel's auth configuration, middleware, and guards.
// =============================================================================

package com.example.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration.
 *
 * Replaces:
 *   - Laravel's config/auth.php (guards, providers, passwords)
 *   - Laravel's auth middleware ('auth', 'guest', 'verified')
 *   - Laravel's Gate and Policy definitions (partially)
 *   - Laravel's app/Http/Kernel.php middleware stack
 *
 * @EnableWebSecurity activates Spring Security's web support.
 * @EnableMethodSecurity enables @PreAuthorize and @Secured on methods.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables @PreAuthorize("hasRole('ADMIN')") on controller/service methods
public class SecurityConfig {

    // ==========================================================================
    // Password Encoder
    //
    // Laravel: Hash::make($password) uses bcrypt by default
    // Spring: BCryptPasswordEncoder is the equivalent
    //
    // Usage in services: passwordEncoder.encode("password")
    //                    passwordEncoder.matches("password", encodedPassword)
    // ==========================================================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ==========================================================================
    // Security Filter Chain — Form Login (Session-based)
    //
    // This replaces:
    //   - Route middleware: Route::middleware('auth')
    //   - Guest middleware: Route::middleware('guest')
    //   - config/auth.php guards (web guard)
    //   - Login/Logout routes
    //
    // Use this for server-rendered apps with Thymeleaf (replaces Blade + auth).
    // ==========================================================================

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ================================================================
            // CSRF Protection
            // Laravel: @csrf in Blade forms (automatic with VerifyCsrfToken middleware)
            // Spring: Enabled by default. Thymeleaf auto-adds CSRF tokens with th:action.
            //
            // For REST APIs: disable CSRF and use JWT or other token auth instead.
            // ================================================================
            .csrf(csrf -> csrf
                // CSRF is enabled by default for form-based apps
                // To disable for API endpoints:
                // .ignoringRequestMatchers("/api/**")
            )

            // ================================================================
            // Session Management
            // Laravel: config/session.php
            // Spring: Configures session creation and fixation protection.
            // ================================================================
            .sessionManagement(session -> session
                .maximumSessions(1)                              // Max concurrent sessions per user
                // .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // Default
            )

            // ================================================================
            // Authorization Rules
            //
            // Laravel equivalents:
            //   Route::middleware('auth')  → .authenticated()
            //   Route::middleware('guest') → .anonymous() or permitAll
            //   Route::middleware('admin') → .hasRole("ADMIN")
            //   Route::middleware('can:manage-products') → .hasAuthority("MANAGE_PRODUCTS")
            //
            // Rules are evaluated in order — put specific rules first.
            // ================================================================
            .authorizeHttpRequests(auth -> auth
                // Public routes (no auth required) — replaces routes without 'auth' middleware
                .requestMatchers("/", "/home", "/about").permitAll()
                .requestMatchers("/login", "/register", "/forgot-password").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                .requestMatchers("/actuator/health").permitAll()

                // Admin-only routes — replaces Route::middleware('admin')
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // API routes — replaces Route::middleware('auth:sanctum')
                .requestMatchers("/api/**").authenticated()

                // All other routes require authentication — replaces Route::middleware('auth')
                .anyRequest().authenticated()
            )

            // ================================================================
            // Form Login
            //
            // Laravel: Auth::routes() generates login/register/password routes
            // Spring: Configure login page and success/failure handlers.
            // ================================================================
            .formLogin(form -> form
                .loginPage("/login")                             // Custom login page (Thymeleaf)
                .loginProcessingUrl("/login")                    // POST endpoint for login form
                .defaultSuccessUrl("/dashboard", true)           // Redirect after login
                .failureUrl("/login?error=true")                 // Redirect on login failure
                .usernameParameter("email")                      // Laravel uses 'email' field
                .passwordParameter("password")
                .permitAll()
            )

            // ================================================================
            // Logout
            //
            // Laravel: Auth::logout() or POST /logout
            // Spring: Configured here with redirect.
            // ================================================================
            .logout(logout -> logout
                .logoutUrl("/logout")                            // POST /logout
                .logoutSuccessUrl("/login?logout=true")          // Redirect after logout
                .invalidateHttpSession(true)                     // Destroy session
                .deleteCookies("JSESSIONID")                     // Clear session cookie
                .permitAll()
            )

            // ================================================================
            // Remember Me
            //
            // Laravel: Auth::viaRemember() / 'remember' checkbox
            // Spring: Token-based remember-me.
            // ================================================================
            .rememberMe(remember -> remember
                .key("uniqueAndSecretKey")                       // Change in production!
                .tokenValiditySeconds(86400 * 30)                // 30 days
                .rememberMeParameter("remember")                 // Form checkbox name
            );

        return http.build();
    }

    // ==========================================================================
    // JWT Configuration (Alternative — for API-only applications)
    //
    // Laravel Sanctum/Passport → Spring Security + JWT
    //
    // Uncomment and use this INSTEAD of the form login SecurityFilterChain
    // for REST API applications.
    // ==========================================================================

    /*
    @Bean
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // Disable CSRF for stateless API

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // No sessions

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )

            // Add JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    */
}

// =============================================================================
// UserDetailsService Implementation
// Replaces: Laravel's User model with Authenticatable trait
//
// Place in: security/CustomUserDetailsService.java
// =============================================================================

/*
package com.example.app.security;

import com.example.app.entity.User;
import com.example.app.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

// This replaces Laravel's EloquentUserProvider
// It loads user data from the database for authentication

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Called by Spring Security during authentication
    // Laravel equivalent: Auth::attempt(['email' => $email, 'password' => $password])
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Map roles to Spring Security authorities
        // Laravel: $user->hasRole('admin')
        // Spring: hasRole('ADMIN') checks for ROLE_ADMIN authority
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()))
                .toList();

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())        // BCrypt hash from database
                .authorities(authorities)
                .accountLocked(!user.isActive())          // Laravel: $user->is_active
                .build();
    }
}
*/

// =============================================================================
// Accessing Current User in Controllers
// =============================================================================
//
// Laravel: Auth::user(), Auth::id(), auth()->user()
//
// Spring Option 1: @AuthenticationPrincipal annotation (recommended)
//
//   @GetMapping("/profile")
//   public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
//       model.addAttribute("email", userDetails.getUsername());
//       return "profile";
//   }
//
// Spring Option 2: SecurityContextHolder (for service classes)
//
//   Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//   String email = auth.getName();
//
// Spring Option 3: Principal parameter
//
//   @GetMapping("/profile")
//   public String profile(Principal principal) {
//       String email = principal.getName();
//       return "profile";
//   }
//

// =============================================================================
// Thymeleaf Security Integration
// =============================================================================
//
// Add dependency: thymeleaf-extras-springsecurity6
//
// In Thymeleaf templates (replaces Blade @auth, @guest, @can directives):
//
//   <!-- @auth → sec:authorize="isAuthenticated()" -->
//   <div sec:authorize="isAuthenticated()">
//       Welcome, <span sec:authentication="name">User</span>!
//   </div>
//
//   <!-- @guest → sec:authorize="isAnonymous()" -->
//   <div sec:authorize="isAnonymous()">
//       <a th:href="@{/login}">Login</a>
//   </div>
//
//   <!-- @can('admin') → sec:authorize="hasRole('ADMIN')" -->
//   <div sec:authorize="hasRole('ADMIN')">
//       <a th:href="@{/admin}">Admin Panel</a>
//   </div>
//
//   <!-- Logout form (replaces @csrf + POST /logout) -->
//   <form th:action="@{/logout}" method="post">
//       <button type="submit">Logout</button>
//   </form>
//
