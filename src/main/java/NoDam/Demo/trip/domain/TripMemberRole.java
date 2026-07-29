package NoDam.Demo.trip.domain;

// Trip(여행) 내에서의 역할. User.role(UserRole - 시스템 전역 권한)과는 별개의 개념이다.
public enum TripMemberRole {

    // 여행 생성자. 여행 삭제, 초대(지정/링크) 발급 등 관리 권한을 가진다. 나가기(leave) 불가, 삭제만 가능하다.
    OWNER,

    // 초대를 통해 참여한 사용자. 여행 조회/일정 참여는 가능하나 삭제·초대 권한은 없다. 나가기(leave)만 가능하다.
    MEMBER,

    ;

}
