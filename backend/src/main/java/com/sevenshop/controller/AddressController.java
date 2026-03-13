package com.sevenshop.controller;

import com.sevenshop.dto.AddressRequest;
import com.sevenshop.entity.Address;
import com.sevenshop.service.AddressService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/addresses")
@CrossOrigin(origins = "*")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @PostMapping
    public ResponseEntity<?> createAddress(@RequestBody AddressRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        try {
            Address address = addressService.createAddress(userId, request);
            return ResponseEntity.ok(address);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAddress(@PathVariable Long id, @RequestBody AddressRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        try {
            Address address = addressService.updateAddress(userId, id, request);
            return ResponseEntity.ok(address);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        try {
            addressService.deleteAddress(userId, id);
            return ResponseEntity.ok(Map.of("message", "删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getMyAddresses(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        try {
            List<Address> addresses = addressService.getUserAddresses(userId);
            return ResponseEntity.ok(addresses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAddress(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        try {
            Address address = addressService.getAddressById(userId, id);
            return ResponseEntity.ok(address);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/default")
    public ResponseEntity<?> getDefaultAddress(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        try {
            Address address = addressService.getDefaultAddress(userId);
            if (address == null) {
                return ResponseEntity.ok(null);
            }
            return ResponseEntity.ok(address);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
