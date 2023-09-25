package world.trecord.dto.record.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.record.RecordEntity;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class RecordUpdateRequest {

    @NotBlank
    private String title;

    @NotNull
    private LocalDateTime date;

    @NotBlank
    private String place;

    @NotBlank
    private String latitude;

    @NotBlank
    private String longitude;

    @NotBlank
    private String feeling;

    @NotBlank
    private String weather;

    @NotBlank
    private String transportation;

    @NotBlank
    private String content;

    private String imageUrl;

    @Builder
    private RecordUpdateRequest(String title,
                                LocalDateTime date,
                                String place,
                                String longitude,
                                String latitude,
                                String feeling,
                                String weather,
                                String transportation,
                                String content,
                                String imageUrl) {
        this.title = title;
        this.date = date;
        this.place = place;
        this.longitude = longitude;
        this.latitude = latitude;
        this.feeling = feeling;
        this.weather = weather;
        this.transportation = transportation;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    public RecordEntity toUpdateEntity() {
        return RecordEntity.builder()
                .title(this.title)
                .date(this.date)
                .place(this.place)
                .latitude(this.latitude)
                .longitude(this.longitude)
                .feeling(this.feeling)
                .weather(this.weather)
                .transportation(this.transportation)
                .content(this.content)
                .imageUrl(this.imageUrl)
                .build();
    }
}
