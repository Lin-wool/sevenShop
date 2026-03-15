package com.sevenshop.controller;

import com.sevenshop.common.ApiResponse;
import com.sevenshop.common.BusinessException;
import com.sevenshop.dto.AddressRequest;
import com.sevenshop.entity.Address;
import com.sevenshop.service.AddressService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@CrossOrigin(origins = "*")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @PostMapping
    public ResponseEntity<ApiResponse<Address>> createAddress(@RequestBody AddressRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        Address address = addressService.createAddress(userId, request);
        return ResponseEntity.ok(ApiResponse.success(address));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Address>> updateAddress(@PathVariable Long id, @RequestBody AddressRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        Address address = addressService.updateAddress(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(address));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        addressService.deleteAddress(userId, id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Address>>> getMyAddresses(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        List<Address> addresses = addressService.getUserAddresses(userId);
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Address>> getAddress(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        Address address = addressService.getAddressById(userId, id);
        return ResponseEntity.ok(ApiResponse.success(address));
    }

    @GetMapping("/default")
    public ResponseEntity<ApiResponse<Address>> getDefaultAddress(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        Address address = addressService.getDefaultAddress(userId);
        return ResponseEntity.ok(ApiResponse.success(address));
    }
}
