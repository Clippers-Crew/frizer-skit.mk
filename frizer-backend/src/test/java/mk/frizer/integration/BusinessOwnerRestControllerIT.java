package mk.frizer.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import mk.frizer.domain.BaseUser;
import mk.frizer.domain.BusinessOwner;
import mk.frizer.domain.dto.simple.BusinessOwnerSimpleDTO;
import mk.frizer.domain.enums.Role;
import mk.frizer.domain.exceptions.UserNotFoundException;
import mk.frizer.service.BusinessOwnerService;
import mk.frizer.web.rest.BusinessOwnerRestController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(BusinessOwnerRestController.class)
@AutoConfigureMockMvc(addFilters = false)
public class BusinessOwnerRestControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BusinessOwnerService businessOwnerService;

    @Autowired
    private ObjectMapper objectMapper;

    private BusinessOwner businessOwner;
    private BaseUser baseUser;

    @BeforeEach
    void setUp() {
        baseUser = new BaseUser("johndoe@example.com", "password", "John", "Doe", "1234567890", Role.ROLE_USER);
        baseUser.setId(1L);
        businessOwner = new BusinessOwner(baseUser);
        businessOwner.setId(1L);
    }

    @Test
    void testGetAllOwners() throws Exception {
        // Arrange
        BusinessOwnerSimpleDTO ownerDTO = businessOwner.toDto();

        // When
        when(businessOwnerService.getBusinessOwners()).thenReturn(List.of(businessOwner));

        // Act & Assert
        mockMvc.perform(get("/api/owners"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(ownerDTO.getId()))
                .andExpect(jsonPath("$[0].baseUserId").value(ownerDTO.getBaseUserId()))
                .andExpect(jsonPath("$[0].email").value(ownerDTO.getEmail()));
    }

    @Test
    void testGetBusinessOwnerById() throws Exception {
        // Arrange
        Long ownerId = 1L;
        BusinessOwnerSimpleDTO ownerDTO = businessOwner.toDto();

        // When
        when(businessOwnerService.getBusinessOwnerById(ownerId)).thenReturn(Optional.of(businessOwner));

        // Act & Assert
        mockMvc.perform(get("/api/owners/{id}", ownerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(ownerDTO.getId()))
                .andExpect(jsonPath("$.baseUserId").value(ownerDTO.getBaseUserId()))
                .andExpect(jsonPath("$.email").value(ownerDTO.getEmail()));
    }

    @Test
    void testGetBusinessOwnerNotFound() throws Exception {
        // Arrange
        Long ownerId = 99L;

        // When
        when(businessOwnerService.getBusinessOwnerById(ownerId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/owners/{id}", ownerId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateBusinessOwner() throws Exception {
        // Arrange
        Long baseUserId = 1L;
        BusinessOwnerSimpleDTO ownerDTO = businessOwner.toDto();

        // When
        when(businessOwnerService.createBusinessOwner(baseUserId)).thenReturn(Optional.of(businessOwner));

        // Act & Assert
        mockMvc.perform(post("/api/owners/add/{id}", baseUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(ownerDTO.getId()))
                .andExpect(jsonPath("$.baseUserId").value(ownerDTO.getBaseUserId()))
                .andExpect(jsonPath("$.email").value(ownerDTO.getEmail()));
    }

    @Test
    void testCreateBusinessOwnerBadRequest() throws Exception {
        // Arrange
        Long invalidBaseUserId = 99L;

        // When
        when(businessOwnerService.createBusinessOwner(invalidBaseUserId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/owners/add/{id}", invalidBaseUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteBusinessOwnerById() throws Exception {
        // Arrange
        Long ownerId = 1L;
        BusinessOwnerSimpleDTO ownerDTO = businessOwner.toDto();

        // When
        when(businessOwnerService.deleteBusinessOwnerById(ownerId)).thenReturn(Optional.of(businessOwner));
        when(businessOwnerService.getBusinessOwnerById(ownerId)).thenThrow(UserNotFoundException.class);

        // Act & Assert
        mockMvc.perform(delete("/api/owners/delete/{id}", ownerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(ownerDTO.getId()));
    }

    @Test
    void testDeleteBusinessOwnerNotFound() throws Exception {
        // Arrange
        Long ownerId = 99L;

        // When
        when(businessOwnerService.deleteBusinessOwnerById(ownerId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/api/owners/delete/{id}", ownerId))
                .andExpect(status().isNotFound());
    }
}
