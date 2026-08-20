-- 기존에 생성된 Trip에 대해 OWNER TripMember를 일괄 백필한다.
-- 실행 시점: TripMember 관련 코드 배포 후, ddl-auto=update로 애플리케이션을 한 번 기동해서
--           trip_member 테이블이 Hibernate에 의해 자동 생성된 이후에 수동으로 1회 실행한다.
-- 재실행 안전: 이미 등록된 (trip_id, user_id) 조합은 NOT EXISTS 조건으로 건너뛴다.
-- 삭제된(is_deleted = true) Trip은 대상에서 제외한다.

INSERT INTO trip_member (trip_id, user_id, role, joined_at)
SELECT t.id, t.user_id, 'OWNER', NOW()
FROM trip t
WHERE t.is_deleted = false
  AND NOT EXISTS (
      SELECT 1 FROM trip_member tm
      WHERE tm.trip_id = t.id
        AND tm.user_id = t.user_id
  );
