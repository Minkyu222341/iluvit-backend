package FIS.iLUVit.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class ExpoTokenRequest {

    @NotNull(message = "토큰 값을 정확히 넣어주세요.")
    private String token;

    @NotNull(message = "동의 여부에는 true/false 만 허용합니다. null 값 X")
    private Boolean accept;
}