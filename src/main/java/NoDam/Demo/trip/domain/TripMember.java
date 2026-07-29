package NoDam.Demo.trip.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Trip - User 간 멤버십(참여) 관계.
 * 관계 엔티티 성격(TripRequest, UserFixedTrip과 동일)이라 BaseEntity(soft delete)를 상속하지 않는다.
 * 나가기/강퇴는 실제 row 삭제(hard delete)로 처리한다.
 * - soft delete를 쓰면 unique(trip_id, user_id) 제약 때문에 나간 사용자가 재참여할 수 없게 되는 문제가 있다.
 */
@Entity
@Table(name = "trip_member",
        uniqueConstraints = @UniqueConstraint(name = "uk_trip_member_trip_user", columnNames = {"trip_id", "user_id"}),
        indexes = @Index(name = "idx_trip_member_user_id", columnList = "user_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TripMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trip_id", nullable = false)
    private Long tripId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TripMemberRole role;

    @Column(updatable = false)
    private LocalDateTime joinedAt;

    @Builder
    public TripMember(Long tripId, Long userId, TripMemberRole role) {
        this.tripId = tripId;
        this.userId = userId;
        this.role = role;
    }

    @PrePersist
    protected void onCreate() {
        this.joinedAt = LocalDateTime.now();
    }

    public boolean isOwner() {
        return this.role == TripMemberRole.OWNER;
    }

    // OWNER 나가기 시 위임 대상에게 호출 (기존 OWNER는 나가면서 row 자체가 삭제됨)
    public void promoteToOwner() {
        this.role = TripMemberRole.OWNER;
    }

}
