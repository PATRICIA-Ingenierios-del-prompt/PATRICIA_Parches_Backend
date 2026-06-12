package ingprompt.patricia.parches.infrastructure.web.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class CreateInviteRequest {
    private UUID parcheId;
}
