package mk.frizer.unit;

import mk.frizer.domain.*;
import mk.frizer.domain.dto.ReviewAddDTO;
import mk.frizer.domain.dto.ReviewUpdateDTO;
import mk.frizer.domain.events.ReviewCreatedEvent;
import mk.frizer.domain.events.ReviewDeletedEvent;
import mk.frizer.domain.events.ReviewEditedEvent;
import mk.frizer.domain.exceptions.ReviewNotFoundException;
import mk.frizer.repository.CustomerRepository;
import mk.frizer.repository.*;
import mk.frizer.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Review review;
    private Employee employee;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        employee = new Employee();
        review = new Review(customer.getBaseUser(), employee, 5.0, "Great service!");
    }

    /**
     * Test creating a review for an employee.
     * Ensures the review is created and events are published correctly.
     */
    @Test
    void testCreateReviewForEmployee() {
        ReviewAddDTO reviewAddDTO = new ReviewAddDTO(1L, 2L, 4.5, "Good service!");
        Review newReview = new Review(customer.getBaseUser(), employee, 4.5, "Good service!");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(customerRepository.findById(2L)).thenReturn(Optional.of(customer));
        when(reviewRepository.save(any(Review.class))).thenReturn(newReview);

        Optional<Review> createdReview = reviewService.createReviewForEmployee(reviewAddDTO);

        assertTrue(createdReview.isPresent());
        assertEquals(4.5, createdReview.get().getRating());
        assertEquals("Good service!", createdReview.get().getComment());
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(applicationEventPublisher, times(1)).publishEvent(any(ReviewCreatedEvent.class));
    }

    /**
     * Test updating a review for an employee.
     * Verifies the review's rating and comment are updated and events are published.
     */
    @Test
    void testUpdateReview() {
        ReviewUpdateDTO reviewUpdateDTO = new ReviewUpdateDTO(4.0, "Updated Review");

        when(reviewRepository.findById(anyLong())).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        Optional<Review> updatedReview = reviewService.updateReview(1L, reviewUpdateDTO);

        assertTrue(updatedReview.isPresent());
        assertEquals(4.0, updatedReview.get().getRating());
        assertEquals("Updated Review", updatedReview.get().getComment());
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(applicationEventPublisher, times(1)).publishEvent(any(ReviewEditedEvent.class));
    }

    /**
     * Test deleting a review by its ID.
     * Checks if the review is removed and the deletion event is triggered.
     */
    @Test
    void testDeleteReviewById() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        Optional<Review> deletedReview = reviewService.deleteReviewById(1L);

        assertTrue(deletedReview.isPresent());
        verify(reviewRepository, times(1)).deleteById(1L);
        verify(applicationEventPublisher, times(1)).publishEvent(any(ReviewDeletedEvent.class));
    }

    /**
     * Test fetching a review that doesn't exist.
     * Ensures the service throws a ReviewNotFoundException when review is not found.
     */
    @Test
    void testGetReviewById_NotFound() {
        when(reviewRepository.findById(anyLong())).thenThrow(new ReviewNotFoundException());

        assertThrows(ReviewNotFoundException.class, () -> reviewService.getReviewById(1L));
    }
}
