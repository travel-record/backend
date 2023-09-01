package world.trecord.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {

    @Column(name = "created_date_time")
    @CreatedDate
    private LocalDateTime createdDateTime;

    @Column(name = "modified_date_time")
    @LastModifiedDate
    private LocalDateTime modifiedDateTime;

    @Column(name = "deleted_date_time")
    private LocalDateTime deletedDateTime;
}
