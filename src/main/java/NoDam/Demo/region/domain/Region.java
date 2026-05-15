package NoDam.Demo.region.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "region")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // region 이름

    @Column(nullable = false, length = 15)
    private String code; // front를 위한 지역코드

    @Column
    private Double lat;

    @Column
    private Double lon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "super_id", nullable = true)
    private Region superRegion; // 상위 region, null이면 최상위

    @Builder
    public Region(Long id, String name, String code, Double lat, Double lon, Region superRegion) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.lat = lat;
        this.lon = lon;
        this.superRegion = superRegion;
    }

}
