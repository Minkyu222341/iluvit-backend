package FIS.iLUVit.dto.scrap;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ScrapDirDetailRequest {
    @NotNull(message = "입력되지 않은 목록이 있습니다.")
    private Long scrapId;
    @NotEmpty(message = "입력되지 않은 목록이 있습니다.")
    private String dirName;

}
