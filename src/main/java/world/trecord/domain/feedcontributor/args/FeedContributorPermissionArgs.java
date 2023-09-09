package world.trecord.domain.feedcontributor.args;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedContributorPermissionArgs {

    private FeedPermissionArgs feedPermission;
    private RecordPermissionArgs recordPermission;

    public FeedContributorPermissionArgs() {
        this.feedPermission = new FeedPermissionArgs();
        this.recordPermission = new RecordPermissionArgs();
    }
}
