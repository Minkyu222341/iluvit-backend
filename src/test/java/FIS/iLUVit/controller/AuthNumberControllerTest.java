package FIS.iLUVit.controller;

import FIS.iLUVit.config.argumentResolver.LoginUserArgumentResolver;
import FIS.iLUVit.domain.AuthNumber;
import FIS.iLUVit.domain.enumtype.AuthKind;
import FIS.iLUVit.exception.AuthNumberErrorResult;
import FIS.iLUVit.exception.AuthNumberException;
import FIS.iLUVit.exception.exceptionHandler.ErrorResponse;
import FIS.iLUVit.exception.exceptionHandler.controllerAdvice.GlobalControllerAdvice;
import FIS.iLUVit.service.AuthNumberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.result.StatusResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AuthNumberControllerTest {

    @Mock
    private AuthNumberService authNumberService;
    @InjectMocks
    private AuthNumberController target;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private final String phoneNum = "phoneNumber";

    @BeforeEach
    public void init() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(target)
                .setCustomArgumentResolvers(new LoginUserArgumentResolver())
                .setControllerAdvice(GlobalControllerAdvice.class)
                .build();
    }

    @Test
    public void 회원가입인증번호받기_실패_이미가입된번호() throws Exception {
        // given
        final String url = "/authNumber/signup";
        AuthNumberErrorResult error = AuthNumberErrorResult.ALREADY_PHONENUMBER_REGISTER;

        doThrow(new AuthNumberException(error))
                .when(authNumberService)
                .sendAuthNumberForSignup(phoneNum, AuthKind.signup);

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(url)
                        .param("phoneNumber", phoneNum)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(
                        content().json(objectMapper.writeValueAsString(
                                        new ErrorResponse(error.getHttpStatus(), error.getMessage())))
                );
    }

    @Test
    public void 인증번호받기_실패_유효시간남음() throws Exception {
        // given
        final String url = "/authNumber/signup";
        AuthNumberErrorResult error = AuthNumberErrorResult.YET_AUTHNUMBER_VALID;

        doThrow(new AuthNumberException(error))
                .when(authNumberService)
                .sendAuthNumberForSignup(phoneNum, AuthKind.signup);
        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(url)
                        .param("phoneNumber", phoneNum)
                        .contentType(MediaType.APPLICATION_JSON)
        );
        // then
        resultActions.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(
                        content().json(objectMapper.writeValueAsString(
                                new ErrorResponse(error.getHttpStatus(), error.getMessage())
                        ))
                );
    }

    @Test
    public void 인증번호받기_성공() throws Exception {
        // given
        final String url = "/authNumber/signup";

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(url)
                        .param("phoneNumber", phoneNum)
                        .contentType(MediaType.APPLICATION_JSON)
        );
        // then
        resultActions.andExpect(status().isOk());
    }


}
