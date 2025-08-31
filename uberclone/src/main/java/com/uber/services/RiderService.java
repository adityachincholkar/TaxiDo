package com.uber.services;

import com.uber.payloads.NearbyDriverResponseDto;
import com.uber.payloads.RideResponseDto;
import com.uber.payloads.RiderRequestDto;
import com.uber.payloads.RiderResponseDto;

import java.util.List;

public interface RiderService {

    RiderResponseDto createRider(RiderRequestDto riderRequestDto);

    List<RiderResponseDto> getAllRiders ();

    RiderResponseDto findRider(String email);

    RiderResponseDto updateRider(RiderRequestDto riderRequestDto,String userEmail);

    void deleteRider(String email);

    List<NearbyDriverResponseDto> getNearByDrivers(Double longitude,Double latitude, int km);


}
