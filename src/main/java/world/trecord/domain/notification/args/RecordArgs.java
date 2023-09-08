package world.trecord.domain.notification.args;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RecordArgs {
    private Long recordId;
    private String recordTitle;
}
