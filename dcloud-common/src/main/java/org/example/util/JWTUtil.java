package org.example.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.example.model.LoginUser;

@Slf4j
public class JWTUtil {

    /**
     * 主题
     */
    private static final String SUBJECT = "dcloud-short-link";

    /**
     * 加密密钥
     */
    private static final String SECRET = "dcloud-short-link";

    /**
     * 令牌前缀
     */
    private static final String TOKNE_PREFIX = "dcloud-link";

    /**
     * token过期时间，7天
     */
    private static final long EXPIRED = 1000 * 60 * 60 * 24 * 7;

    /**
     * 生成token
     *
     * @param loginUser
     * @return
     */
    public static String geneJsonWebTokne(LoginUser loginUser) {
        if (loginUser == null) {
            throw new NullPointerException("对象为空");
        }
        String token = Jwts.builder().setSubject(SUBJECT)
            .claim("head_img", loginUser.getHeadImg())
            .claim("account_no", loginUser.getAccountNo())
            .claim("username", loginUser.getUsername())
            .claim("mail", loginUser.getMail())
            .claim("phone", loginUser.getPhone())
            .claim("auth", loginUser.getAuth())
            .setIssuedAt(new Date())
            .setExpiration(new Date(CommonUtil.getCurrentTimestamp() + EXPIRED))
            .signWith(SignatureAlgorithm.HS256, SECRET).compact();
        token = TOKNE_PREFIX + token;
        return token;
    }


    /**
     * 解密jwt
     *
     * @param token
     * @return
     */
    public static Claims checkJWT(String token) {
        try {
            final Claims claims = Jwts.parser().setSigningKey(SECRET)
                .parseClaimsJws(token.replace(TOKNE_PREFIX, "")).getBody();
            return claims;
        } catch (Exception e) {
            log.error("JWT解密失败");
            return null;
        }
    }
}
