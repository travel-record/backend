package world.trecord.domain.feedcontributor.args;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedContributorPermissionArgs {

    private FeedPermissionArgs feed;
    private RecordPermissionArgs record;

    public FeedContributorPermissionArgs() {
        this.feed = new FeedPermissionArgs();
        this.record = new RecordPermissionArgs();
    }
}
