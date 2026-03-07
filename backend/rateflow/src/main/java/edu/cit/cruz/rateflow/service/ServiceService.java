package edu.cit.cruz.rateflow.service;

import edu.cit.cruz.rateflow.entity.Service;
import edu.cit.cruz.rateflow.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
public class ServiceService {

    @Autowired
    private ServiceRepository serviceRepository;

    public Service createService(Service service){
        return serviceRepository.save(service);
    }

    public List<Service> getAllServices(){
        return serviceRepository.findAll();
    }

    public Optional<Service> getServiceById(Integer serviceId){
        return serviceRepository.findById(serviceId);
    }

    public Service updateService(Service service){
        return serviceRepository.save(service);
    }

    public void deleteService(Integer serviceId){
        serviceRepository.deleteById(serviceId);
    }

    public boolean exists(Integer serviceId){
        return serviceRepository.existsById(serviceId);
    }
}