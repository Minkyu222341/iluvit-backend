package FIS.iLUVit.controller;

import FIS.iLUVit.config.argumentResolver.Login;
import FIS.iLUVit.controller.dto.*;
import FIS.iLUVit.repository.dto.PresentationPreviewDto;
import FIS.iLUVit.service.PresentationService;
import FIS.iLUVit.service.UserService;
import FIS.iLUVit.service.dto.ParentInfoForDirectorDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PresentationController {

    private final PresentationService presentationService;
    private final UserService userService;

    /**
     * 모달창으로 나오는 시설 정보 + 설명회 + 리뷰 정보가 나오는 곳에서 보여줄 설명회에 대한 내용 <p>
     * 현재날짜에 맞춰서 설명회 기간에 있으면 반환 그렇지 않으면 반환 하지않음 <p>
     * 내용 - 신청기간, 내용, 사진, 동영상, 신청할 수 있는 설명회 목록?
     */
    @GetMapping("/presentation/center/{center_id}")
    public List<PresentationResponseDto> findPresentationByCenterId(@PathVariable("center_id") Long centerId){
        return presentationService.findPresentationByCenterIdAndDate(centerId);
    }

    /**
     * 원장/ 선생의 presentation 등록 PtDate 설정하기
     * @return
     */
    @PostMapping(value = "/presentation", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public PresentationSaveResponseDto registerPresentation(@RequestPart PresentationRequestRequestFormDto request,
                                                            @RequestPart List<MultipartFile> images,
                                                            @Login Long userId){
        return new PresentationSaveResponseDto(presentationService.saveWithPtDate(request, images, userId));
    }

    /**
     * 원장, 선생의 설명회 수정
     */
    @PatchMapping(value = "/presentation", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public PresentationModifyResponseDto modifyPresentation(@RequestPart PresentationModifyRequestDto request,
                                                            @RequestPart List<MultipartFile> images,
                                                            @Login Long userId){
        return new PresentationModifyResponseDto(presentationService.modifyWithPtDate(request, images, userId));
    }

    /**
     * 원장 전용 설명회 자세히 보기 기능
     */
    @GetMapping("/presentation/{presentationId}/teacher")
    public void findMyCenterPresentation(@PathVariable("presentationId") Long presentationId, @Login Long userId){
        presentationService.findPresentationDetail(presentationId, userId);
    }

    /**
     * 원장의 시설 설명회 내역
     * @return
     */
    @GetMapping("/presentation/center/{centerId}/list")
    public List<PresentationPreviewDto> findMyCenterPresentationList(@PathVariable("centerId") Long centerId){
        return presentationService.findPresentationListByCenterId(centerId);
    }

    /**
     * 설명회를 신청한 사람들의 목록 반환 이름, 전화번호
     */
    @GetMapping("/presentation/ptDate/{ptDateId}/participating/parentList")
    public List<ParentInfoForDirectorDto> findParentParticipate(@Login Long userId, @PathVariable("ptDateId") Long ptDateId){
        return presentationService.findPtDateParticipatingParents(userId, ptDateId);
    }

    /**
     * 대기를 신청한 사람들의 목록 반환 이름, 전화번호
     */
    @GetMapping("/presentation/ptDate/{ptDateId}/waiting/parentList")
    public List<ParentInfoForDirectorDto> findParentWait(@Login Long userId, @PathVariable("ptDateId") Long ptDateId){
        return presentationService.findPtDateWaitingParents(userId, ptDateId);
    }
}
