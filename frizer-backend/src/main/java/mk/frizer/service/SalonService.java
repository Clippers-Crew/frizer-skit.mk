package mk.frizer.service;

import mk.frizer.domain.Review;
import mk.frizer.domain.Salon;
import mk.frizer.domain.Treatment;
import mk.frizer.domain.dto.SalonAddDTO;
import mk.frizer.domain.dto.SalonUpdateDTO;
import mk.frizer.domain.dto.TagAddDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface SalonService {
    List<Salon> getSalons();

    Optional<Salon> getSalonById(Long id);

    Optional<Salon> createSalon(SalonAddDTO salonAddDTO);

    Optional<Salon> updateSalon(Long id, SalonUpdateDTO salonUpdateDTO);

    Optional<Salon> deleteSalonById(Long id);

    Optional<Salon> addTagToSalon(TagAddDTO tagAddDTO);

    Optional<Salon> addTreatmentToSalon(Treatment treatment);

    Optional<Salon> editTreatmentForSalon(Treatment treatment);

    Optional<Salon> addReview(Review review);
    Optional<Salon> deleteReview(Review review);
    Optional<Salon> updateReview(Review review, Double oldReview);

    List<Salon> filterSalons(String name, String city, Float distance, Float rating, String userLocation);

    List<String> getSalonsAsString(List<Salon> salons);

    String getSalonAsString(Salon salons);

    boolean isUserAuthorizedToAddTreatment(Long id, String name);

    boolean isUserAuthorizedToAddSalon(Long id, String email);
}
