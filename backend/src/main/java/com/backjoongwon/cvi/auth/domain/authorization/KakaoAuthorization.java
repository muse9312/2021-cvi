package com.backjoongwon.cvi.auth.domain.authorization;

import com.backjoongwon.cvi.auth.dto.oauthtoken.KakaoOAuthToken;
import com.backjoongwon.cvi.auth.dto.oauthtoken.OAuthToken;
import com.backjoongwon.cvi.auth.dto.profile.KakaoProfile;
import com.backjoongwon.cvi.auth.dto.profile.SocialProfile;
import com.backjoongwon.cvi.auth.dto.profile.UserInformation;
import com.backjoongwon.cvi.common.exception.MappingFailureException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class KakaoAuthorization implements Authorization {

    private static final String PROFILE_REQUEST_URL = "https://kapi.kakao.com/v2/user/me";
    private static final String TOKEN_REQUEST_URL = "https://kauth.kakao.com/oauth/token";
    private static final String REDIRECT_URL = "https://vaccine-review.com/auth/kakao/callback";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public UserInformation requestProfile(String code, String state) {
        OAuthToken kakaoOAuthToken = requestToken(code, state);
        return parseProfile(kakaoOAuthToken);
    }

    @Override
    public UserInformation parseProfile(OAuthToken oAuthToken) {
        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest = createProfileRequest(oAuthToken);
        ResponseEntity<String> response = sendRequest(kakaoProfileRequest, PROFILE_REQUEST_URL);
        return UserInformation.of(mapToProfile(response));
    }

    @Override
    public OAuthToken requestToken(String code, String state) {
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = createTokenRequest(code, state);
        ResponseEntity<String> response = sendRequest(kakaoTokenRequest, TOKEN_REQUEST_URL);
        return mapToOAuthToken(response);
    }

    @Override
    public OAuthToken mapToOAuthToken(ResponseEntity<String> response) {
        try {
            return objectMapper.readValue(response.getBody(), KakaoOAuthToken.class);
        } catch (JsonProcessingException e) {
            log.info("토큰 정보를 매핑하는데 실패했습니다. 입력값: {}", response.getBody());
            throw new MappingFailureException(String.format("토큰 정보를 매핑하는데 실패했습니다. 입력값: %s", response.getBody()));
        }
    }

    @Override
    public SocialProfile mapToProfile(ResponseEntity<String> response) {
        try {
            return objectMapper.readValue(response.getBody(), KakaoProfile.class);
        } catch (JsonProcessingException e) {
            log.info("프로필 정보를 매핑하는데 실패했습니다. 입력값: {}", response.getBody());
            throw new MappingFailureException(String.format("프로필 정보를 매핑하는데 실패했습니다. 입력값: %s", response.getBody()));
        }
    }

    @Override
    public HttpEntity<MultiValueMap<String, String>> createTokenRequest(String code, String state) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", "1a06cf63be2ce0a6ebd8f49cd534e1c9");
        params.add("redirect_uri", REDIRECT_URL);
        params.add("code", code);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        return new HttpEntity<>(params, headers);
    }

    @Override
    public HttpEntity<MultiValueMap<String, String>> createProfileRequest(OAuthToken kakaoOAuthToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + kakaoOAuthToken.getAccess_token());
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        return new HttpEntity<>(headers);
    }

    @Override
    public ResponseEntity<String> sendRequest(HttpEntity<MultiValueMap<String, String>> request, String url) {
        return restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                String.class
        );
    }
}
