package mk.frizer.unit;

import mk.frizer.domain.*;
import mk.frizer.domain.dto.SalonAddDTO;
import mk.frizer.domain.dto.SalonUpdateDTO;
import mk.frizer.domain.exceptions.CityNotFoundException;
import mk.frizer.domain.exceptions.SalonNotFoundException;
import mk.frizer.domain.exceptions.UserNotFoundException;
import mk.frizer.repository.*;
import mk.frizer.service.impl.SalonServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalonServiceImplTest {

    @Mock
    private SalonRepository salonRepository;

    @Mock
    private BusinessOwnerRepository businessOwnerRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private SalonServiceImpl salonService;

    private Salon salon;
    private BusinessOwner businessOwner;
    private City city;

    @BeforeEach
    void setUp() {
        businessOwner = new BusinessOwner();
        city = new City("Skopje");
        salon = new Salon("Test Salon", "A great place", "Somewhere", city, "123456", businessOwner, 42.0f, 21.4f);
    }

    /**
     * Test retrieving a salon by ID.
     * Ensures the correct salon is returned, or an exception is thrown if not found.
     */
    @Test
    void testGetSalonById() {
        when(salonRepository.findById(1L)).thenReturn(Optional.of(salon));

        Optional<Salon> foundSalon = salonService.getSalonById(1L);

        assertTrue(foundSalon.isPresent());
        assertEquals("Test Salon", foundSalon.get().getName());
        verify(salonRepository, times(1)).findById(1L);
    }

    @Test
    void testGetSalonById_NotFound() {
        when(salonRepository.findById(anyLong())).thenThrow(new SalonNotFoundException());

        assertThrows(SalonNotFoundException.class, () -> salonService.getSalonById(1L));
    }

    /**
     * Test creating a new salon.
     * Ensures the salon is saved correctly in the repository.
     */
    @Test
    void testCreateSalon() {
        SalonAddDTO salonAddDTO = new SalonAddDTO("New Salon", "Great service", "Center", "Скопје","123456", 1L, 42.0f, 21.4f);

        when(businessOwnerRepository.findById(1L)).thenReturn(Optional.of(businessOwner));
        when(cityRepository.findByName("Скопје")).thenReturn(Optional.of(city));
        when(salonRepository.save(any(Salon.class))).thenReturn(salon);

        Optional<Salon> createdSalon = salonService.createSalon(salonAddDTO);

        assertTrue(createdSalon.isPresent());
        assertEquals("New Salon", createdSalon.get().getName());
        verify(salonRepository, times(1)).save(any(Salon.class));
        verify(applicationEventPublisher, times(1)).publishEvent(any());
    }

    @Test
    void testCreateSalon_UserNotFound() {
        SalonAddDTO salonAddDTO = new SalonAddDTO("New Salon", "Great service", "Center", "Скопје","123456", 1L, 42.0f, 21.4f);

        when(businessOwnerRepository.findById(1L)).thenThrow(new UserNotFoundException());

        assertThrows(UserNotFoundException.class, () -> salonService.createSalon(salonAddDTO));
    }

    @Test
    void testCreateSalon_CityNotFound() {
        SalonAddDTO salonAddDTO = new SalonAddDTO("New Salon", "Great service", "Center", "Скопје","123456", 1L, 42.0f, 21.4f);

        when(businessOwnerRepository.findById(1L)).thenReturn(Optional.of(businessOwner));
        when(cityRepository.findByName("Скопје")).thenThrow(new CityNotFoundException());

        assertThrows(CityNotFoundException.class, () -> salonService.createSalon(salonAddDTO));
    }

    /**
     * Test updating a salon.
     * Ensures the salon details are updated correctly.
     */
    @Test
    void testUpdateSalon() {
        SalonUpdateDTO salonUpdateDTO = new SalonUpdateDTO("Updated Salon", "New description", "New location", "654321", 43.0f, 22.0f);

        when(salonRepository.findById(1L)).thenReturn(Optional.of(salon));
        when(salonRepository.save(any(Salon.class))).thenReturn(salon);

        Optional<Salon> updatedSalon = salonService.updateSalon(1L, salonUpdateDTO);

        assertTrue(updatedSalon.isPresent());
        assertEquals("Updated Salon", updatedSalon.get().getName());
        verify(salonRepository, times(1)).save(any(Salon.class));
        verify(applicationEventPublisher, times(1)).publishEvent(any());
    }

    @Test
    void testUpdateSalon_NotFound() {
        SalonUpdateDTO salonUpdateDTO = new SalonUpdateDTO("Updated Salon", "New description", "New location", "654321", 43.0f, 22.0f);

        when(salonRepository.findById(1L)).thenThrow(new SalonNotFoundException());

        assertThrows(SalonNotFoundException.class, () -> salonService.updateSalon(1L, salonUpdateDTO));
    }

    /**
     * Test deleting a salon by ID.
     * Ensures the salon is deleted from the repository.
     */
    @Test
    void testDeleteSalonById() {
        when(salonRepository.findById(1L)).thenReturn(Optional.of(salon));

        Optional<Salon> deletedSalon = salonService.deleteSalonById(1L);

        assertTrue(deletedSalon.isPresent());
        verify(salonRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteSalonById_NotFound() {
        when(salonRepository.findById(anyLong())).thenThrow(new SalonNotFoundException());

        assertThrows(SalonNotFoundException.class, () -> salonService.deleteSalonById(1L));
    }
}
