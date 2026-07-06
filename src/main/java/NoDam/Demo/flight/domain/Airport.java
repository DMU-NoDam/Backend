package NoDam.Demo.flight.domain;

import NoDam.Demo.flight.service.AirportCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "airport")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Airport {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "iata_code", length = 3)
    private AirportCode iataCode; // IATA 공항 코드 (ex: NRT, HND, ICN)

    @Column(name = "place_id", nullable = false)
    private Long placeId; // cross-module: place 참조 (FK 없음)
}
