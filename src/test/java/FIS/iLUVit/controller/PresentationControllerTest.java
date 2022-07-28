package FIS.iLUVit.controller;

import FIS.iLUVit.config.argumentResolver.LoginUserArgumentResolver;
import FIS.iLUVit.controller.dto.PresentationRequestRequestFormDto;
import FIS.iLUVit.controller.dto.PresentationSaveResponseDto;
import FIS.iLUVit.controller.dto.PtDateRequestDto;
import FIS.iLUVit.domain.Parent;
import FIS.iLUVit.domain.Presentation;
import FIS.iLUVit.domain.PtDate;
import FIS.iLUVit.domain.User;
import FIS.iLUVit.domain.enumtype.Auth;
import FIS.iLUVit.exception.PresentationErrorResult;
import FIS.iLUVit.exception.PresentationException;
import FIS.iLUVit.exception.UserErrorResult;
import FIS.iLUVit.exception.UserException;
import FIS.iLUVit.exception.exceptionHandler.ErrorResponse;
import FIS.iLUVit.exception.exceptionHandler.controllerAdvice.GlobalControllerAdvice;
import FIS.iLUVit.service.PresentationService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static FIS.iLUVit.Creator.createPtDate;
import static FIS.iLUVit.Creator.createValidPresentation;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PresentationControllerTest {

    @Mock
    PresentationService presentationService;

    @InjectMocks
    PresentationController target;

    MockMvc mockMvc;
    ObjectMapper objectMapper;

    User user;

    @BeforeEach
    void init(){
        // Controller
        mockMvc = MockMvcBuilders.standaloneSetup(target)
                .setCustomArgumentResolvers(new LoginUserArgumentResolver())
                .setControllerAdvice(GlobalControllerAdvice.class)
                .build();

        objectMapper = new ObjectMapper();

        user = Parent.builder()
                .id(1L)
                .auth(Auth.PARENT)
                .build();

    }

    public String createJwtToken(){
        return JWT.create()
                .withSubject("JWT")
                .withExpiresAt(new Date(System.currentTimeMillis() + (60000 * 60 * 3))) // JWT 만료시간 밀리세컨단위
                .withClaim("id", user.getId())
                .sign(Algorithm.HMAC512("symmetricKey"));
    }

    @Nested
    @DisplayName("설명회 저장")
    class 설명회저장{

        PtDateRequestDto ptDateRequestDto1;
        PtDateRequestDto ptDateRequestDto2;
        PtDateRequestDto ptDateRequestDto3;
        List<PtDateRequestDto> dtoList = new ArrayList<>();
        PresentationRequestRequestFormDto request;
        MultipartFile multipartFile;
        List<MultipartFile> multipartFileList = new ArrayList<>();

        @BeforeEach
        void init() throws IOException {
            ptDateRequestDto1 = new PtDateRequestDto(LocalDate.now(), "test time", 10);
            ptDateRequestDto2 = new PtDateRequestDto(LocalDate.now(), "test time", 10);
            ptDateRequestDto3 = new PtDateRequestDto(LocalDate.now(), "test time", 10);
            dtoList.add(ptDateRequestDto1);
            dtoList.add(ptDateRequestDto2);
            dtoList.add(ptDateRequestDto3);
            request = new PresentationRequestRequestFormDto(1L, LocalDate.now(), LocalDate.now(), "test place", "test content", dtoList);
            String name = "162693895955046828.png";
            Path path1 = Paths.get(new File("").getAbsolutePath() + '/' + name);
            byte[] content = Files.readAllBytes(path1);
            multipartFile = new MockMultipartFile(name, name, "image", content);
            multipartFileList.add(multipartFile);
            multipartFileList.add(multipartFile);
        }

        @Test
        @DisplayName("[error] 로그인을 하지 않은 경우")
        public void 로그인X() throws Exception {

            MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/presentation");

            MockMultipartFile requestDto = new MockMultipartFile("request", null,
                    "application/json", objectMapper.writeValueAsString(request).getBytes());

            //when
            ResultActions result = mockMvc.perform(
                    builder.file("images", multipartFile.getBytes())
                            .file(requestDto)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            );

            //then
            result.andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(content().json(objectMapper.writeValueAsString(
                            new ErrorResponse(HttpStatus.FORBIDDEN
                                    , "인증된 사용자가 아닙니다")
                    )));
        }

        @Test
        @DisplayName("[error] 잘못된 요청")
        public void 잘못된요청() throws Exception {
            MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/presentation");
            request = new PresentationRequestRequestFormDto();
            MockMultipartFile requestDto = new MockMultipartFile("request", null,
                    "application/json", objectMapper.writeValueAsString(request).getBytes());

            //when
            ResultActions result = mockMvc.perform(
                    builder.file("images", multipartFile.getBytes())
                            .file(requestDto)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .header(HttpHeaders.AUTHORIZATION, createJwtToken())
            );

            //then
            result.andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("[error] 존재 하지 않는 선생님")
        public void 존재하지않는선생님() throws Exception {
            MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/presentation");
            MockMultipartFile requestDto = new MockMultipartFile("request", null,
                    "application/json", objectMapper.writeValueAsString(request).getBytes());

            Mockito.doThrow(new UserException(UserErrorResult.USER_NOT_EXIST))
                    .when(presentationService).saveWithPtDate(any(PresentationRequestRequestFormDto.class), anyList(), any(Long.class));

            //when
            ResultActions result = mockMvc.perform(
                    builder.file("images", multipartFile.getBytes())
                            .file(requestDto)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .header(HttpHeaders.AUTHORIZATION, createJwtToken())
            );

            //then
            result.andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().json(objectMapper.writeValueAsString(
                            new ErrorResponse(UserErrorResult.USER_NOT_EXIST)
                    )));
        }

        @Test
        @DisplayName("[error] 이미 유효한 설명회 존재")
        public void 이미유효한설명회존재() throws Exception {
            MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/presentation");
            MockMultipartFile requestDto = new MockMultipartFile("request", null,
                    "application/json", objectMapper.writeValueAsString(request).getBytes());

            Mockito.doThrow(new PresentationException(PresentationErrorResult.ALREADY_PRESENTATION_EXIST))
                    .when(presentationService).saveWithPtDate(any(PresentationRequestRequestFormDto.class), anyList(), any(Long.class));

            //when
            ResultActions result = mockMvc.perform(
                    builder.file("images", multipartFile.getBytes())
                            .file(requestDto)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .header(HttpHeaders.AUTHORIZATION, createJwtToken())
            );

            //then
            result.andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().json(objectMapper.writeValueAsString(
                            new ErrorResponse(PresentationErrorResult.ALREADY_PRESENTATION_EXIST)
                    )));
        }

        @Test
        @DisplayName("[success] 설명회 저장 성공")
        public void 저장성공() throws Exception {
            MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/presentation");

            Presentation presentation = createValidPresentation();
            PtDate ptDate1 = createPtDate(1L);
            PtDate ptDate2 = createPtDate(2L);
            presentation.getPtDates().add(ptDate1);
            presentation.getPtDates().add(ptDate2);
            MockMultipartFile requestDto = new MockMultipartFile("request", null,
                    "application/json", objectMapper.writeValueAsString(request).getBytes());

            Mockito.doReturn(presentation)
                    .when(presentationService).saveWithPtDate(any(PresentationRequestRequestFormDto.class), anyList(), any(Long.class));

            //when
            ResultActions result = mockMvc.perform(
                    builder.file("images", multipartFile.getBytes())
                            .file(requestDto)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .header(HttpHeaders.AUTHORIZATION, createJwtToken())
            );

            //then
            result.andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().json(objectMapper.writeValueAsString(
                            new PresentationSaveResponseDto(presentation)
                    )));
        }


    }

}