package world.trecord.domain.notification.args;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CommentArgs {
    private Long id;
    private Long parentId;
    private String content;
}
