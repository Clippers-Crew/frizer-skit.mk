package mk.frizer.unit;

import mk.frizer.domain.BaseUser;
import mk.frizer.domain.BusinessOwner;
import mk.frizer.domain.Salon;
import mk.frizer.domain.enums.Role;
import mk.frizer.domain.exceptions.UserNotFoundException;
import mk.frizer.repository.BaseUserRepository;
import mk.frizer.repository.BusinessOwnerRepository;
import mk.frizer.repository.SalonRepository;
import mk.frizer.service.impl.BusinessOwnerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BusinessOwnerServiceImplTest {

    @Mock
    private BusinessOwnerRepository businessOwnerRepository;

    @Mock
    private SalonRepository salonRepository;

    @Mock
    private BaseUserRepository baseUserRepository;

    @InjectMocks
    private BusinessOwnerServiceImpl businessOwnerService;

    private BaseUser baseUser;
    private BusinessOwner businessOwner;
    private Salon salon;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        baseUser = new BaseUser();
        baseUser.setId(1L);
        baseUser.setEmail("test@example.com");
        baseUser.setPassword("password");
        baseUser.setFirstName("John");
        baseUser.setLastName("Doe");
        baseUser.setRoles(new HashSet<>());

        businessOwner = new BusinessOwner(baseUser);
        businessOwner.setId(1L);

        salon = new Salon();
        salon.setId(1L);
        salon.setName("Test Salon");
        salon.setOwner(businessOwner);
    }

    @Test
    void getBusinessOwners_shouldReturnAllBusinessOwners() {
        when(businessOwnerRepository.findAll()).thenReturn(Collections.singletonList(businessOwner));

        List<BusinessOwner> owners = businessOwnerService.getBusinessOwners();

        assertEquals(1, owners.size());
        assertEquals(businessOwner, owners.get(0));
    }

    @Test
    void getBusinessOwnerById_shouldReturnBusinessOwner() {
        when(businessOwnerRepository.findById(1L)).thenReturn(Optional.of(businessOwner));

        Optional<BusinessOwner> owner = businessOwnerService.getBusinessOwnerById(1L);

        assertTrue(owner.isPresent());
        assertEquals(businessOwner, owner.get());
    }

    @Test
    void getBusinessOwnerById_shouldThrowUserNotFoundException() {
        when(businessOwnerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> businessOwnerService.getBusinessOwnerById(1L));
    }

    @Test
    void createBusinessOwner_shouldCreateBusinessOwnerAndAssignRole() {
        when(baseUserRepository.findById(1L)).thenReturn(Optional.of(baseUser));
        when(businessOwnerRepository.save(any(BusinessOwner.class)))
                .thenAnswer(invocation -> {
                    BusinessOwner savedOwner = invocation.getArgument(0);
                    savedOwner.setId(1L);
                    return savedOwner;
                });

        Optional<BusinessOwner> owner = businessOwnerService.createBusinessOwner(1L);

        assertTrue(owner.isPresent());
        assertEquals(businessOwner, owner.get());
        assertTrue(baseUser.getRoles().contains(Role.ROLE_OWNER));
        verify(baseUserRepository, times(1)).save(baseUser);
        verify(businessOwnerRepository, times(1)).save(any(BusinessOwner.class));
    }


    @Test
    void createBusinessOwner_shouldThrowUserNotFoundException() {
        when(baseUserRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> businessOwnerService.createBusinessOwner(1L));
    }

    @Test
    void deleteBusinessOwnerById_shouldDeleteBusinessOwner() {
        when(businessOwnerRepository.findById(1L)).thenReturn(Optional.of(businessOwner));

        Optional<BusinessOwner> owner = businessOwnerService.deleteBusinessOwnerById(1L);

        assertTrue(owner.isPresent());
        assertEquals(businessOwner, owner.get());
        verify(businessOwnerRepository, times(1)).deleteById(1L);
    }

    @Test
    void addSalonToBusinessOwner_shouldAddSalon() {
        when(businessOwnerRepository.findById(1L)).thenReturn(Optional.of(businessOwner));
        when(businessOwnerRepository.save(businessOwner)).thenReturn(businessOwner);

        Optional<BusinessOwner> owner = businessOwnerService.addSalonToBusinessOwner(1L, salon);

        assertTrue(owner.isPresent());
        assertEquals(1, owner.get().getSalonList().size());
        assertEquals(salon, owner.get().getSalonList().get(0));
    }

    @Test
    void editSalonForBusinessOwner_shouldEditSalon() {
        businessOwner.getSalonList().add(salon);
        Salon updatedSalon = new Salon();
        updatedSalon.setId(1L);
        updatedSalon.setName("Updated Salon");
        updatedSalon.setOwner(businessOwner);

        when(businessOwnerRepository.findById(1L)).thenReturn(Optional.of(businessOwner));
        when(businessOwnerRepository.save(any(BusinessOwner.class))).thenReturn(businessOwner);

        Optional<BusinessOwner> owner = businessOwnerService.editSalonForBusinessOwner(updatedSalon);

        assertTrue(owner.isPresent());
        assertEquals("Updated Salon", owner.get().getSalonList().get(0).getName());
    }
}