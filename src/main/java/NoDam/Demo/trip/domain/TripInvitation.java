package NoDam.Demo.trip.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 여행 초대 링크. 여행당 하나를 유지하며(token), 로그인한 사용자는 누구든 이 token으로
 * 수락 절차 없이 바로 여행에 참여(MEMBER)할 수 있다. 여러 명이 반복해서 참여 가능하다.
 * 관계 엔티티 성격이라 TripMember와 마찬가지로 BaseEntity(soft delete)를 상속하지 않는다.
 */
@Entity
@Table(name = "trip_invitation",
        uniqueConstraints = @UniqueConstraint(name = "uk_trip_invitation_token", columnNames = {"token"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TripInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trip_id", nullable = false)
    private Long tripId;

    @Column(name = "inviter_user_id", nullable = false)
    private Long inviterUserId;

    @Column(nullable = false, length = 36)
    private String token;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public TripInvitation(Long tripId, Long inviterUserId, String token) {
        this.tripId = tripId;
        this.inviterUserId = inviterUserId;
        this.token = token;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
