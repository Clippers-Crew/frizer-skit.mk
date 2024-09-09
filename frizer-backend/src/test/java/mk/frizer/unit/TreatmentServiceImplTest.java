package mk.frizer.unit;

import mk.frizer.domain.Salon;
import mk.frizer.domain.Treatment;
import mk.frizer.domain.dto.*;
import mk.frizer.domain.events.*;
import mk.frizer.domain.exceptions.*;
import mk.frizer.repository.*;
import mk.frizer.service.impl.TreatmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TreatmentServiceImplTest {

    @Mock
    private TreatmentRepository treatmentRepository;

    @Mock
    private SalonRepository salonRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private TreatmentServiceImpl treatmentService;

    private Treatment treatment;
    private Salon salon;

    @BeforeEach
    void setUp() {
        salon = new Salon();
        treatment = new Treatment("Haircut", salon, 50.0, 1);
    }

    /**
     * Test retrieving all treatments.
     * Ensures that the service fetches all treatments from the repository.
     */
    @Test
    void testGetTreatments() {
        when(treatmentRepository.findAll()).thenReturn(List.of(treatment));

        List<Treatment> treatments = treatmentService.getTreatments();

        assertEquals(1, treatments.size());
        assertEquals("Haircut", treatments.get(0).getName());
        verify(treatmentRepository, times(1)).findAll();
    }

    /**
     * Test retrieving treatments by salon ID.
     * Verifies that the service filters treatments by the correct salon.
     */
    @Test
    void testGetTreatmentsForSalon() {
        when(treatmentRepository.findAll()).thenReturn(List.of(treatment));
        salon.setId(1L);

        List<Treatment> treatmentsForSalon = treatmentService.getTreatmentsForSalon(1L);

        assertEquals(1, treatmentsForSalon.size());
        assertEquals("Haircut", treatmentsForSalon.get(0).getName());
        verify(treatmentRepository, times(1)).findAll();
    }

    @Test
    void testCreateTreatment_SalonNotFound() {
        TreatmentAddDTO treatmentAddDTO = new TreatmentAddDTO("Haircut", 1L, 50.0, 1);
        when(salonRepository.findById(anyLong())).thenThrow(new SalonNotFoundException());

        assertThrows(SalonNotFoundException.class, () -> treatmentService.createTreatment(treatmentAddDTO));
    }

    /**
     * Test retrieving a treatment by ID.
     * Ensures the correct treatment is returned or an exception is thrown if not found.
     */
    @Test
    void testGetTreatmentById() {
        when(treatmentRepository.findById(1L)).thenReturn(Optional.of(treatment));

        Optional<Treatment> foundTreatment = treatmentService.getTreatmentById(1L);

        assertTrue(foundTreatment.isPresent());
        assertEquals("Haircut", foundTreatment.get().getName());
        verify(treatmentRepository, times(1)).findById(1L);
    }

    @Test
    void testGetTreatmentById_NotFound() {
        when(treatmentRepository.findById(anyLong())).thenThrow(new TreatmentNotFoundException());

        assertThrows(TreatmentNotFoundException.class, () -> treatmentService.getTreatmentById(1L));
    }

    /**
     * Test creating a new treatment.
     * Verifies the treatment is saved correctly and events are published.
     */
    @Test
    void testCreateTreatment() {
        TreatmentAddDTO treatmentAddDTO = new TreatmentAddDTO("Haircut", 1L, 50.0, 1);
        when(salonRepository.findById(1L)).thenReturn(Optional.of(salon));
        when(treatmentRepository.save(any(Treatment.class))).thenReturn(treatment);

        Optional<Treatment> createdTreatment = treatmentService.createTreatment(treatmentAddDTO);

        assertTrue(createdTreatment.isPresent());
        assertEquals("Haircut", createdTreatment.get().getName());
        verify(treatmentRepository, times(1)).save(any(Treatment.class));
        verify(applicationEventPublisher, times(1)).publishEvent(any(TreatmentCreatedEvent.class));
    }

    /**
     * Test updating a treatment by ID.
     * Ensures the treatment is updated and events are published.
     */
    @Test
    void testUpdateTreatment() {
        TreatmentUpdateDTO treatmentUpdateDTO = new TreatmentUpdateDTO("Updated Treatment", 60.0, 2);

        when(treatmentRepository.findById(1L)).thenReturn(Optional.of(treatment));
        when(treatmentRepository.save(any(Treatment.class))).thenReturn(treatment);

        Optional<Treatment> updatedTreatment = treatmentService.updateTreatment(1L, treatmentUpdateDTO);

        assertTrue(updatedTreatment.isPresent());
        assertEquals("Updated Treatment", updatedTreatment.get().getName());
        assertEquals(60.0, updatedTreatment.get().getPrice());
        assertEquals(2, updatedTreatment.get().getDurationMultiplier());
        verify(treatmentRepository, times(1)).save(any(Treatment.class));
        verify(applicationEventPublisher, times(1)).publishEvent(any(TreatmentUpdatedEvent.class));
    }

    /**
     * Test deleting a treatment by ID.
     * Ensures the treatment is deleted from the repository.
     */
    @Test
    void testDeleteTreatmentById() {
        when(treatmentRepository.findById(1L)).thenReturn(Optional.of(treatment));

        Optional<Treatment> deletedTreatment = treatmentService.deleteTreatmentById(1L);

        assertTrue(deletedTreatment.isPresent());
        verify(treatmentRepository, times(1)).deleteById(1L);
    }
}
