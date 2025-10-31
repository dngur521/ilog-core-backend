package com.webkit640.ilog_core_backend;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * Jitsi as a Service (JaaS)와의 연동을 위한 JWT(JSON Web Token)를 생성하는 컨트롤러입니다. 프론트엔드에서
 * Jitsi 회의에 참가하기 위해 필요한 인증 토큰을 발급합니다.
 */
@RestController
public class JitsiController {

    // application.properties(yml) 파일에서 JaaS App ID를 주입받습니다.
    @Value("${jaas.app.id}")
    private String jaasAppId;

    // application.properties(yml) 파일에 명시된 경로의 JaaS Private Key 파일을 리소스로 주입받습니다.
    @Value("${jaas.api.key.path}")
    private Resource privateKeyResource;

    /**
     * 프론트엔드로부터 방 이름(roomName), 사용자 이름(userName) 등을 받아 Jitsi 회의 참가에 필요한 JWT를
     * 생성하여 반환합니다.
     *
     * @param payload 프론트엔드에서 전송한 JSON 데이터 (roomName, userName 등 포함)
     * @return 생성된 JWT가 담긴 ResponseEntity 객체
     */
    @PostMapping("/jitsi-jwt")
    public ResponseEntity<Map<String, String>> generateJitsiJwt(@RequestBody Map<String, String> payload) {
        try {
            // 1. 프론트엔드에서 받은 데이터 추출 (없을 경우 기본값 사용)
            String roomName = payload.getOrDefault("roomName", "*"); // room이 '*'이면 사용자가 방을 자유롭게 생성/참가 가능
            String userName = payload.getOrDefault("userName", "참가자");
            String userEmail = payload.getOrDefault("userEmail", "email@example.com");

            // 2. JWT의 'sub' 클레임 값 설정 (App ID)
            String sub = jaasAppId;
            if (jaasAppId.contains("/")) {
                sub = jaasAppId.split("/")[0];
            }

            // 3. 리소스 경로에서 Private Key 파일 읽기
            String privateKeyPEM;
            try (Reader reader = new InputStreamReader(privateKeyResource.getInputStream(), StandardCharsets.UTF_8)) {
                privateKeyPEM = FileCopyUtils.copyToString(reader);
            }

            // 4. PEM 형식의 문자열을 Java의 PrivateKey 객체로 변환
            PrivateKey privateKey = getPrivateKeyFromString(privateKeyPEM);

            // 5. JWT 생성 (jjwt 라이브러리 사용)
            String jwt = Jwts.builder()
                    // --- Header ---
                    .setHeaderParam("kid", jaasAppId) // Key ID: JaaS App ID
                    .setHeaderParam("typ", "JWT")
                    // --- Payload ---
                    .setIssuer("chat") // 발급자
                    .setAudience("jitsi") // 대상
                    .setSubject(sub) // 주제 (App ID)
                    .setExpiration(Date.from(Instant.now().plusSeconds(7200))) // 만료 시간 (2시간)
                    .setNotBefore(Date.from(Instant.now().minusSeconds(10))) // 유효 시작 시간 (10초 전)
                    .claim("room", roomName) // 참가할 방 이름
                    .claim("context", Map.of( // 사용자 정보 및 기능 활성화 컨텍스트
                            "user", Map.of(
                                    "id", UUID.randomUUID().toString(),
                                    "name", userName,
                                    "email", userEmail,
                                    "moderator", "true" // 모든 사용자를 중재자로 설정
                            ),
                            "features", Map.of(
                                    "livestreaming", false,
                                    "recording", true,
                                    "transcription", false,
                                    "outbound-call", false
                            )
                    ))
                    // --- Signature ---
                    .signWith(privateKey, SignatureAlgorithm.RS256) // Private Key와 RS256 알고리즘으로 서명
                    .compact(); // 문자열로 직렬화

            System.out.println("RS256 기반 최종 Jitsi JWT 발급 완료: room=" + roomName);

            // 6. 생성된 JWT를 JSON 형태로 클라이언트에 반환
            return ResponseEntity.ok(Map.of("jwt", jwt));
        } catch (Exception e) {
            System.err.println("JWT 생성 중 심각한 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "JWT 생성 실패"));
        }
    }

    /**
     * PEM 형식의 Private Key 문자열을 Java의 PrivateKey 객체로 변환하는 유틸리티 메소드입니다.
     *
     * @param pemKey "-----BEGIN PRIVATE KEY-----" ... "-----END PRIVATE
     * KEY-----" 형식의 문자열
     * @return PrivateKey 객체
     * @throws Exception 키 변환 과정에서 발생하는 예외
     */
    private PrivateKey getPrivateKeyFromString(String pemKey) throws Exception {
        // 1. PEM 형식에서 헤더, 푸터, 개행 문자를 제거합니다.
        String privateKeyPEM = pemKey
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("\\r\\n|\\n", "")
                .replace("-----END PRIVATE KEY-----", "");

        // 2. Base64로 인코딩된 키 문자열을 바이트 배열로 디코딩합니다.
        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);

        // 3. RSA 알고리즘을 사용하는 KeyFactory를 통해 PKCS8 형식의 키 스펙으로 PrivateKey 객체를 생성합니다.
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return keyFactory.generatePrivate(keySpec);
    }
}
