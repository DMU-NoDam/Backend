package NoDam.Demo.trip.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.domain.TripMember;
import NoDam.Demo.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripSelectService {

    private final TripRepository tripRepository;
    private final TripMemberService tripMemberService; // 같은 trip domain 내부 service, 멤버십 기반 인가/조회에 사용

    // 내가 참여 중인(OWNER/MEMBER 무관) 여행 목록, 여행 시작일(startDate) 순 정렬
    public List<Trip> getTripList(Long userId) {
        List<Long> tripIds = tripMemberService.getMyMemberships(userId).stream()
                .map(TripMember::getTripId)
                .toList();

        return tripRepository.findAllById(tripIds).stream()
                .sorted(Comparator.comparing(Trip::getStartDate))
                .toList();
    }

    public Trip findById(Long tripId, Long userId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 작성자(trip.userId) 여부가 아닌 여행 멤버(OWNER/MEMBER) 여부로 접근 권한을 판단한다
        if (!tripMemberService.isMember(tripId, userId))
            throw new CustomException(ErrorCode.NOT_AUTHOR);

        return trip;
    }

    // 초대 링크 미리보기(비로그인) 등, 멤버십 확인 없이 여행 자체만 필요할 때 사용
    public Trip findByIdPublic(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
    }

}
