package org.adt.volunteerscase.service.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${SECRET_KEY}")
    private String SECRET_KEY;

    @Value("${jwt.access-token.expiration.ms}")
    private Integer accessTokenExpirationMs;

    /**
     * Retrieve the username stored in the JWT's subject claim.
     *
     * @param token the JWT string to extract the subject from
     * @return the username contained in the token's `sub` (subject) claim, or `null` if the claim is absent
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts a specific value from a JWT's claims using the provided resolver.
     *
     * @param token the JWT string to parse
     * @param claimsResolver function that receives the token's Claims and returns a desired value
     * @param <T> the type of the value returned by the resolver
     * @return the value produced by applying the resolver to the token's claims
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parse and verify a signed JWT using the service signing key and return its claims.
     *
     * @param token the signed JWT in compact serialization form
     * @return the token's claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Generate a JWT access token for the provided user using no additional claims.
     *
     * @param userDetails the authenticated user's details whose username will be used as the token subject
     * @return a signed JWT access token string with the subject set to the user's username and expiration set per configuration
     */
    public String generateAccessToken(UserDetails userDetails) {
        return generateAccessToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates a JWT access token containing the provided claims and the user's username as the subject.
     *
     * The token includes the provided claims, the subject set to {@code userDetails.getUsername()},
     * an issued-at timestamp of the current time, and an expiration time of now plus the configured
     * access-token expiration. The token is signed with the service's HMAC-SHA signing key.
     *
     * @param extractClaims additional claims to include in the token payload
     * @param userDetails   the authenticated user's details; the username is used as the token subject
     * @return              the compact serialized JWT access token string
     */
    public String generateAccessToken(
            Map<String, Object> extractClaims,
            UserDetails userDetails
    ) {
        return Jwts.builder()
                .claims(extractClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(getSignKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Checks whether a JWT belongs to the given user and is not expired.
     *
     * @param token the JWT string to validate
     * @param userDetails the expected user details whose username must match the token's subject
     * @return `true` if the token's subject equals `userDetails.getUsername()` and the token is not expired, `false` otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Checks whether the given JWT has passed its expiration time.
     *
     * @param token the JWT string to inspect
     * @return `true` if the token's expiration date is before the current time, `false` otherwise
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from the given JWT.
     *
     * @param token the JWT string to extract the expiration from
     * @return the token's expiration date
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Creates the HMAC-SHA secret key derived from the configured Base64-encoded SECRET_KEY.
     *
     * @return the SecretKey used to sign and verify JWTs
     */
    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}