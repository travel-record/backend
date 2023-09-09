package world.trecord.domain.feedcontributor.args;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FeedPermissionArgs {
    private Boolean read = false;
    private Boolean write = false;
    private Boolean modify = false;
    private Boolean delete = false;
}
