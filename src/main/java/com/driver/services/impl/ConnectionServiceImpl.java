package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ConnectionRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class ConnectionServiceImpl implements ConnectionService {
    @Autowired
    UserRepository userRepository2;
    @Autowired
    ServiceProviderRepository serviceProviderRepository2;
    @Autowired
    ConnectionRepository connectionRepository2;

    @Override
    public User connect(int userId, String countryName) throws Exception{
        User user = userRepository2.findById(userId).get();
        countryName = countryName.toUpperCase();

        if(user.getConnected()) throw new Exception("Already connected");
        else if (String.valueOf(user.getOriginalCountry().getCountryName()).equals(countryName)) {
            return user;
        }else if (!userIsSubscribed(user.getServiceProviderList(), countryName)) {
            throw new Exception("Unable to connect");
        }

        //Getting service provider having smallest id
        int serviceProviderId = getSmallestServiceProviderId(user.getServiceProviderList(), countryName);
        ServiceProvider serviceProvider = serviceProviderRepository2.findById(serviceProviderId).get();

        Connection connection = new Connection();
        connection.setServiceProvider(serviceProvider);
        connection.setUser(user);

        serviceProvider.getConnectionList().add(connection);
        user.getConnectionList().add(connection);
        /**********************NOT SURE***********************/
        user.setConnected(true);
        user.setMaskedIp(CountryName.valueOf(countryName).toCode());
        userRepository2.save(user);

        return user;
    }
    @Override
    public User disconnect(int userId) throws Exception {
        User user = userRepository2.findById(userId).get();

        if(!user.getConnected()) throw new Exception("Already disconnected");
        user.setConnected(false);
        user.setMaskedIp(null);
        userRepository2.save(user);

        return user;
    }
    @Override
    public User communicate(int senderId, int receiverId) throws Exception {
        User sender = userRepository2.findById(senderId).get();
        User receiver = userRepository2.findById(receiverId).get();

        //To communicate to the receiver, sender should be in the current country of the receiver.
        String senderCountry = String.valueOf(sender.getOriginalCountry().getCountryName());
        String receiverCountry = String.valueOf(receiver.getOriginalCountry().getCountryName());

        try {
            if(sender.getConnected()) senderCountry = getCountryUsingMaskedIp(sender.getMaskedIp());
            if(receiver.getConnected()) receiverCountry = getCountryUsingMaskedIp(receiver.getMaskedIp());
        }catch (Exception e) {
            throw new Exception("Cannot establish communication");
        }

        if(!senderCountry.equals(receiverCountry))
            sender = connect(senderId, receiverCountry);

        return sender;
    }

    private boolean userIsSubscribed(List<ServiceProvider> serviceProviderList, String countryName) {
        for (ServiceProvider serviceProvider: serviceProviderList) {
            for (Country country: serviceProvider.getCountryList()) {
                if (String.valueOf(country.getCountryName()).equals(countryName)) return true;
            }
        }

        return false;
    }

    private int getSmallestServiceProviderId(List<ServiceProvider> serviceProviderList, String countryName) {
        int serviceProviderId = Integer.MAX_VALUE;
        for (ServiceProvider serviceProvider: serviceProviderList) {
            for (Country country: serviceProvider.getCountryList()) {
                if (String.valueOf(country.getCountryName()).equals(countryName))
                    serviceProviderId = Math.min(serviceProviderId, serviceProvider.getId());
            }
        }

        return serviceProviderId;
    }

    private String getCountryUsingMaskedIp(String maskedIp) throws Exception{
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("001", "IND");
        hashMap.put("002", "USA");
        hashMap.put("003","AUS");
        hashMap.put("004", "CHI");
        hashMap.put("005", "JPN");
        return hashMap.get(maskedIp);
    }
}
