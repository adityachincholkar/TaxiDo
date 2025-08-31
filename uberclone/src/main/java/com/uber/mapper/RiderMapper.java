package com.uber.mapper;

import com.uber.entities.Rider;
import com.uber.payloads.RiderRequestDto;
import com.uber.payloads.RiderResponseDto;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = PasswordEncoderMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RiderMapper {

    @Mapping(target = "rating", constant = "5.0")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "password", source = "password", qualifiedByName = "encode")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "rides", ignore = true)
    Rider RiderRequestDtoToRider(RiderRequestDto riderRequestDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "password", source = "password", qualifiedByName = "encode")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "rides", ignore = true)
    void updateRiderFromDto(RiderRequestDto riderRequestDto, @MappingTarget Rider rider);
    
    RiderResponseDto RiderToRiderResponseDto(Rider rider);
}

