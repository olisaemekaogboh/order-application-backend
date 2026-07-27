package com.inkfront.logisticsApplication.service.interfaces;

import com.inkfront.logisticsApplication.dto.request.user.AddressRequestDTO;
import com.inkfront.logisticsApplication.dto.response.user.AddressDTO;

import java.util.List;

public interface AddressService {

    AddressDTO createAddress(String userId, AddressRequestDTO addressRequest);

    AddressDTO updateAddress(String addressId, AddressRequestDTO addressRequest);

    AddressDTO getAddressById(String addressId);

    List<AddressDTO> getUserAddresses(String userId);

    AddressDTO getDefaultAddress(String userId);

    void deleteAddress(String addressId);

    void setDefaultAddress(String userId, String addressId);

    boolean validateAddress(AddressRequestDTO addressRequest);

    AddressDTO geocodeAddress(String address);
}