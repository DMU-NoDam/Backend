package NoDam.Demo.trip.service;

import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripDeleteService {

    private final TripRepository tripRepository;

    // Trip은 @SQLDelete가 걸려 있어 delete 호출 시 soft delete(is_deleted=true)로 처리된다
    public void deleteTrip(Trip trip) {
        tripRepository.delete(trip);
    }

}
