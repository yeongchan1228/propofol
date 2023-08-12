package propofol.tagservice.api.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import propofol.tagservice.api.common.properties.JwtProperties;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;
    private Key key;

    @PostConstruct
    private void createKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        key = Keys.hmacShaKeyFor(keyBytes);
    }

    public Authentication getUserInfo(String token){
        JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(key).build();
        Claims claims = jwtParser.parseClaimsJws(token).getBody();
        String memberId = claims.getSubject();
        String authority = claims.get("role").toString();

        Collection<? extends GrantedAuthority> at = Arrays.stream(authority.split(","))
                .map(role -> new SimpleGrantedAuthority(role))
                .collect(Collectors.toList());

        UserDetails principal = new User(memberId, "", at);


        return new UsernamePasswordAuthenticationToken(principal, "", at);
    }
}
