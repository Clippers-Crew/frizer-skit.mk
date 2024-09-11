package mk.frizer.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import mk.frizer.domain.City;
import mk.frizer.domain.Salon;
import mk.frizer.domain.dto.SalonAddDTO;
import mk.frizer.domain.dto.SalonUpdateDTO;
import mk.frizer.domain.dto.TagAddDTO;
import mk.frizer.domain.dto.simple.SalonSimpleDTO;
import mk.frizer.domain.exceptions.SalonNotFoundException;
import mk.frizer.service.ImageService;
import mk.frizer.service.SalonService;
import mk.frizer.web.rest.SalonRestController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(SalonRestController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SalonRestControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SalonService salonService;

    @MockBean
    private ImageService imageService;

    @Autowired
    private ObjectMapper objectMapper;

    private Salon salon;

    @BeforeEach
    void setUp() {
        City city = new City("Skopje");  // Example city
        //        BusinessOwner owner = new BusinessOwner("John Doe", "johndoe@example.com");

        // Create the Salon object
        salon = new Salon(
                "Beauty Bliss",
                "A luxurious beauty salon",
                "Main Street 123",
                city,
                "+38970123456",
                null,
                41.9981f,
                21.4254f
        );
    }

    @Test
    void testGetAllSalons() throws Exception {
        // Arrange
        SalonSimpleDTO salonDTO = salon.toDto();

        // When
        when(salonService.getSalons()).thenReturn(List.of(salon));

        // Act & Assert
        mockMvc.perform(get("/api/salons"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(salonDTO.getId()));
    }

    @Test
    void testGetSalonById() throws Exception {
        // Arrange
        Long salonId = 1L;
        salon.setId(salonId);
        SalonSimpleDTO salonDTO = salon.toDto();

        // When
        when(salonService.getSalonById(salonId)).thenReturn(Optional.of(salon));

        // Act & Assert
        mockMvc.perform(get("/api/salons/{id}", salonId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(salonDTO.getId()))
                .andExpect(jsonPath("$.name").value(salonDTO.getName()))
                .andExpect(jsonPath("$.description").value(salonDTO.getDescription()))
                .andExpect(jsonPath("$.city").value(salonDTO.getCity()))
                .andExpect(jsonPath("$.location").value(salonDTO.getLocation()))
                .andExpect(jsonPath("$.phoneNumber").value(salonDTO.getPhoneNumber()));
    }

    @Test
    void testGetSalonById_NotFound() throws Exception {
        // Arrange
        Long salonId = 1L;

        // When
        when(salonService.getSalonById(salonId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/salons/{id}", salonId))
                .andExpect(status().isNotFound());
    }


    @Test
    void testCreateSalon() throws Exception {
        // Arrange
        SalonAddDTO salonAddDTO = new SalonAddDTO(
                "New Salon", "Description", "Location", "Прилеп", "1234567890", 1L, 12.34F, 56.78F
        );
        salon.setId(1L);
        SalonSimpleDTO salonDTO = salon.toDto();

        //When
        when(salonService.createSalon(any(SalonAddDTO.class))).thenReturn(Optional.of(salon));

        // Act & Assert
        mockMvc.perform(post("/api/salons/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(salonAddDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(salonDTO.getId()));
    }

    @Test
    void testCreateSalon_BadRequest() throws Exception {
        // Arrange
        SalonAddDTO salonAddDTO = new SalonAddDTO(
                "", "", "", "Прилеп", "1234567890", 1L, 12.34F, 56.78F // Invalid input (e.g., empty name)
        );

        // When
        when(salonService.createSalon(any(SalonAddDTO.class))).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/salons/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(salonAddDTO)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testUpdateSalon() throws Exception {
        // Arrange
        Long salonId = 1L;
        SalonUpdateDTO salonUpdateDTO = new SalonUpdateDTO(
                "Updated Name", "Updated Description", "Updated Location", "9876543210", 12.34F, 56.78F
        );
        salon.setId(salonId);
        SalonSimpleDTO salonDTO = salon.toDto();
        when(salonService.updateSalon(anyLong(), any(SalonUpdateDTO.class))).thenReturn(Optional.of(salon));

        // Act & Assert
        mockMvc.perform(put("/api/salons/edit/{id}", salonId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(salonUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(salonDTO.getId()));
    }

    @Test
    void testUpdateSalon_BadRequest() throws Exception {
        // Arrange
        Long salonId = 1L;
        SalonUpdateDTO salonUpdateDTO = new SalonUpdateDTO(
                "Updated Name", "Updated Description", "Updated Location", "9876543210", 12.34F, 56.78F
        );

        // When
        when(salonService.updateSalon(anyLong(), any(SalonUpdateDTO.class))).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/api/salons/edit/{id}", salonId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(salonUpdateDTO)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testDeleteSalonById() throws Exception {
        // Arrange
        Long salonId = 1L;
        salon.setId(salonId);
        SalonSimpleDTO salonDTO = salon.toDto();

        // When
        when(salonService.deleteSalonById(salonId)).thenReturn(Optional.of(salon));
        when(salonService.getSalonById(salonId)).thenThrow(SalonNotFoundException.class);

        // Act & Assert
        mockMvc.perform(delete("/api/salons/delete/{id}", salonId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(salonDTO.getId()));
    }

    @Test
    void testDeleteSalonById_NotFound() throws Exception {
        // Arrange
        Long salonId = 1L;

        // When
        when(salonService.deleteSalonById(salonId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/api/salons/delete/{id}", salonId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddTagToSalon() throws Exception {
        // Arrange
        TagAddDTO tagAddDTO = new TagAddDTO(
                1L, 1L
        );

        salon.setId(1L);
        SalonSimpleDTO salonDTO = salon.toDto();

        // When
        when(salonService.addTagToSalon(any(TagAddDTO.class))).thenReturn(Optional.of(salon));

        // Act & Assert
        mockMvc.perform(post("/api/salons/add-tag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagAddDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(salonDTO.getId()));
    }



    @Test
    void testAddTagToSalon_BadRequest() throws Exception {
        // Arrange
        TagAddDTO tagAddDTO = new TagAddDTO(
                1L, 1L
        );

        // When
        when(salonService.addTagToSalon(any(TagAddDTO.class))).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/salons/add-tag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagAddDTO)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testUploadImage() throws Exception {
        // Arrange
        Long salonId = 1L;
        byte[] imageBytes = new byte[]{1, 2, 3};
        MockMultipartFile imageFile = new MockMultipartFile("image", "image.png", "image/png", imageBytes);
        salon.setId(salonId);
        SalonSimpleDTO salonDTO = salon.toDto();

        // When
        when(imageService.saveImage(anyLong(), any(MultipartFile.class))).thenReturn(Optional.of(salon));

        // Act & Assert
        mockMvc.perform(multipart("/api/salons/{id}/upload-image", salonId)
                        .file(imageFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(salonDTO.getId()));
    }

    @Test
    void testUploadBackgroundImage() throws Exception {
        // Arrange
        Long salonId = 1L;
        byte[] imageBytes = new byte[]{1, 2, 3};
        MockMultipartFile imageFile = new MockMultipartFile("image", "background.png", "image/png", imageBytes);
        salon.setId(salonId);
        SalonSimpleDTO salonDTO = salon.toDto();

        // When
        when(imageService.saveBackgroundImage(anyLong(), any(MultipartFile.class))).thenReturn(Optional.of(salon));

        // Act & Assert
        mockMvc.perform(multipart("/api/salons/{id}/upload-background-image", salonId)
                        .file(imageFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(salonDTO.getId()));
    }

    @Test
    void testGetImage() throws Exception {
        // Arrange
        Long salonId = 1L;
        Long imageId = 2L;
        byte[] imageBytes = new byte[]{1, 2, 3};

        // When
        when(imageService.getImage(salonId, imageId)).thenReturn(imageBytes);

        // Act & Assert
        mockMvc.perform(get("/api/salons/{id}/image/{imageId}", salonId, imageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(imageBytes));
    }
}
