package FIS.iLUVit.service;

import FIS.iLUVit.controller.dto.AlarmDto;
import FIS.iLUVit.controller.dto.TokenRefreshRequest;
import FIS.iLUVit.controller.dto.UpdatePasswordRequest;
import FIS.iLUVit.controller.dto.UserInfoResponse;
import FIS.iLUVit.domain.AuthNumber;
import FIS.iLUVit.domain.TokenPair;
import FIS.iLUVit.domain.User;
import FIS.iLUVit.domain.alarms.Alarm;
import FIS.iLUVit.domain.enumtype.AuthKind;
import FIS.iLUVit.exception.*;
import FIS.iLUVit.repository.AlarmRepository;
import FIS.iLUVit.repository.AuthNumberRepository;
import FIS.iLUVit.repository.TokenPairRepository;
import FIS.iLUVit.repository.UserRepository;
import FIS.iLUVit.security.JwtUtils;
import FIS.iLUVit.security.LoginRequest;
import FIS.iLUVit.security.LoginResponse;
import FIS.iLUVit.security.uesrdetails.PrincipalDetails;
import com.auth0.jwt.exceptions.JWTVerificationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;
    private final AuthNumberRepository authNumberRepository;
    private final AlarmRepository alarmRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final TokenPairRepository tokenPairRepository;

    /**
    *   작성날짜: 2022/05/16 11:57 AM
    *   작성자: 이승범
    *   작성내용: 사용자 기본정보(id, nickname, auth) 반환
    */
    public UserInfoResponse findUserInfo(Long id) {
        User findUser = userRepository.findById(id)
                .orElseThrow(() -> new UserException(UserErrorResult.NOT_VALID_TOKEN));
        return findUser.getUserInfo();
    }

    /**
    *   작성날짜: 2022/05/16 11:57 AM
    *   작성자: 이승범
    *   작성내용: 비밀번호 변경
    */
    public User updatePassword(Long id, UpdatePasswordRequest request) {

        User findUser = userRepository.findById(id)
                .orElseThrow(() -> new UserException(UserErrorResult.NOT_VALID_TOKEN));

        if (!encoder.matches(request.getOriginPwd(), findUser.getPassword())) {
            throw new SignupException(SignupErrorResult.NOT_MATCH_PWD);
        } else if (!request.getNewPwd().equals(request.getNewPwdCheck())) {
            throw new SignupException(SignupErrorResult.NOT_MATCH_PWDCHECK);
        }

        findUser.changePassword(encoder.encode(request.getNewPwd()));

        return findUser;
    }

    // 회원가입 학부모, 교사 공통 로직(유효성 검사 및 비밀번호 해싱)
    public String signupValidation(String password, String passwordCheck, String loginId, String phoneNum, String nickName) {

        // 비밀번호 확인
        if (!password.equals(passwordCheck)) {
            throw new SignupException(SignupErrorResult.NOT_MATCH_PWDCHECK);
        }

        // 로그인 아이디, 닉네임 중복확인
        User duplicatedUser = userRepository.findByLoginIdOrNickName(loginId, nickName).orElse(null);
        if (duplicatedUser != null) {
            throw new SignupException(SignupErrorResult.DUPLICATED_NICKNAME);
        }

        // 핸드폰 인증확인
        AuthNumber authComplete = authNumberRepository.findAuthComplete(phoneNum, AuthKind.signup)
                .orElseThrow(() -> new AuthNumberException(AuthNumberErrorResult.NOT_AUTHENTICATION));

        // 핸드폰 인증후 너무 많은 시간이 지났으면 인증 무효
        if (Duration.between(authComplete.getAuthTime(), LocalDateTime.now()).getSeconds() > (60 * 60)) {
            throw new AuthNumberException(AuthNumberErrorResult.EXPIRED);
        }

        return encoder.encode(password);
    }

    public Slice<AlarmDto> findUserActiveAlarm(Long userId, Pageable pageable) {
        Slice<Alarm> alarmSlice = alarmRepository.findActiveByUser(userId, pageable);
        return new SliceImpl<>(alarmSlice.stream()
                .map(Alarm::exportAlarm)
                .collect(Collectors.toList()),
                pageable, alarmSlice.hasNext());
    }

    public Slice<AlarmDto> findPresentationActiveAlarm(Long userId, Pageable pageable) {
        Slice<Alarm> alarmSlice = alarmRepository.findPresentationByUser(userId, pageable);
        return new SliceImpl<>(alarmSlice.stream()
                .map(Alarm::exportAlarm)
                .collect(Collectors.toList()),
                pageable, alarmSlice.hasNext());
    }

    public Integer deleteUserAlarm(Long userId, List<Long> alarmIds) {
        return alarmRepository.deleteByIds(userId, alarmIds);
    }


    /**
     *   작성날짜: 2022/07/29 01:32 AM
     *   작성자: 이승범
     *   작성내용: login service layer로 옮김
     */
    public LoginResponse login(LoginRequest request) {
        // authenticationManager 이용한 아이디 및 비밀번호 확인
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getLoginId(), request.getPassword()));

        // 인증된 객체 생성
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();

        String jwt = jwtUtils.createAccessToken(authentication);
        String refresh = jwtUtils.createRefreshToken(authentication);
        TokenPair tokenPair = TokenPair.createTokenPair(jwt, refresh, principal.getUser());

        // 기존 토큰이 있으면 수정, 없으면 생성
        tokenPairRepository.findByUserId(principal.getUser().getId())
                .ifPresentOrElse(
                        (findTokenPair) -> findTokenPair.updateToken(jwt, refresh),
                        () -> tokenPairRepository.save(tokenPair)
                );

        LoginResponse response = principal.getUser().getLoginInfo();
        response.setAccessToken(jwtUtils.addPrefix(jwt));
        response.setRefreshToken(jwtUtils.addPrefix(refresh));
        return response;
    }

    /**
     *   작성날짜: 2022/07/29 01:32 AM
     *   작성자: 이승범
     *   작성내용: refreshToken으로 accessToken를 재발급
     */
    public LoginResponse refresh(TokenRefreshRequest request) {

        String requestRefreshTokenToken = request.getRefreshToken().replace("Bearer ", "");

        // 요청으로 받은 refreshToken 유효한지 확인
        jwtUtils.validateToken(requestRefreshTokenToken);

        // 이전에 받았던 refreshToken과 일치하는지 확인(tokenPair 유저당 하나로 유지)
        Long userId = jwtUtils.getUserIdFromToken(requestRefreshTokenToken);
        TokenPair findTokenPair = tokenPairRepository.findByUserIdWithUser(userId)
                .orElseThrow(() -> new JWTVerificationException("유효하지 않은 토큰입니다."));

        if (!requestRefreshTokenToken.equals(findTokenPair.getRefreshToken())) {
            throw new JWTVerificationException("중복 로그인 되었습니다.");
        }

        // 이전에 발급했던 AccessToken 만료되지 않았다면 refreshToken 탈취로 판단
        // TokenPair 삭제 -> 다시 로그인 해야됨
        if (jwtUtils.isExpired(findTokenPair.getAccessToken())) {
            // refreshToken 유효하고, AccessToken 정상적으로 Expired 상태일때
            PrincipalDetails principal = new PrincipalDetails(findTokenPair.getUser());
            Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

            String jwt = jwtUtils.createAccessToken(authentication);
            String refresh = jwtUtils.createRefreshToken(authentication);
            findTokenPair.updateToken(jwt, refresh);

            LoginResponse response = principal.getUser().getLoginInfo();
            response.setAccessToken(jwtUtils.addPrefix(jwt));
            response.setRefreshToken(jwtUtils.addPrefix(refresh));
            return response;

        } else {
            // accessToken이 아직 만료되지 않은 상태 -> 토큰 탈취로 판단 -> delete tokenPair
            tokenPairRepository.delete(findTokenPair);
            return null;
        }
    }

    /**
    *   작성날짜: 2022/07/29 1:50 PM
    *   작성자: 이승범
    *   작성내용: 로그인아이디 중복 확인
    */
    public void checkLoginId(String loginId) {
        userRepository.findByLoginId(loginId)
                .ifPresent((user)->{
                    throw new UserException(UserErrorResult.ALREADY_LOGINID_EXIST);
                });
    }

    /**
    *   작성날짜: 2022/07/29 5:05 PM
    *   작성자: 이승범
    *   작성내용: 닉네임 중복 확인
    */
    public void checkNickname(String nickname) {
        userRepository.findByNickName(nickname)
                .ifPresent((user)->{
                    throw new UserException(UserErrorResult.ALREADY_NICKNAME_EXIST);
                });
    }
}
