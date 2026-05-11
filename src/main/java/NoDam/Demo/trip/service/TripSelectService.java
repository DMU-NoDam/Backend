package NoDam.Demo.trip.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TripSelectService {

    private final TripRepository tripRepository;

    public List<Trip> getTripList(Long userId) {
        return tripRepository.findAllByUserId(userId);
    }

    public Trip findById(Long tripId, Long userId) {
        Optional<Trip> trip = tripRepository.findById(tripId);

        if(trip.isEmpty())
            throw new CustomException(ErrorCode.NOT_FOUND);

        if(!trip.get().getUserId().equals(userId))
            throw new CustomException(ErrorCode.NOT_AUTHOR);

        return trip.get();
    }

}
