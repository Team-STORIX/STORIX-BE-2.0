package com.storix.domain.domains.user.adaptor;

import com.storix.domain.domains.user.dto.OIDCDecodePayload;
import com.storix.domain.domains.user.dto.OIDCPublicKeyDTO;
import com.storix.domain.domains.user.dto.OIDCPublicKeysResponse;
import com.storix.domain.domains.user.exception.token.ExpiredTokenException;
import com.storix.domain.domains.user.exception.token.InvalidTokenException;
import com.storix.domain.domains.user.exception.oauth.OidcJwksRefreshRequiredException;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

import static com.storix.common.utils.STORIXStatic.KID;

@Service
@RequiredArgsConstructor
public class JwtOIDCProvider {

    /**
     * idToken Payload 검증
     * */
    private Jwt<Header, Claims> getUnsignedTokenClaims(String idToken, String iss, String aud) {
        try {
            return Jwts.parserBuilder()
                    .requireAudience(aud)
                    .requireIssuer(iss)
                    .build()
                    .parseClaimsJwt(getUnsignedToken(idToken));
        } catch (ExpiredJwtException e) {
            throw ExpiredTokenException.EXCEPTION;
        } catch (Exception e) {
            throw InvalidTokenException.EXCEPTION;
        }
    }

    private String getUnsignedToken(String idToken) {
        String[] splitToken = idToken.split("\\.");
        if (splitToken.length != 3) throw InvalidTokenException.EXCEPTION;
        return splitToken[0] + "." + splitToken[1] + ".";
    }

    /**
     * idToken 서명 검증
     * */
    public String getKidFromUnsignedTokenHeader(String token, String iss, String aud) {
        return (String) getUnsignedTokenClaims(token, iss, aud).getHeader().get(KID);
    }

    public Jws<Claims> getOIDCTokenJws(String token, String modulus, String exponent) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getRSAPublicKey(modulus, exponent))
                    .build()
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw ExpiredTokenException.EXCEPTION;
        } catch (Exception e) {
            throw InvalidTokenException.EXCEPTION;
        }
    }

    public OIDCDecodePayload getOIDCTokenBody(String token, String modulus, String exponent) {
        Claims body = getOIDCTokenJws(token, modulus, exponent).getBody();
        return new OIDCDecodePayload(
                body.getIssuer(),
                body.getAudience(),
                body.getSubject());
    }

    private Key getRSAPublicKey(String modulus, String exponent)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] decodeN = Base64.getUrlDecoder().decode(modulus);
        byte[] decodeE = Base64.getUrlDecoder().decode(exponent);
        BigInteger n = new BigInteger(1, decodeN);
        BigInteger e = new BigInteger(1, decodeE);

        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(n, e);
        return keyFactory.generatePublic(keySpec);
    }

    public OIDCDecodePayload getPayloadFromIdToken(
            String idToken, String iss, String aud, OIDCPublicKeysResponse oidcPublicKeysResponse) {
        String kid = getKidFromUnsignedTokenHeader(idToken, iss, aud);

        OIDCPublicKeyDTO oidcPublicKeyDto =
                oidcPublicKeysResponse.getKeys().stream()
                        .filter(o -> o.getKid().equals(kid))
                        .findFirst()
                        .orElseThrow(OidcJwksRefreshRequiredException::new);

        return getOIDCTokenBody(
                idToken, oidcPublicKeyDto.getN(), oidcPublicKeyDto.getE());
    }

}
