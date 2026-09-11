package com.habiterra.identity.controller;
import com.habiterra.identity.dto.*;
import com.habiterra.identity.service.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final OtpService otp;private final AuthService auth;
    public AuthController(OtpService otp,AuthService auth){this.otp=otp;this.auth=auth;}
    @PostMapping("/request-otp") public RequestOtpResponse request(@Valid @RequestBody RequestOtpRequest r){return otp.request(r.identifier());}
    @PostMapping("/resend-otp") public RequestOtpResponse resend(@Valid @RequestBody ResendOtpRequest r){return otp.request(r.identifier());}
    @PostMapping("/verify-otp") public VerifyOtpResponse verify(@Valid @RequestBody VerifyOtpRequest r){return otp.verify(r.identifier(),r.otp());}
    @PostMapping("/complete-registration") public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody CompleteRegistrationRequest r){
        return ResponseEntity.status(201).body(auth.register(r));
    }
    @PostMapping("/login") public AuthenticationResponse login(@Valid @RequestBody LoginRequest r){return auth.login(r);}
    @GetMapping("/me") public UserResponse me(Authentication authentication){return auth.me(Long.valueOf(authentication.getName()));}
    /** Stateless logout: the client discards its token; existing JWTs retain their expiry. */
    @PostMapping("/logout") public ResponseEntity<Void> logout(){return ResponseEntity.noContent().build();}
}
