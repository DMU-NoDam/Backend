package NoDam.Demo.trip.domain;

import NoDam.Demo.common.converter.StringListConverter;
import NoDam.Demo.common.domain.BaseEntity;
import NoDam.Demo.common.type.PriceType;
import NoDam.Demo.common.type.ScheduleType;
import NoDam.Demo.common.type.TripThemeType;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "trip")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@SQLDelete(sql = "UPDATE trip SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Trip extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30, nullable = false)
    private String name;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true, length = 36)
    private String uuid;

    // @Column(nullable = true)
    // private Long siteId;

    @Column(nullable = false)
    private int personCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 15)
    private ScheduleType scheduleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 15)
    private TripThemeType tripThemeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 15)
    private PriceType priceType;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private boolean isFixed;

    @Column(nullable = false)
    private Boolean isPlanning = false;

    // 사용자가 고른 region code 목록 (예: jp-tokyo). TripRequest는 일정 생성 후 삭제되므로 trip에도 남긴다.
    // 이 컬럼 추가 전에 만든 trip은 null → 빈 목록으로 읽힌다 (StringListConverter)
    @Convert(converter = StringListConverter.class)
    @Column(nullable = true)
    private List<String> regionCodes;

    @Builder
    public Trip(String name, Long userId, String uuid, int personCount,
                ScheduleType scheduleType, PriceType priceType,
                LocalDate startDate, LocalDate endDate, boolean isFixed, List<String> regionCodes) {
        this.name = name;
        this.userId = userId;
        this.uuid = uuid;
        this.personCount = personCount;
        this.scheduleType = scheduleType;
        this.priceType = priceType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isFixed = isFixed;
        this.regionCodes = regionCodes != null ? regionCodes : List.of();
    }

    public void updateFixed(boolean isFixed) {
        this.isFixed = isFixed;
    }

    public void updatePlanning(boolean isPlanning) {
        this.isPlanning = isPlanning;
    }

    public void updateTheme(TripThemeType themeType) {
        if(themeType != null && this.tripThemeType == null)
            this.tripThemeType = themeType;
    }

    public void updateInfo(String name, Integer personCount) {
        if (name != null && !name.isBlank()) this.name = name;
        if (personCount != null) this.personCount = personCount;
    }

}
