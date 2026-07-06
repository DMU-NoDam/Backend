package NoDam.Demo.adapter.google;

import NoDam.Demo.adapter.google.dto.GooglePlaceInfo;

import java.util.List;

public interface GooglePort {

    List<GooglePlaceInfo> searchByText(String hotelName);

    GooglePlaceInfo searchByGoogleId(String googleId);

}
