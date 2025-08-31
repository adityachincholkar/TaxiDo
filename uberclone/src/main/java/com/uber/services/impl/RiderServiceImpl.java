package com.uber.services.impl;

import com.uber.entities.Driver;
import com.uber.entities.Ride;
import com.uber.entities.Rider;
import com.uber.entities.Role;
import com.uber.exceptions.ResourceNotFoundException;
import com.uber.mapper.RiderMapper;
import com.uber.payloads.*;
import com.uber.repositories.DriverRepo;
import com.uber.repositories.RideRepo;
import com.uber.repositories.RiderRepo;
import com.uber.repositories.RoleRepo;
import com.uber.services.RiderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class RiderServiceImpl implements RiderService {

    @Autowired
    private RiderRepo riderRepo;

    @Autowired
    private RoleRepo roleRepo;

    private final RiderMapper riderMapper;

    @Autowired
    private DriverRepo driverRepo;

    @Autowired
    private RideRepo rideRepo;

    @Autowired
    public RiderServiceImpl(RiderMapper riderMapper){
        this.riderMapper = riderMapper;
    }


    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public RiderResponseDto createRider(RiderRequestDto riderRequestDto) {
        Rider rider = riderMapper.RiderRequestDtoToRider(riderRequestDto);

        // Assign ROLE_RIDER to the new rider
        Role riderRole = roleRepo.findByName("ROLE_RIDER").orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName("ROLE_RIDER");
            return roleRepo.save(newRole);
        });
        rider.setRoles(Collections.singleton(riderRole));

        Rider createdRider = riderRepo.save(rider);
        return riderMapper.RiderToRiderResponseDto(createdRider);
    }

    @Override
    public List<RiderResponseDto> getAllRiders() {
        List<Rider> allRiders = riderRepo.findAll();
        return allRiders.stream()
                .map(riderMapper::RiderToRiderResponseDto)
                .toList();
    }

    @Override
    public RiderResponseDto findRider(String email) {
        Rider rider = riderRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Rider","RiderId" + email , 0));
        return riderMapper.RiderToRiderResponseDto(rider);
    }

    @Override
    public RiderResponseDto updateRider(RiderRequestDto riderRequestDto,String  userEmail) {

        Rider rider = riderRepo.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Rider","RiderId" + userEmail, 0));
        riderMapper.updateRiderFromDto(riderRequestDto, rider);
        Rider updated = riderRepo.save(rider);
        return riderMapper.RiderToRiderResponseDto(updated);
    }

    @Override
    public void deleteRider(String email) {
        Rider rider = riderRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Rider","RiderId" +email, 0));
        riderRepo.delete(rider);
    }

    @Override
    public List<NearbyDriverResponseDto> getNearByDrivers(Double longitude, Double latitude, int km) {
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeCoordinates().includeDistance().sortAscending().limit(10);

        Circle circle = new Circle(new Point(longitude, latitude),new Distance(km, Metrics.KILOMETERS));
        GeoResults<RedisGeoCommands.GeoLocation<String>> response = redisTemplate.opsForGeo().radius("places",circle,args);

        List<NearbyDriverResponseDto> getNearByDriverReponse = new ArrayList<>();
        response.getContent().stream().forEach((data)->{
            Driver driver = driverRepo.findByEmail(data.getContent().getName()).orElseThrow(()-> new ResourceNotFoundException("Driver", "with id" + data.getContent().getName(),0 ));
            getNearByDriverReponse.add(NearbyDriverResponseDto.builder().email(data
                    .getContent().getName()).
                    name(driver.getName()).
                    driverId(driver.getId()).
                    carModel(driver.getCarModel()).
                    distanceInKm(data.getDistance().getValue()).
                    location(new LocationDto(data.getContent().getPoint().getX(),data.getContent().getPoint().getY()))
                    .build());





        });
        return getNearByDriverReponse;
    }




}
