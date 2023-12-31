package FIS.iLUVit.dto.scrap;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotEmpty;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ScrapDirRequest {
    @NotEmpty(message = "입력되지 않은 목록이 있습니다.")
    private String name;
}
