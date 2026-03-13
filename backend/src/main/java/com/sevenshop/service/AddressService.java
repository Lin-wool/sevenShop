package com.sevenshop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sevenshop.dto.AddressRequest;
import com.sevenshop.entity.Address;
import com.sevenshop.mapper.AddressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {

    @Autowired
    private AddressMapper addressMapper;

    @Transactional
    public Address createAddress(Long userId, AddressRequest request) {
        // 如果设置为默认地址，先取消其他默认地址
        if (request.getIsDefault() != null && request.getIsDefault() == 1) {
            clearDefaultAddress(userId);
        }

        Address address = new Address();
        address.setUserId(userId);
        address.setName(request.getName());
        address.setPhone(request.getPhone());
        address.setAddress(request.getAddress());
        address.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : 0);

        addressMapper.insert(address);
        return address;
    }

    @Transactional
    public Address updateAddress(Long userId, Long id, AddressRequest request) {
        Address address = addressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new RuntimeException("地址不存在");
        }

        // 如果设置为默认地址，先取消其他默认地址
        if (request.getIsDefault() != null && request.getIsDefault() == 1) {
            clearDefaultAddress(userId);
        }

        address.setName(request.getName());
        address.setPhone(request.getPhone());
        address.setAddress(request.getAddress());
        if (request.getIsDefault() != null) {
            address.setIsDefault(request.getIsDefault());
        }

        addressMapper.updateById(address);
        return address;
    }

    public void deleteAddress(Long userId, Long id) {
        Address address = addressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new RuntimeException("地址不存在");
        }
        addressMapper.deleteById(id);
    }

    public List<Address> getUserAddresses(Long userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId);
        wrapper.orderByDesc(Address::getIsDefault);
        return addressMapper.selectList(wrapper);
    }

    public Address getAddressById(Long userId, Long id) {
        Address address = addressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new RuntimeException("地址不存在");
        }
        return address;
    }

    public Address getDefaultAddress(Long userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId);
        wrapper.eq(Address::getIsDefault, 1);
        return addressMapper.selectOne(wrapper);
    }

    private void clearDefaultAddress(Long userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId);
        wrapper.eq(Address::getIsDefault, 1);
        List<Address> defaultAddresses = addressMapper.selectList(wrapper);
        for (Address address : defaultAddresses) {
            address.setIsDefault(0);
            addressMapper.updateById(address);
        }
    }
}
