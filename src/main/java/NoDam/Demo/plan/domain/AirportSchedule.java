package NoDam.Demo.plan.domain;

import NoDam.Demo.place.domain.Place;

import java.time.LocalTime;

// plan domain VO : 특정 날짜의 공항 방문 (공항 Place + 방문 시간)
public record AirportSchedule(Place airport, LocalTime time) {
}
