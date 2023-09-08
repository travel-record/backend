package world.trecord.domain.notification.args;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FeedArgs {
    private Long feedId;
    private String feedName;
}
