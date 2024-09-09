package mk.frizer.service.impl;

import jakarta.transaction.Transactional;
import mk.frizer.domain.*;
import mk.frizer.domain.dto.*;
import mk.frizer.domain.enums.Role;
import mk.frizer.domain.events.SalonCreatedEvent;
import mk.frizer.domain.events.SalonUpdatedEvent;
import mk.frizer.domain.exceptions.CityNotFoundException;
import mk.frizer.domain.exceptions.SalonNotFoundException;
import mk.frizer.domain.exceptions.TagNotFoundException;
import mk.frizer.domain.exceptions.UserNotFoundException;
import mk.frizer.repository.*;
import mk.frizer.service.SalonService;
import mk.frizer.utilities.DistanceCalculator;
import mk.frizer.utilities.SalonAdapter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SalonServiceImpl implements SalonService {
    private final SalonRepository salonRepository;
    private final BusinessOwnerRepository businessOwnerRepository;
    private final EmployeeRepository employeeRepository;
    private final TagRepository tagRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DistanceCalculator distanceCalculator;
    private final CityRepository cityRepository;
    private final BaseUserRepository userRepository;

    public SalonServiceImpl(SalonRepository salonRepository, BusinessOwnerRepository businessOwnerRepository, EmployeeRepository employeeRepository, TagRepository tagRepository, ApplicationEventPublisher applicationEventPublisher, DistanceCalculator distanceCalculator, CityRepository cityRepository, BaseUserRepository userRepository) {
        this.salonRepository = salonRepository;
        this.businessOwnerRepository = businessOwnerRepository;
        this.employeeRepository = employeeRepository;
        this.tagRepository = tagRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.distanceCalculator = distanceCalculator;
        this.cityRepository = cityRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Salon> getSalons() {
        return salonRepository.findAll();
    }

    @Override
    public Optional<Salon> getSalonById(Long id) throws SalonNotFoundException {
        Salon salon = salonRepository.findById(id).orElseThrow(SalonNotFoundException::new);
        return Optional.of(salon);
    }

    @Override
    @Transactional
    public Optional<Salon> createSalon(SalonAddDTO salonAddDTO) {
        BusinessOwner businessOwner = businessOwnerRepository.findById(salonAddDTO.getBusinessOwnerId()).orElseThrow(UserNotFoundException::new);
        City city = cityRepository.findByName(salonAddDTO.getCity())
                .orElseThrow(CityNotFoundException::new);

        Salon salon = new Salon(salonAddDTO.getName(), salonAddDTO.getDescription(), salonAddDTO.getLocation(), city, salonAddDTO.getPhoneNumber(), businessOwner, salonAddDTO.getLatitude(), salonAddDTO.getLongitude());

        salonRepository.save(salon);

        applicationEventPublisher.publishEvent(new SalonCreatedEvent(salon));
        return Optional.of(salon);
    }

    @Override
    @Transactional
    public Optional<Salon> updateSalon(Long id, SalonUpdateDTO salonUpdateDTO) {
        Salon salon = getSalonById(id).orElseThrow(SalonNotFoundException::new);

        salon.setName(salonUpdateDTO.getName());
        salon.setDescription(salonUpdateDTO.getDescription());
        salon.setLocation(salonUpdateDTO.getLocation());
        salon.setPhoneNumber(salonUpdateDTO.getPhoneNumber());
        salon.setLatitude(salonUpdateDTO.getLatitude());
        salon.setLongitude(salonUpdateDTO.getLongitude());
        salonRepository.save(salon);

        applicationEventPublisher.publishEvent(new SalonUpdatedEvent(salon));

        return Optional.of(salon);
    }

    @Override
    @Transactional
    public Optional<Salon> deleteSalonById(Long id) {
        Salon salon = getSalonById(id).orElseThrow(SalonNotFoundException::new);
        salonRepository.deleteById(id);
        return Optional.of(salon);
    }

    @Override
    @Transactional
    public Optional<Salon> addTagToSalon(TagAddDTO tagAddDTO) {
        Salon salon = getSalonById(tagAddDTO.getSalonId()).get();
        Tag tag = tagRepository.findById(tagAddDTO.getTagId()).orElseThrow(TagNotFoundException::new);
        salon.getTags().add(tag);
        return Optional.of(salonRepository.save(salon));
    }

    @Override
    @Transactional
    public Optional<Salon> addTreatmentToSalon(Treatment treatment) {
        Salon salon = getSalonById(treatment.getSalon().getId()).get();
        salon.getSalonTreatments().add(treatment);
        salonRepository.save(salon);
        return Optional.of(salon);
    }

    @Override
    @Transactional
    public Optional<Salon> editTreatmentForSalon(Treatment treatment) {
        Salon salon = getSalonById(treatment.getSalon().getId()).get();

        salon.setSalonTreatments(salon.getSalonTreatments().stream().map(item -> {
            if (item.getId().equals(treatment.getId())) {
                return treatment;
            }
            return item;
        }).collect(Collectors.toList()));
        salonRepository.save(salon);
        return Optional.empty();
    }

    @Override
    public Optional<Salon> addReview(Review review) {
        Salon salon = review.getEmployee().getSalon();
        salon.addReview(review.getRating());
        return Optional.of(salonRepository.save(salon));
    }

    @Override
    public Optional<Salon> deleteReview(Review review) {
        Salon salon = review.getEmployee().getSalon();
        salon.deleteReview(review.getRating());
        return Optional.of(salonRepository.save(salon));
    }

    @Override
    public Optional<Salon> updateReview(Review review, Double oldRating) {
        Salon salon = review.getEmployee().getSalon();
        salon.updateReview(review.getRating(), oldRating);
        return Optional.of(salonRepository.save(salon));
    }

    @Override
    public List<Salon> filterSalons(String name, String city, Float distance, Float rating, String userLocation) {
        List<Salon> salonByName = salonRepository.findAll()
                .stream().filter(salon -> salon.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
        List<Salon> salonsByRating = salonRepository.findAllByRatingGreaterThanEqual(rating);
        List<Salon> salonsByLocation = new ArrayList<>();
        if (!city.equals("Цела Македонија")) {
            Optional<City> city1 = cityRepository.findByNameEqualsIgnoreCase(city);
            if (city1.isPresent()) {
                salonsByLocation = city1.get().getSalonsInCity();
            }
        } else {
            salonsByLocation = this.getSalons();
        }
        List<Salon> salonsByDistance = this
                .getSalons()
                .stream()
                .filter(salon -> distance >= distanceCalculator.getDistance(userLocation, salon.getLatitude(), salon.getLongitude()))
                .toList();

        List<Salon> interceptSalons = new ArrayList<>(salonByName);
        interceptSalons.retainAll(salonsByRating);
        interceptSalons.retainAll(salonsByLocation);
        interceptSalons.retainAll(salonsByDistance);
        return interceptSalons;
    }

    @Override
    public List<String> getSalonsAsString(List<Salon> salons) {
        return salons.stream()
                .map(SalonAdapter::convertToString)
                .collect(Collectors.toList());
    }

    @Override
    public String getSalonAsString(Salon salon) {
        return SalonAdapter.convertToString(salon);
    }

    @Override
    public boolean isUserAuthorizedToAddTreatment(Long id, String userEmail) {
        Salon salon = salonRepository.findById(id).orElseThrow(SalonNotFoundException::new);
        BaseUser user = userRepository.findByEmail(userEmail).orElseThrow(UserNotFoundException::new);

        if (user.getRoles().contains(Role.ROLE_OWNER) && salon.getOwner().getBaseUser().getEmail().equals(userEmail)) {
            return true;
        }
        if (user.getRoles().contains(Role.ROLE_EMPLOYEE)) {
            for (Employee employee : salon.getEmployees()) {
                if (employee.getBaseUser().getEmail().equals(userEmail)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isUserAuthorizedToAddSalon(Long id, String email) {
        Salon salon = salonRepository.findById(id).orElseThrow(SalonNotFoundException::new);
        BaseUser user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);

        if (user.getRoles().contains(Role.ROLE_OWNER) && salon.getOwner().getBaseUser().getEmail().equals(email)) {
            return true;
        }
        return false;
    }

}

