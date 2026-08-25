// =============================================================================
// Spring Security Configuration
// Part of the ColdFusion-to-Java Migration Framework
// =============================================================================
// Spring Security setup for migrated ColdFusion applications.
// Replaces <cflogin> / <cfloginuser>, isUserInRole(), and session-based auth checks.
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
 *   - The <cflogin> / <cfloginuser> authentication container
 *   - isUserInRole() role checks scattered through .cfm pages
 *   - Security gating done in Application.cfc onRequestStart()
 *   - Custom login/logout .cfm pages
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
    // CFML: hash(password, "SHA-512") / a custom bcrypt CFC / encrypt()
    // Spring: BCryptPasswordEncoder is the modern equivalent
    //
    // If the legacy app used weaker hashing (SHA/MD5), re-hash with BCrypt on the
    // user's next successful login.
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
    //   - The <cflogin> block that wrapped protected pages
    //   - isUserInRole() checks and onRequestStart() security gates
    //   - Login/Logout .cfm pages
    //
    // Use this for server-rendered apps with Thymeleaf (replaces .cfm pages + <cflogin>).
    // ==========================================================================

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ================================================================
            // CSRF Protection
            // CFML: <cfform> or CSRFGenerateToken()/CSRFVerifyToken() (often absent in legacy apps)
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
            // CFML: this.sessionManagement / this.sessionTimeout in Application.cfc
            // Spring: Configures session creation and fixation protection.
            // ================================================================
            .sessionManagement(session -> session
                .maximumSessions(1)                              // Max concurrent sessions per user
                // .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // Default
            )

            // ================================================================
            // Authorization Rules
            //
            // CFML equivalents:
            //   <cfif isUserLoggedIn()>          → .authenticated()
            //   pages outside the <cflogin> block → .anonymous() or permitAll
            //   <cfif isUserInRole("admin")>     → .hasRole("ADMIN")
            //   fine-grained permission checks    → .hasAuthority("MANAGE_PRODUCTS")
            //
            // Rules are evaluated in order — put specific rules first.
            // ================================================================
            .authorizeHttpRequests(auth -> auth
                // Public routes (no auth required) — replaces .cfm pages outside the <cflogin> block
                .requestMatchers("/", "/home", "/about").permitAll()
                .requestMatchers("/login", "/register", "/forgot-password").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                .requestMatchers("/actuator/health").permitAll()

                // Admin-only routes — replaces isUserInRole("admin") checks
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // API routes — replaces API-key / session checks in api/
                .requestMatchers("/api/**").authenticated()

                // All other routes require authentication — replaces the <cflogin> gate
                .anyRequest().authenticated()
            )

            // ================================================================
            // Form Login
            //
            // CFML: login.cfm form → <cflogin> / <cfloginuser name="..." roles="...">
            // Spring: Configure login page and success/failure handlers.
            // ================================================================
            .formLogin(form -> form
                .loginPage("/login")                             // Custom login page (Thymeleaf)
                .loginProcessingUrl("/login")                    // POST endpoint for login form
                .defaultSuccessUrl("/dashboard", true)           // Redirect after login
                .failureUrl("/login?error=true")                 // Redirect on login failure
                .usernameParameter("email")                      // the login form's email field
                .passwordParameter("password")
                .permitAll()
            )

            // ================================================================
            // Logout
            //
            // CFML: logout.cfm calling <cflogout>
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
            // CFML: a persistent-login cookie (e.g. an encrypted cookie checked in onRequestStart)
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
    // CFML API token / session auth (custom) → Spring Security + JWT
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
// Replaces: the CFC that validated credentials for <cfloginuser>
//           (e.g. a security/user service CFC + its <cfquery> lookup)
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

// This replaces the <cfquery> that looked up a user before <cfloginuser>.
// It loads user data from the database for authentication.

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Called by Spring Security during authentication
    // CFML equivalent: the <cfquery> verifying email + password before <cfloginuser>
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Map roles to Spring Security authorities
        // CFML: <cfloginuser name="#email#" password="..." roles="#roleList#">
        // Spring: hasRole('ADMIN') checks for ROLE_ADMIN authority
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()))
                .toList();

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())        // BCrypt hash from database
                .authorities(authorities)
                .accountLocked(!user.isActive())          // the active flag column
                .build();
    }
}
*/

// =============================================================================
// Accessing Current User in Controllers
// =============================================================================
//
// CFML: getAuthUser(), session.user
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
// In Thymeleaf templates (replaces <cfif isUserLoggedIn()> / <cfif isUserInRole()> blocks in .cfm):
//
//   <!-- <cfif isUserLoggedIn()> → sec:authorize="isAuthenticated()" -->
//   <div sec:authorize="isAuthenticated()">
//       Welcome, <span sec:authentication="name">User</span>!
//   </div>
//
//   <!-- <cfif NOT isUserLoggedIn()> → sec:authorize="isAnonymous()" -->
//   <div sec:authorize="isAnonymous()">
//       <a th:href="@{/login}">Login</a>
//   </div>
//
//   <!-- <cfif isUserInRole('admin')> → sec:authorize="hasRole('ADMIN')" -->
//   <div sec:authorize="hasRole('ADMIN')">
//       <a th:href="@{/admin}">Admin Panel</a>
//   </div>
//
//   <!-- Logout form (replaces a link to logout.cfm calling <cflogout>) -->
//   <form th:action="@{/logout}" method="post">
//       <button type="submit">Logout</button>
//   </form>
//
