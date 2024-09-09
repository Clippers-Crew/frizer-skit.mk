package mk.frizer.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import mk.frizer.domain.BaseUser;
import mk.frizer.domain.Customer;
import mk.frizer.domain.dto.simple.CustomerSimpleDTO;
import mk.frizer.domain.enums.Role;
import mk.frizer.service.CustomerService;
import mk.frizer.web.rest.CustomerRestController;
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
@WebMvcTest(CustomerRestController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CustomerRestControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    private Customer customer;
    private BaseUser baseUser;

    @BeforeEach
    void setUp() {
        baseUser = new BaseUser("janedoe@example.com", "password", "Jane", "Doe", "0987654321", Role.ROLE_USER);
        baseUser.setId(1L);
        customer = new Customer(baseUser);
        customer.setId(1L);
    }

    @Test
    void testGetAllCustomers() throws Exception {
        // Arrange
        CustomerSimpleDTO customerDTO = customer.toDto();

        // When
        when(customerService.getCustomers()).thenReturn(List.of(customer));

        // Act & Assert
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(customerDTO.getId()))
                .andExpect(jsonPath("$[0].baseUserId").value(customerDTO.getBaseUserId()))
                .andExpect(jsonPath("$[0].email").value(customerDTO.getEmail()));
    }

    @Test
    void testGetCustomerById() throws Exception {
        // Arrange
        Long customerId = 1L;
        CustomerSimpleDTO customerDTO = customer.toDto();

        // When
        when(customerService.getCustomerById(customerId)).thenReturn(Optional.of(customer));

        // Act & Assert
        mockMvc.perform(get("/api/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(customerDTO.getId()))
                .andExpect(jsonPath("$.baseUserId").value(customerDTO.getBaseUserId()))
                .andExpect(jsonPath("$.email").value(customerDTO.getEmail()));
    }

    @Test
    void testGetCustomerNotFound() throws Exception {
        // Arrange
        Long customerId = 99L;

        // When
        when(customerService.getCustomerById(customerId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/customers/{id}", customerId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateCustomer() throws Exception {
        // Arrange
        Long baseUserId = 1L;
        CustomerSimpleDTO customerDTO = customer.toDto();

        // When
        when(customerService.createCustomer(baseUserId)).thenReturn(Optional.of(customer));

        // Act & Assert
        mockMvc.perform(post("/api/customers/add/{id}", baseUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(customerDTO.getId()))
                .andExpect(jsonPath("$.baseUserId").value(customerDTO.getBaseUserId()))
                .andExpect(jsonPath("$.email").value(customerDTO.getEmail()));
    }

    @Test
    void testCreateCustomerBadRequest() throws Exception {
        // Arrange
        Long invalidBaseUserId = 99L;

        // When
        when(customerService.createCustomer(invalidBaseUserId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/customers/add/{id}", invalidBaseUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteCustomerById() throws Exception {
        // Arrange
        Long customerId = 1L;
        CustomerSimpleDTO customerDTO = customer.toDto();

        // When
        when(customerService.deleteCustomerById(customerId)).thenReturn(Optional.of(customer));

        // Act & Assert
        mockMvc.perform(delete("/api/customers/delete/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(customerDTO.getId()));
    }

    @Test
    void testDeleteCustomerNotFound() throws Exception {
        // Arrange
        Long customerId = 99L;

        // When
        when(customerService.deleteCustomerById(customerId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/api/customers/delete/{id}", customerId))
                .andExpect(status().isNotFound());
    }
}
