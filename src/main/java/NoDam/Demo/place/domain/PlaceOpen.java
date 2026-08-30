package NoDam.Demo.place.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 장소 요일별 영업시간 (분 단위). 한 장소에 요일·시간대(브레이크타임) 다중 행이 정상이므로
 * 자체 AUTO_INCREMENT id를 PK로 두고 place.id 참조(placeId)에는 일반 인덱스만 둔다.
 * (크롤측 google-open과 동일 구조. open/close, 테이블명이 예약어/하이픈이라 백틱 escape.)
 */
@Entity
@Table(name = "`place-open`")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PlaceOpen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long placeId; // place.id 참조

    @Column(nullable = false)
    private Integer day; //  일(0) ~ 토(6)

    @Column(name = "`open`")
    private Integer open; // 개점(분). null = 알 수 없음

    @Column(name = "`close`")
    private Integer close; // 폐점(분). null = 알 수 없음

    @Builder
    public PlaceOpen(Long placeId, Integer day, Integer open, Integer close) {
        this.placeId = placeId;
        this.day = day;
        this.open = open;
        this.close = close;
    }
}
