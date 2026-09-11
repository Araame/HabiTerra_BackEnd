package com.habiterra.shared.security;
import com.habiterra.identity.exception.AuthException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.Clock;
import java.util.HashMap;
@Component
public class AuthRateLimiter {
    private record Bucket(long until,int count){}
    private final java.util.Map<String,Bucket> buckets=new HashMap<>();
    private final Clock clock;private final int limit;
    public AuthRateLimiter(Clock clock,@Value("${auth.http.requests-per-minute:30}") int limit){
        if(limit<1)throw new IllegalArgumentException("Invalid rate limit");this.clock=clock;this.limit=limit;
    }
    public synchronized void check(String address){
        long now=clock.millis();buckets.entrySet().removeIf(e->e.getValue().until<=now);
        Bucket b=buckets.get(address);
        if(b!=null && b.count>=limit || b==null && buckets.size()>=10000)
            throw new AuthException(429,"AUTH_RATE_LIMIT","Trop de requetes, veuillez patienter");
        buckets.put(address,new Bucket(b==null?now+60000:b.until,b==null?1:b.count+1));
    }
}
