package FIS.iLUVit.dto.post;

import FIS.iLUVit.domain.enumtype.Auth;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PostSearchRequest {
    private Long center_id;
    private String input;
    private Auth auth;
}