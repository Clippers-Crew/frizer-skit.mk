package mk.frizer.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import mk.frizer.domain.BaseUser;
import mk.frizer.domain.Employee;
import mk.frizer.domain.Review;
import mk.frizer.domain.dto.ReviewAddDTO;
import mk.frizer.domain.dto.ReviewUpdateDTO;
import mk.frizer.domain.dto.simple.ReviewSimpleDTO;
import mk.frizer.domain.exceptions.ReviewNotFoundException;
import mk.frizer.service.ReviewService;
import mk.frizer.web.rest.ReviewRestController;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ReviewRestController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ReviewRestControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    private Review review;
    private BaseUser author;
    private BaseUser employeeUser;
    private Employee employee;

    @BeforeEach
    void setUp() {
        author = new BaseUser("johndoe@example.com", "password", "John", "Doe", "1234567890");
        author.setId(1L);

        employeeUser = new BaseUser("employe@example.com", "password", "Jane", "Smith", "0987654321");
        employeeUser.setId(2L);

        employee = new Employee(employeeUser, null);
        employee.setId(1L);

        review = new Review(author, employee, 5.0, "Excellent service");
        review.setId(1L);
    }

    @Test
    void testGetAllReviews() throws Exception {
        // Arrange
        ReviewSimpleDTO reviewDTO = review.toDto();

        // When
        when(reviewService.getReviews()).thenReturn(List.of(review));

        // Act & Assert
        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(reviewDTO.getId()))
                .andExpect(jsonPath("$[0].authorId").value(reviewDTO.getAuthorId()))
                .andExpect(jsonPath("$[0].employeeId").value(reviewDTO.getEmployeeId()))
                .andExpect(jsonPath("$[0].rating").value(reviewDTO.getRating()))
                .andExpect(jsonPath("$[0].comment").value(reviewDTO.getComment()));
    }

    @Test
    void testGetReviewById() throws Exception {
        // Arrange
        Long reviewId = 1L;
        ReviewSimpleDTO reviewDTO = review.toDto();

        // When
        when(reviewService.getReviewById(reviewId)).thenReturn(Optional.of(review));

        // Act & Assert
        mockMvc.perform(get("/api/reviews/{id}", reviewId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(reviewDTO.getId()))
                .andExpect(jsonPath("$.authorId").value(reviewDTO.getAuthorId()))
                .andExpect(jsonPath("$.employeeId").value(reviewDTO.getEmployeeId()))
                .andExpect(jsonPath("$.rating").value(reviewDTO.getRating()))
                .andExpect(jsonPath("$.comment").value(reviewDTO.getComment()));
    }

    @Test
    void testGetReviewNotFound() throws Exception {
        // Arrange
        Long reviewId = 99L;

        // When
        when(reviewService.getReviewById(reviewId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/reviews/{id}", reviewId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateReview() throws Exception {
        // Arrange
        ReviewAddDTO reviewAddDTO = new ReviewAddDTO(1L, 1L, 4.5, "Great service");
        ReviewSimpleDTO reviewDTO = review.toDto();

        // When
        when(reviewService.createReviewForEmployee(reviewAddDTO)).thenReturn(Optional.of(review));

        // Act & Assert
        mockMvc.perform(post("/api/reviews/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewAddDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(reviewDTO.getId()))
                .andExpect(jsonPath("$.authorId").value(reviewDTO.getAuthorId()))
                .andExpect(jsonPath("$.employeeId").value(reviewDTO.getEmployeeId()))
                .andExpect(jsonPath("$.rating").value(reviewDTO.getRating()))
                .andExpect(jsonPath("$.comment").value(reviewDTO.getComment()));
    }

    @Test
    void testCreateReviewBadRequest() throws Exception {
        // Arrange
        ReviewAddDTO reviewAddDTO = new ReviewAddDTO(1L, 1L, 4.5, "Great service");

        // When
        when(reviewService.createReviewForEmployee(reviewAddDTO)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/reviews/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewAddDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateReview() throws Exception {
        // Arrange
        Long reviewId = 1L;
        ReviewUpdateDTO reviewUpdateDTO = new ReviewUpdateDTO(3.0, "Good service");
        ReviewSimpleDTO reviewDTO = review.toDto();

        // When
        when(reviewService.updateReview(reviewId, reviewUpdateDTO)).thenReturn(Optional.of(review));

        // Act & Assert
        mockMvc.perform(put("/api/reviews/edit/{id}", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(reviewDTO.getId()))
                .andExpect(jsonPath("$.authorId").value(reviewDTO.getAuthorId()))
                .andExpect(jsonPath("$.employeeId").value(reviewDTO.getEmployeeId()))
                .andExpect(jsonPath("$.rating").value(reviewDTO.getRating()))
                .andExpect(jsonPath("$.comment").value(reviewDTO.getComment()));
    }

    @Test
    void testUpdateReviewBadRequest() throws Exception {
        // Arrange
        Long reviewId = 1L;
        ReviewUpdateDTO reviewUpdateDTO = new ReviewUpdateDTO(3.0, "Good service");

        // When
        when(reviewService.updateReview(reviewId, reviewUpdateDTO)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/api/reviews/edit/{id}", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewUpdateDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteReviewById() throws Exception {
        // Arrange
        Long reviewId = 1L;
        ReviewSimpleDTO reviewDTO = review.toDto();

        // When
        when(reviewService.deleteReviewById(reviewId)).thenReturn(Optional.of(review));
        when(reviewService.getReviewById(reviewId)).thenThrow(ReviewNotFoundException.class);

        // Act & Assert
        mockMvc.perform(delete("/api/reviews/delete/{id}", reviewId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(reviewDTO.getId()));
    }

    @Test
    void testDeleteReviewNotFound() throws Exception {
        // Arrange
        Long reviewId = 99L;

        // When
        when(reviewService.deleteReviewById(reviewId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/api/reviews/delete/{id}", reviewId))
                .andExpect(status().isNotFound());
    }
}