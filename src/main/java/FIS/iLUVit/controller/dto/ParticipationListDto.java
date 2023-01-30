package FIS.iLUVit.controller.dto;

import FIS.iLUVit.domain.*;
import FIS.iLUVit.domain.enumtype.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipationListDto {
    private Long parentId;
    private Long participantId;
    private Long waitingId;
    private Long ptDateId;
    private Long presentationId;
    private Long centerId;
    @DateTimeFormat(pattern = "yyyy-MM-dd", iso = DateTimeFormat.ISO.DATE)
    private LocalDate presentationDate;
    private String time;            // 설명회 날짜 시간
    private String centerProfileImage;
    private String place;               // 설명회 장소
    private String content;             // 설명회 내용
    private String centerName;
    private String tel;                     // 전화번호
    private String address;                 // 주소
    private String addressDetail;
    private Integer ablePersonNum;
    private Integer participantCnt;
    private Status status;

    public ParticipationListDto(Long parentId, Long participantId, Long ptDateId, Long presentationId, Long centerId, LocalDate presentationDate, String time, String centerProfileImage, String place, String content, String centerName, String tel, String address, String addressDetail, Integer ablePersonNum, Integer participantCnt, Status status) {
        this.parentId = parentId;
        this.participantId = participantId;
        this.ptDateId = ptDateId;
        this.presentationId = presentationId;
        this.centerId = centerId;
        this.presentationDate = presentationDate;
        this.time = time;
        this.centerProfileImage = centerProfileImage;
        this.place = place;
        this.content = content;
        this.centerName = centerName;
        this.tel = tel;
        this.address = address;
        this.addressDetail = addressDetail;
        this.ablePersonNum = ablePersonNum;
        this.participantCnt = participantCnt;
        this.status = status;
    }

    public ParticipationListDto(Long parentId, Long waitingId, Long ptDateId, Long presentationId, Long centerId, LocalDate presentationDate, String time, String centerProfileImage, String place, String content, String centerName, String tel, String address, String addressDetail, Integer ablePersonNum, Integer participantCnt) {
        this.parentId = parentId;
        this.waitingId = waitingId;
        this.ptDateId = ptDateId;
        this.presentationId = presentationId;
        this.centerId = centerId;
        this.presentationDate = presentationDate;
        this.time = time;
        this.centerProfileImage = centerProfileImage;
        this.place = place;
        this.content = content;
        this.centerName = centerName;
        this.tel = tel;
        this.address = address;
        this.addressDetail = addressDetail;
        this.ablePersonNum = ablePersonNum;
        this.participantCnt = participantCnt;
        this.status = Status.WAITING;
    }

    public static ParticipationListDto createDto(Participation participation) {
        PtDate ptDate = participation.getPtDate();
        Parent parent = participation.getParent();
        Presentation presentation = ptDate.getPresentation();
        Center center = presentation.getCenter();

        return ParticipationListDto.builder()
                .parentId(parent.getId())
                .participantId(participation.getId())
                .ptDateId(ptDate.getId())
                .presentationId(presentation.getId())
                .centerId(center.getId())
                .presentationDate(ptDate.getDate())
                .time(ptDate.getTime())
                .place(presentation.getPlace())
                .content(presentation.getContent())
                .centerName(center.getName())
                .tel(center.getTel())
                .address(center.getAddress())
                .addressDetail(center.getAddressDetail())
                .centerProfileImage(center.getProfileImagePath())
                .ablePersonNum(ptDate.getAblePersonNum())
                .participantCnt(ptDate.getParticipantCnt())
                .status(participation.getStatus())
                .build();
    }

    public static ParticipationListDto createDto(Waiting waiting){
        PtDate ptDate = waiting.getPtDate();
        Parent parent = waiting.getParent();
        Presentation presentation = ptDate.getPresentation();
        Center center = presentation.getCenter();

        return ParticipationListDto.builder()
                .parentId(parent.getId())
                .participantId(waiting.getId())
                .ptDateId(ptDate.getId())
                .presentationId(presentation.getId())
                .centerId(center.getId())
                .presentationDate(ptDate.getDate())
                .time(ptDate.getTime())
                .place(presentation.getPlace())
                .content(presentation.getContent())
                .centerName(center.getName())
                .centerProfileImage(center.getProfileImagePath())
                .tel(center.getTel())
                .address(center.getAddress())
                .addressDetail(center.getAddressDetail())
                .ablePersonNum(ptDate.getAblePersonNum())
                .participantCnt(ptDate.getParticipantCnt())
                .status(Status.WAITING)
                .build();
    }

}