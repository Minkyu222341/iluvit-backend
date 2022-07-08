package FIS.iLUVit.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class CreateChatRoomRequest {
    @NotBlank(message = "메시지에 값을 채워주세요.")
    private String message;

    @NotBlank(message = "대화방 id 필요")
    private Long room_id;

    @NotBlank(message = "받는 사람 id 필요")
    private Long receiver_id;
}
