package mk.frizer.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import mk.frizer.domain.*;
import mk.frizer.domain.dto.TreatmentAddDTO;
import mk.frizer.domain.dto.TreatmentUpdateDTO;
import mk.frizer.domain.dto.simple.TreatmentSimpleDTO;
import mk.frizer.domain.exceptions.TreatmentNotFoundException;
import mk.frizer.service.TreatmentService;
import mk.frizer.web.rest.TreatmentRestController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(TreatmentRestController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TreatmentRestControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TreatmentService treatmentService;

    @Mock
    private Salon salon;

    private Treatment treatment;
    private TreatmentSimpleDTO treatmentDTO;

    @BeforeEach
    void setUp() {
        salon = new Salon();
        salon.setId(1L);

        treatment = new Treatment("TreatmentName", salon, 100.0, 2);
        treatment.setId(1L);

        treatmentDTO = treatment.toDto();
    }

    @Test
    void testGetAllTreatments() throws Exception {
        // Arrange
        List<Treatment> treatments = List.of(treatment);
        List<TreatmentSimpleDTO> treatmentDTOs = List.of(treatmentDTO);

        // When
        when(treatmentService.getTreatments()).thenReturn(treatments);

        // Act & Assert
        mockMvc.perform(get("/api/treatments"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(treatmentDTO.getId()))
                .andExpect(jsonPath("$[0].name").value(treatmentDTO.getName()))
                .andExpect(jsonPath("$[0].durationMultiplier").value(treatmentDTO.getDurationMultiplier()))
                .andExpect(jsonPath("$[0].salonId").value(treatmentDTO.getSalonId()))
                .andExpect(jsonPath("$[0].price").value(treatmentDTO.getPrice()));
    }

    @Test
    void testGetTreatmentById() throws Exception {
        // Arrange
        Long treatmentId = 1L;

        // When
        when(treatmentService.getTreatmentById(treatmentId)).thenReturn(Optional.of(treatment));

        // Act & Assert
        mockMvc.perform(get("/api/treatments/{id}", treatmentId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(treatmentDTO.getId()))
                .andExpect(jsonPath("$.name").value(treatmentDTO.getName()))
                .andExpect(jsonPath("$.durationMultiplier").value(treatmentDTO.getDurationMultiplier()))
                .andExpect(jsonPath("$.salonId").value(treatmentDTO.getSalonId()))
                .andExpect(jsonPath("$.price").value(treatmentDTO.getPrice()));
    }

    @Test
    void testGetTreatmentByIdNotFound() throws Exception {
        // Arrange
        Long treatmentId = 1L;

        // When
        when(treatmentService.getTreatmentById(treatmentId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/treatments/{id}", treatmentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateTreatment() throws Exception {
        // Arrange
        TreatmentAddDTO treatmentAddDTO = new TreatmentAddDTO("NewTreatment", 1L, 150.0, 3);

        // When
        when(treatmentService.createTreatment(treatmentAddDTO)).thenReturn(Optional.of(treatment));

        // Act & Assert
        mockMvc.perform(post("/api/treatments/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(treatmentAddDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(treatmentDTO.getId()))
                .andExpect(jsonPath("$.name").value(treatmentDTO.getName()))
                .andExpect(jsonPath("$.durationMultiplier").value(treatmentDTO.getDurationMultiplier()))
                .andExpect(jsonPath("$.salonId").value(treatmentDTO.getSalonId()))
                .andExpect(jsonPath("$.price").value(treatmentDTO.getPrice()));
    }

    @Test
    void testCreateTreatmentBadRequest() throws Exception {
        // Arrange
        TreatmentAddDTO invalidTreatmentAddDTO = new TreatmentAddDTO("", null, -1.0, -1);

        // When
        when(treatmentService.createTreatment(invalidTreatmentAddDTO)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/treatments/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(invalidTreatmentAddDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateTreatment() throws Exception {
        // Arrange
        TreatmentUpdateDTO treatmentUpdateDTO = new TreatmentUpdateDTO("UpdatedName", 120.0, 4);

        // When
        when(treatmentService.updateTreatment(treatment.getId(), treatmentUpdateDTO)).thenReturn(Optional.of(treatment));

        // Act & Assert
        mockMvc.perform(put("/api/treatments/edit/{id}", treatment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(treatmentUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(treatmentDTO.getId()))
                .andExpect(jsonPath("$.name").value(treatmentDTO.getName()))
                .andExpect(jsonPath("$.durationMultiplier").value(treatmentDTO.getDurationMultiplier()))
                .andExpect(jsonPath("$.salonId").value(treatmentDTO.getSalonId()))
                .andExpect(jsonPath("$.price").value(treatmentDTO.getPrice()));
    }

    @Test
    void testUpdateTreatmentBadRequest() throws Exception {
        // Arrange
        TreatmentUpdateDTO invalidTreatmentUpdateDTO = new TreatmentUpdateDTO("", -1.0, -1);

        // When
        when(treatmentService.updateTreatment(anyLong(), any(TreatmentUpdateDTO.class))).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/api/treatments/edit/{id}", treatment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(invalidTreatmentUpdateDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteTreatmentById() throws Exception {
        // Arrange
        Long treatmentId = 1L;
        when(treatmentService.deleteTreatmentById(treatmentId)).thenReturn(Optional.of(treatment));
        when(treatmentService.getTreatmentById(treatmentId)).thenThrow(TreatmentNotFoundException.class);

        // Act & Assert
        mockMvc.perform(delete("/api/treatments/delete/{id}", treatmentId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(treatmentDTO.getId()))
                .andExpect(jsonPath("$.name").value(treatmentDTO.getName()))
                .andExpect(jsonPath("$.durationMultiplier").value(treatmentDTO.getDurationMultiplier()))
                .andExpect(jsonPath("$.salonId").value(treatmentDTO.getSalonId()))
                .andExpect(jsonPath("$.price").value(treatmentDTO.getPrice()));
    }

    @Test
    void testDeleteTreatmentByIdNotFound() throws Exception {
        // Arrange
        Long treatmentId = 1L;

        // When
        when(treatmentService.deleteTreatmentById(treatmentId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/api/treatments/delete/{id}", treatmentId))
                .andExpect(status().isNotFound());
    }
}
