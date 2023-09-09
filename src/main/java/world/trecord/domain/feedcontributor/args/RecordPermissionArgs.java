package world.trecord.domain.feedcontributor.args;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RecordPermissionArgs {
    private Boolean read = true;
    private Boolean write = true;
    private Boolean modify = true;
    private Boolean delete = true;
}
