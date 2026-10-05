package NoDam.Demo.memo.domain;

import NoDam.Demo.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * 개인용 여행 메모. 여행(tripId)마다 여러 개를 만들 수 있고,
 * 같은 여행이라도 다른 사용자와 공유되지 않는다(작성자 본인만 접근 가능).
 */
@Entity
@Table(name = "memo", indexes = {
        @Index(name = "idx_memo_trip_user", columnList = "trip_id,user_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@SQLDelete(sql = "UPDATE memo SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Memo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trip_id", nullable = false)
    private Long tripId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemoIconType icon;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(length = 2000)
    private String content;

    @Builder
    public Memo(Long tripId, Long userId, MemoIconType icon, String title, String content) {
        this.tripId = tripId;
        this.userId = userId;
        this.icon = icon != null ? icon : MemoIconType.ETC;
        this.title = title;
        this.content = content;
    }

    // null은 "변경 없음", content는 빈 문자열("")을 보내면 내용을 비운다
    public void update(MemoIconType icon, String title, String content) {
        if (icon != null)
            this.icon = icon;
        if (title != null && !title.isBlank())
            this.title = title;
        if (content != null)
            this.content = content;
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }

}
