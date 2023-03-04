package com.driver.services.impl;

import com.driver.model.Admin;
import com.driver.model.Country;
import com.driver.model.CountryName;
import com.driver.model.ServiceProvider;
import com.driver.repository.AdminRepository;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    AdminRepository adminRepository1;

    @Autowired
    ServiceProviderRepository serviceProviderRepository1;

    @Autowired
    CountryRepository countryRepository1;

    @Override
    public Admin register(String username, String password) {
        Admin admin = new Admin(username, password);
        adminRepository1.save(admin);
        return admin;
    }

    @Override
    public Admin addServiceProvider(int adminId, String providerName) {
        Admin admin = adminRepository1.findById(adminId).get();
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setName(providerName);
        serviceProvider.setAdmin(admin);

        admin.getServiceProviders().add(serviceProvider);
        adminRepository1.save(admin);

        return admin;
    }

    @Override
    public ServiceProvider addCountry(int serviceProviderId, String countryName) throws Exception{
        countryName = countryName.toUpperCase();
        if(!correctCountryName(countryName)) throw new Exception("Country not found");

        ServiceProvider serviceProvider = serviceProviderRepository1.findById(serviceProviderId).get();
        Country country = new Country();
        country.setCountryName(CountryName.valueOf(countryName));
        country.setCode(CountryName.valueOf(countryName).toCode());
        country.setServiceProvider(serviceProvider);

        serviceProvider.getCountryList().add(country);
        serviceProviderRepository1.save(serviceProvider);

        return serviceProvider;
    }

    private boolean correctCountryName(String countryName) {
        HashSet<String> set = new HashSet<>();
        set.add("IND");
        set.add("USA");
        set.add("AUS");
        set.add("CHI");
        set.add("JPN");

        if(!set.contains(countryName)) return false;
        return true;
    }
}
