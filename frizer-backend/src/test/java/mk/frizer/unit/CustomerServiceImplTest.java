package mk.frizer.unit;

import mk.frizer.domain.*;
import mk.frizer.domain.enums.Role;
import mk.frizer.domain.exceptions.CustomerNotFoundException;
import mk.frizer.domain.exceptions.UserNotFoundException;
import mk.frizer.repository.AppointmentRepository;
import mk.frizer.repository.BaseUserRepository;
import mk.frizer.repository.CustomerRepository;
import mk.frizer.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private BaseUserRepository baseUserRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer customer;
    private BaseUser baseUser;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        baseUser = new BaseUser("test@example.com", "encodedPassword", "John", "Doe", "1234567890");
        customer = new Customer(baseUser);
        appointment = new Appointment();
        appointment.setCustomer(customer);
        appointment.setAttended(false);
    }

    /***
     * Test Case for `getCustomers` Method
     * Checks if the method returns all customers
     * */
    @Test
    void testGetCustomers() {
        when(customerRepository.findAll()).thenReturn(List.of(customer));

        List<Customer> customers = customerService.getCustomers();

        assertEquals(1, customers.size());
        assertEquals(customer.getBaseUser().getEmail(), customers.get(0).getBaseUser().getEmail());
        verify(customerRepository, times(1)).findAll();
    }

    /***
     * Test Case for `getCustomerById` Method
     * These two tests verify that the getCustomerById method
     * correctly retrieves a customer by ID or throws an exception if not found.
     * */
    @Test
    void testGetCustomerById() {
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));

        Optional<Customer> foundCustomer = customerService.getCustomerById(1L);

        assertTrue(foundCustomer.isPresent());
        assertEquals(customer.getBaseUser().getEmail(), foundCustomer.get().getBaseUser().getEmail());
        verify(customerRepository, times(1)).findById(1L);
    }

    @Test
    void testGetCustomerById_CustomerNotFound() {
        when(customerRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.getCustomerById(1L));
        verify(customerRepository, times(1)).findById(1L);
    }

    /***
     * Test Case for `createCustomer` Method
     * This test checks that a new customer is created if not already existing
     * */
    @Test
    void testCreateCustomer() {
        when(customerRepository.findByBaseUserId(anyLong())).thenReturn(Optional.empty());
        when(baseUserRepository.findById(anyLong())).thenReturn(Optional.of(baseUser));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        Optional<Customer> createdCustomer = customerService.createCustomer(1L);

        assertTrue(createdCustomer.isPresent());
        assertEquals(customer.getBaseUser().getEmail(), createdCustomer.get().getBaseUser().getEmail());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void testCreateCustomer_AlreadyExists() {
        when(customerRepository.findByBaseUserId(anyLong())).thenReturn(Optional.of(customer));

        Optional<Customer> existingCustomer = customerService.createCustomer(1L);

        assertTrue(existingCustomer.isPresent());
        assertEquals(customer.getBaseUser().getEmail(), existingCustomer.get().getBaseUser().getEmail());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    /***
     * Test Case for `deleteCustomerById` Method
     * This test checks that a customer is deleted correctly.
     * */
    @Test
    void testDeleteCustomerById() {
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));

        Optional<Customer> deletedCustomer = customerService.deleteCustomerById(1L);

        assertTrue(deletedCustomer.isPresent());
        verify(customerRepository, times(1)).deleteById(1L);
    }

    /***
     * Test Case for `addActiveAppointmentForCustomer` Method
     * This test checks that an appointment is added to the customer's active appointments.
     * */
    @Test
    void testAddActiveAppointmentForCustomer() {
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        Optional<Customer> updatedCustomer = customerService.addActiveAppointmentForCustomer(appointment);

        assertTrue(updatedCustomer.isPresent());
        assertTrue(updatedCustomer.get().getAppointmentsActive().contains(appointment));
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    /***
     * Test Case for `addHistoryAppointmentForCustomer` Method
     * This test checks that an appointment is moved from active to history.
     * */
    @Test
    void testAddHistoryAppointmentForCustomer() {
        customer.getAppointmentsActive().add(appointment);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        Optional<Customer> updatedCustomer = customerService.addHistoryAppointmentForCustomer(appointment);

        assertTrue(updatedCustomer.isPresent());
        assertTrue(updatedCustomer.get().getAppointmentsHistory().contains(appointment));
        assertFalse(updatedCustomer.get().getAppointmentsActive().contains(appointment));
        assertTrue(appointment.isAttended());
        verify(customerRepository, times(1)).save(any(Customer.class));
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    /***
     * Test Case for `getCustomerByEmail` Method
     * This test checks that a customer is retrieved by email.
     * */
    @Test
    void testGetCustomerByEmail() {
        when(customerRepository.findByBaseUser_Email(anyString())).thenReturn(Optional.of(customer));

        Optional<Customer> foundCustomer = customerService.getCustomerByEmail("test@example.com");

        assertTrue(foundCustomer.isPresent());
        assertEquals(customer.getBaseUser().getEmail(), foundCustomer.get().getBaseUser().getEmail());
        verify(customerRepository, times(1)).findByBaseUser_Email("test@example.com");
    }
}
