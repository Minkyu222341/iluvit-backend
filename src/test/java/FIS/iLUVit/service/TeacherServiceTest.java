package FIS.iLUVit.service;

import FIS.iLUVit.Creator;
import FIS.iLUVit.controller.dto.SignupTeacherRequest;
import FIS.iLUVit.controller.dto.TeacherDetailResponse;
import FIS.iLUVit.controller.dto.UpdateTeacherDetailRequest;
import FIS.iLUVit.domain.*;
import FIS.iLUVit.domain.alarms.CenterApprovalReceivedAlarm;
import FIS.iLUVit.domain.enumtype.Approval;
import FIS.iLUVit.domain.enumtype.Auth;
import FIS.iLUVit.domain.enumtype.AuthKind;
import FIS.iLUVit.domain.enumtype.BoardKind;
import FIS.iLUVit.event.AlarmEvent;
import FIS.iLUVit.exception.*;
import FIS.iLUVit.repository.*;
import FIS.iLUVit.service.createmethod.CreateTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static FIS.iLUVit.service.createmethod.CreateTest.createBoard;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TeacherServiceTest {

    @InjectMocks
    private TeacherService target;

    @Mock
    private AuthNumberService authNumberService;
    @Mock
    private UserService userService;
    @Mock
    private CenterRepository centerRepository;
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private AuthNumberRepository authNumberRepository;
    @Mock
    private BoardRepository boardRepository;
    @Mock
    private BookmarkRepository bookmarkRepository;
    @Mock
    private ScrapRepository scrapRepository;
    @Mock
    private ImageService imageService;


    private Center center1;
    private Center center2;
    private Teacher teacher1;
    private Teacher teacher2;
    private Teacher teacher3;
    private Teacher teacher4;
    private Teacher teacher5;
    private Board board1;
    private Board board2;
    private Board board3;
    private Board board4;
    private MockMultipartFile multipartFile;
    @BeforeEach
    public void init() throws IOException {
        center1 = CreateTest.createCenter(1L, "center1");
        center2 = CreateTest.createCenter(2L, "center2");
        teacher1 = Creator.createTeacher(3L, "teacher1", center1, Auth.DIRECTOR, Approval.ACCEPT);
        teacher2 = Creator.createTeacher(4L, "teacher2", center1, Auth.TEACHER, Approval.ACCEPT);
        teacher3 = Creator.createTeacher(5L, "teacher3", center1, Auth.TEACHER, Approval.WAITING);
        teacher4 = Creator.createTeacher(6L, "teacher4", null, Auth.TEACHER, null);
        teacher5 = Creator.createTeacher(7L, "teacher3", center1, Auth.TEACHER, Approval.REJECT);
        board1 = createBoard(8L, "자유게시판", BoardKind.NORMAL, null, true);
        board2 = createBoard(9L, "맛집게시판", BoardKind.NORMAL, null, true);
        board3 = createBoard(10L, "공지게시판", BoardKind.NORMAL, center1, true);
        board4 = createBoard(11L, "자유게시판", BoardKind.NORMAL, center1, true);
        String name = "162693895955046828.png";
        Path path = Paths.get(new File("").getAbsolutePath() + '/' + name);
        byte[] content = Files.readAllBytes(path);
        multipartFile = new MockMultipartFile(name, name, "image", content);
    }


    @Test
    public void 교사회원가입_실패_없는시설로등록() {
        // given
        center1.getTeachers().add(teacher1);
        center1.getTeachers().add(teacher2);
        center1.getTeachers().add(teacher3);
        center1.getTeachers().add(teacher5);
        SignupTeacherRequest request = SignupTeacherRequest.builder()
                .loginId("log")
                .password("password")
                .passwordCheck("password")
                .phoneNum("phoneNum")
                .nickname("nickName")
                .name("name")
                .emailAddress("asd@asd")
                .address("address")
                .detailAddress("detailAddress")
                .centerId(center1.getId())
                .build();
        doReturn(Optional.empty())
                .when(centerRepository)
                .findByIdWithTeacher(request.getCenterId());
        // when
        SignupException result = assertThrows(SignupException.class,
                () -> target.signup(request));
        // then
        assertThat(result.getErrorResult()).isEqualTo(SignupErrorResult.NOT_EXIST_CENTER);
    }

    @Test
    public void 교사회원가입_성공_시설선택O() {
        try (MockedStatic<AlarmUtils> alarmUtils = Mockito.mockStatic(AlarmUtils.class)) {
            // given
            center1.getTeachers().add(teacher1);
            center1.getTeachers().add(teacher2);
            center1.getTeachers().add(teacher3);
            center1.getTeachers().add(teacher5);
            SignupTeacherRequest request = SignupTeacherRequest.builder()
                    .loginId("loginId")
                    .password("password")
                    .passwordCheck("password")
                    .phoneNum("phoneNum")
                    .nickname("nickName")
                    .name("name")
                    .emailAddress("asd@asd")
                    .address("address")
                    .detailAddress("detailAddress")
                    .centerId(center1.getId())
                    .build();
            alarmUtils.when(() -> AlarmUtils.getMessage(any(String.class), any(Object[].class)))
                    .thenReturn("설명회가 가득 찼습니다");
            AlarmEvent alarmEvent = new AlarmEvent(new CenterApprovalReceivedAlarm(Teacher.builder().build()));
            alarmUtils.when(() -> AlarmUtils.publishAlarmEvent(new CenterApprovalReceivedAlarm(Teacher.builder().build())))
                    .thenReturn(alarmEvent);
            doReturn("hashedPwd")
                    .when(userService)
                    .signupValidation(request.getPassword(), request.getPasswordCheck(), request.getLoginId(), request.getPhoneNum(), request.getNickname());
            doReturn(Optional.of(center1))
                    .when(centerRepository)
                    .findByIdWithTeacher(request.getCenterId());
            doReturn(Arrays.asList(board1, board2))
                    .when(boardRepository)
                    .findDefaultByModu();
            // when
            Teacher result = target.signup(request);
            // then
            assertThat(result.getLoginId()).isEqualTo(request.getLoginId());
            assertThat(result.getCenter().getId()).isEqualTo(request.getCenterId());
            // verify
            verify(bookmarkRepository, times(2)).save(any(Bookmark.class));
            verify(centerRepository, times(1)).findByIdWithTeacher(request.getCenterId());
            verify(scrapRepository, times(1)).save(any(Scrap.class));
            verify(authNumberRepository, times(1)).deleteByPhoneNumAndAuthKind(request.getPhoneNum(), AuthKind.signup);
        }
    }

    @Test
    public void 교사회원가입_성공_시설선택X() {
        // given
        center1.getTeachers().add(teacher1);
        center1.getTeachers().add(teacher2);
        center1.getTeachers().add(teacher3);
        center1.getTeachers().add(teacher5);
        SignupTeacherRequest request = SignupTeacherRequest.builder()
                .loginId("loginId")
                .password("password")
                .passwordCheck("password")
                .phoneNum("phoneNum")
                .nickname("nickName")
                .name("name")
                .emailAddress("asd@asd")
                .address("address")
                .detailAddress("detailAddress")
                .centerId(null)
                .build();
        doReturn("hashedPwd")
                .when(userService)
                .signupValidation(request.getPassword(), request.getPasswordCheck(), request.getLoginId(), request.getPhoneNum(), request.getNickname());
        doReturn(new ArrayList<Board>())
                .when(boardRepository)
                .findDefaultByModu();
        // when
        Teacher result = target.signup(request);
        // then
        assertThat(result.getLoginId()).isEqualTo(request.getLoginId());
        assertThat(result.getCenter()).isNull();
        // verify
        verify(centerRepository, times(0)).findByIdWithTeacher(request.getCenterId());
        verify(bookmarkRepository, times(0)).save(any(Bookmark.class));
        verify(scrapRepository, times(1)).save(any(Scrap.class));
        verify(authNumberRepository, times(1)).deleteByPhoneNumAndAuthKind(request.getPhoneNum(), AuthKind.signup);
    }

    @Test
    public void 교사프로필조회_성공() throws IOException {
        // given
        doReturn(Optional.of(teacher1))
                .when(teacherRepository)
                .findById(teacher1.getId());
        TeacherDetailResponse response = new TeacherDetailResponse(teacher1);
        doReturn("imagePath")
                .when(imageService)
                .getProfileImage(teacher1);
        // when
        TeacherDetailResponse result = target.findDetail(teacher1.getId());
        // then
        assertThat(result.getNickname()).isEqualTo(response.getNickname());
        assertThat(result.getProfileImg()).isEqualTo("imagePath");
    }

    @Test
    public void 교사프로필수정_실패_닉네임중복() {
        // given
        UpdateTeacherDetailRequest request = UpdateTeacherDetailRequest.builder()
                .nickname("중복닉네임")
                .build();
        doReturn(Optional.of(teacher1))
                .when(teacherRepository)
                .findById(teacher1.getId());
        doReturn(Optional.of(Teacher.builder().build()))
                .when(teacherRepository)
                .findByNickName("중복닉네임");
        // when
        SignupException result = assertThrows(SignupException.class,
                () -> target.updateDetail(teacher1.getId(), request));
        // then
        assertThat(result.getErrorResult()).isEqualTo(SignupErrorResult.DUPLICATED_NICKNAME);
    }

    @Test
    public void 교사프로필수정_실패_핸드폰변경시미인증() {
        // given
        UpdateTeacherDetailRequest request = UpdateTeacherDetailRequest.builder()
                .name(teacher1.getName())
                .nickname(teacher1.getNickName())
                .changePhoneNum(true)
                .phoneNum("newPhoneNum")
                .emailAddress(teacher1.getEmailAddress())
                .address(teacher1.getAddress())
                .detailAddress(teacher1.getDetailAddress())
                .profileImg(multipartFile)
                .build();
        doReturn(Optional.of(teacher1))
                .when(teacherRepository)
                .findById(teacher1.getId());
        AuthNumberErrorResult error = AuthNumberErrorResult.NOT_AUTHENTICATION;
        doThrow(new AuthNumberException(error))
                .when(authNumberService)
                .validateAuthNumber(request.getPhoneNum(), AuthKind.updatePhoneNum);
        // when
        AuthNumberException result = assertThrows(AuthNumberException.class,
                () -> target.updateDetail(teacher1.getId(), request));
        // then
        assertThat(result.getErrorResult()).isEqualTo(AuthNumberErrorResult.NOT_AUTHENTICATION);
    }

    @Test
    public void 교사프로필수정_성공_핸드폰포함() throws IOException {
        // given
        UpdateTeacherDetailRequest request = UpdateTeacherDetailRequest.builder()
                .name(teacher1.getName())
                .nickname(teacher1.getNickName())
                .changePhoneNum(true)
                .phoneNum("newPhoneNum")
                .emailAddress(teacher1.getEmailAddress())
                .address(teacher1.getAddress())
                .detailAddress(teacher1.getDetailAddress())
                .profileImg(multipartFile)
                .build();
        doReturn(Optional.of(teacher1))
                .when(teacherRepository)
                .findById(teacher1.getId());
        // when
        TeacherDetailResponse response = target.updateDetail(teacher1.getId(), request);
        // then
        assertThat(response.getPhoneNumber()).isEqualTo("newPhoneNum");
    }

    @Nested
    @DisplayName("시설에 등록신청")
    class AssignCenter{

        @Test
        @DisplayName("[error] 이미 시설에 등록되있는경우")
        public void 등록된시설있음() {
            // given
            doReturn(Optional.empty())
                    .when(teacherRepository)
                    .findByIdAndNotAssign(teacher1.getId());
            // when
            SignupException result = assertThrows(SignupException.class,
                    () -> target.assignCenter(teacher1.getId(), center2.getId()));
            // then
            assertThat(result.getErrorResult()).isEqualTo(SignupErrorResult.ALREADY_BELONG_CENTER);
        }

        @Test
        @DisplayName("[success] 시설로의 등록신청")
        public void 시설등록신청_성공() {
            // given
            doReturn(Optional.of(teacher4))
                    .when(teacherRepository)
                    .findByIdAndNotAssign(teacher4.getId());
            doReturn(center2)
                    .when(centerRepository)
                    .getById(center2.getId());
            // when
            Teacher result = target.assignCenter(teacher4.getId(), center2.getId());
            // then
            assertThat(result.getCenter().getId()).isEqualTo(center2.getId());
            assertThat(result.getApproval()).isEqualTo(Approval.WAITING);
        }
    }

    @Nested
    @DisplayName("시설 스스로 탈주하기")
    class escapeCenter{
        @Test
        @DisplayName("[error] 사용자가 해당시설에 속해있지않음")
        public void 해당시설에속해있지않음() {
            // given
            doReturn(Optional.empty())
                    .when(teacherRepository)
                    .findByIdWithCenterWithTeacher(any());
            // when
            SignupException result = assertThrows(SignupException.class,
                    () -> target.escapeCenter(teacher2.getId()));

            // then
            assertThat(result.getErrorResult()).isEqualTo(SignupErrorResult.NOT_BELONG_CENTER);
        }

        @Test
        @DisplayName("[error] 일반교사가있는 시설에 마지막 원장의 탈주")
        public void 마지막원장탈주실패() {
            // given
            center1.getTeachers().add(teacher1);
            center1.getTeachers().add(teacher2);
            center1.getTeachers().add(teacher3);
            center1.getTeachers().add(teacher5);
            doReturn(Optional.of(teacher1))
                    .when(teacherRepository)
                    .findByIdWithCenterWithTeacher(any());
            // when
            SignupException result = assertThrows(SignupException.class,
                    () -> target.escapeCenter(teacher1.getId()));
            // then
            assertThat(result.getErrorResult()).isEqualTo(SignupErrorResult.HAVE_TO_MANDATE);
        }

        @Test
        @DisplayName("[success] 시설탈주 성공")
        public void 시설탈주성공() {
            // given
            center1.getTeachers().add(teacher1);
            center1.getTeachers().add(teacher2);
            center1.getTeachers().add(teacher3);
            center1.getTeachers().add(teacher5);
            doReturn(Optional.of(teacher2))
                    .when(teacherRepository)
                    .findByIdWithCenterWithTeacher(any());
            doReturn(List.of())
                    .when(boardRepository)
                    .findByCenter(teacher2.getCenter().getId());
            // when
            Teacher result = target.escapeCenter(teacher2.getId());
            // then
            assertThat(result.getId()).isEqualTo(teacher2.getId());
            assertThat(result.getCenter()).isNull();
            assertThat(result.getAuth()).isEqualTo(Auth.TEACHER);
        }
    }



}
