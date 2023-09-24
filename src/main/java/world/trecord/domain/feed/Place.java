package world.trecord.domain.feed;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@Data
public class Place {
    @Column(name = "place", columnDefinition = "varchar(30) COMMENT '장소 이름'")
    private String place;
    @Column(name = "latitude", columnDefinition = "varchar(30) COMMENT '위도'")
    private String latitude;
    @Column(name = "longitude", columnDefinition = "varchar(30) COMMENT '경도'")
    private String longitude;

    public static Place of(String place, String latitude, String longitude) {
        return Place.builder()
                .place(place)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }

    @Builder
    private Place(String place, String latitude, String longitude) {
        this.place = place;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
