package com.inkfront.logisticsApplication.service.impl;

import com.inkfront.logisticsApplication.domain.constants.ErrorMessages;
import com.inkfront.logisticsApplication.domain.entity.DeliveryAddress;
import com.inkfront.logisticsApplication.domain.entity.User;
import com.inkfront.logisticsApplication.dto.request.user.AddressRequestDTO;
import com.inkfront.logisticsApplication.dto.response.user.AddressDTO;
import com.inkfront.logisticsApplication.exception.BadRequestException;
import com.inkfront.logisticsApplication.exception.ResourceNotFoundException;
import com.inkfront.logisticsApplication.mapper.AddressMapper;
import com.inkfront.logisticsApplication.repository.DeliveryAddressRepository;
import com.inkfront.logisticsApplication.repository.UserRepository;
import com.inkfront.logisticsApplication.service.interfaces.AddressService;
import com.inkfront.logisticsApplication.service.interfaces.DistanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {

    private final DeliveryAddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;
    private final DistanceService distanceService;

    @Override
    public AddressDTO createAddress(String userId, AddressRequestDTO addressRequest) {
        log.info("Creating address for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        if (!validateAddress(addressRequest)) {
            throw new BadRequestException("Invalid address");
        }

        DeliveryAddress address = addressMapper.toEntity(addressRequest);
        address.setUser(user);

        // First address or explicitly set as default
        if (addressRequest.isDefault() || addressRepository.countByUserId(userId) == 0) {
            addressRepository.clearDefaultAddresses(userId);
            address.setDefault(true);
        }

        // Geocode if coordinates are missing
        if (address.getLatitude() == null || address.getLongitude() == null) {
            try {
                double[] coordinates = distanceService.geocodeAddressToCoordinates(address.getFullAddress());
                address.setLatitude(coordinates[0]);
                address.setLongitude(coordinates[1]);
            } catch (Exception e) {
                log.warn("Failed to geocode address: {}", e.getMessage());
            }
        }

        address = addressRepository.save(address);

        return addressMapper.toDTO(address);
    }

    @Override
    public AddressDTO updateAddress(String userId,
                                    String addressId,
                                    AddressRequestDTO addressRequest) {

        log.info("Updating address: {} for user: {}", addressId, userId);

        DeliveryAddress address = addressRepository
                .findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (!validateAddress(addressRequest)) {
            throw new BadRequestException("Invalid address");
        }

        addressMapper.updateAddressFromDTO(addressRequest, address);

        if (addressRequest.isDefault()) {
            addressRepository.clearDefaultAddresses(userId);
            address.setDefault(true);
        }

        address = addressRepository.save(address);

        return addressMapper.toDTO(address);
    }

    @Override
    public AddressDTO getAddressById(String userId,
                                     String addressId) {

        DeliveryAddress address = addressRepository
                .findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        return addressMapper.toDTO(address);
    }

    @Override
    public List<AddressDTO> getUserAddresses(String userId) {

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND);
        }

        List<DeliveryAddress> addresses =
                addressRepository.findByUserIdOrdered(userId);

        return addresses.stream()
                .map(addressMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AddressDTO getDefaultAddress(String userId) {

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND);
        }

        DeliveryAddress address = addressRepository
                .findDefaultAddressByUser(userId)
                .orElse(null);

        return address != null ? addressMapper.toDTO(address) : null;
    }

    @Override
    public void deleteAddress(String userId,
                              String addressId) {

        log.info("Deleting address: {} for user: {}", addressId, userId);

        DeliveryAddress address = addressRepository
                .findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        addressRepository.delete(address);
    }

    @Override
    public void setDefaultAddress(String userId, String addressId) {

        log.info("Setting default address: {} for user: {}", addressId, userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND);
        }

        DeliveryAddress address = addressRepository
                .findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        addressRepository.clearDefaultAddresses(userId);

        address.setDefault(true);

        addressRepository.save(address);
    }

    @Override
    public boolean validateAddress(AddressRequestDTO addressRequest) {

        if (addressRequest.getAddressLine1() == null ||
                addressRequest.getAddressLine1().isEmpty()) {
            return false;
        }

        if (addressRequest.getCity() == null ||
                addressRequest.getCity().isEmpty()) {
            return false;
        }

        if (addressRequest.getState() == null ||
                addressRequest.getState().isEmpty()) {
            return false;
        }

        return true;
    }

    @Override
    public AddressDTO geocodeAddress(String address) {

        try {
            double[] coordinates =
                    distanceService.geocodeAddressToCoordinates(address);

            AddressDTO addressDTO = new AddressDTO();
            addressDTO.setLatitude(coordinates[0]);
            addressDTO.setLongitude(coordinates[1]);

            return addressDTO;

        } catch (Exception e) {
            throw new BadRequestException(
                    "Failed to geocode address: " + e.getMessage()
            );
        }
    }
}