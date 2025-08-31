package com.uber.mapper;

import com.uber.entities.Driver;
import com.uber.payloads.AvailableDto;
import com.uber.payloads.DriverRequestDto;
import com.uber.payloads.DriverResponseDto;
import org.mapstruct.*;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = PasswordEncoderMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DriverMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isAvailable", ignore = true)
    @Mapping(target = "rating", constant = "5.0")
    @Mapping(target = "password", source = "password", qualifiedByName = "encode")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "rides", ignore = true)
    Driver toDriver(DriverRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isAvailable", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "password", source = "password", qualifiedByName = "encode")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "rides", ignore = true)
    void updateDriverFromDto(DriverRequestDto dto, @MappingTarget Driver entity);

    DriverResponseDto toDriverResponseDto(Driver entity);

    AvailableDto toAvailableDto(Driver driver);



}

