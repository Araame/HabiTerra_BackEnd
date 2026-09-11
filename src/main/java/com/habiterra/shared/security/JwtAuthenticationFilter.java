package com.habiterra.shared.security;
import com.habiterra.identity.service.AuthService;
import com.habiterra.identity.exception.AuthException;
import com.habiterra.shared.exception.ApiErrorWriter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService tokens;private final AuthService auth;private final ApiErrorWriter errors;private final AuthRateLimiter limiter;
    public JwtAuthenticationFilter(JwtService tokens,AuthService auth,ApiErrorWriter errors,AuthRateLimiter limiter){
        this.tokens=tokens;this.auth=auth;this.errors=errors;this.limiter=limiter;
    }
    @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException {
        try{
            if(req.getRequestURI().startsWith("/api/v1/auth/") && "POST".equals(req.getMethod()))limiter.check(req.getRemoteAddr());
            String header=req.getHeader("Authorization");
            if(header!=null){
                if(!header.startsWith("Bearer "))throw new AuthException(401,"JWT_INVALID","Token invalide");
                var claims=tokens.accessClaims(header.substring(7));
                Long id;
                try {
                    id=Long.valueOf(claims.getSubject());
                    if(!id.equals(claims.getLongClaim("userId")) || claims.getStringClaim("role")==null)throw new IllegalArgumentException();
                }catch(Exception e){throw new AuthException(401,"JWT_INVALID","Token invalide");}
                var user=auth.activeUser(id);
                var authentication=UsernamePasswordAuthenticationToken.authenticated(id,null,
                    List.of(new SimpleGrantedAuthority("ROLE_"+user.getRole().name())));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }catch(AuthException e){SecurityContextHolder.clearContext();errors.write(req,res,e.getStatus(),e.getCode(),e.getMessage());return;}
        chain.doFilter(req,res);
    }
}
