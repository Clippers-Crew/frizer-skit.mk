package mk.frizer.unit;

import mk.frizer.domain.*;
import mk.frizer.domain.dto.AppointmentAddDTO;
import mk.frizer.domain.events.AppointmentCreatedEvent;
import mk.frizer.domain.exceptions.*;
import mk.frizer.repository.*;
import mk.frizer.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private TreatmentRepository treatmentRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private SalonRepository salonRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private Appointment appointment, appointment1;
    private Treatment treatment;
    private Salon salon;
    private Customer customer;
    private Employee employee;

    @BeforeEach
    void setUp() {
        salon = new Salon();
        customer = new Customer();
        employee = new Employee();
        treatment = new Treatment("Haircut", salon, 50.0, 1);

        employee.setId(1L);
        treatment.setId(1L);

        salon.setEmployees(List.of(employee));
        salon.setSalonTreatments(List.of(treatment));

        appointment = new Appointment(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), treatment, salon, employee, customer);
        appointment1 = new Appointment(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), treatment, salon, employee, customer);
    }

    /**
     * Test retrieving all appointments.
     * Ensures that the service fetches all appointments from the repository.
     */
    @Test
    void testGetAppointments() {
        when(appointmentRepository.findAll()).thenReturn(List.of(appointment, appointment1));

        List<Appointment> appointments = appointmentService.getAppointments();

        assertEquals(2, appointments.size());
        assertEquals(treatment, appointments.get(0).getTreatment());
        verify(appointmentRepository, times(1)).findAll();
    }

    /**
     * Test retrieving an appointment by ID.
     * Verifies that the correct appointment is returned or an exception is thrown if not found.
     */
    @Test
    void testGetAppointmentById() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        Optional<Appointment> foundAppointment = appointmentService.getAppointmentById(1L);

        assertTrue(foundAppointment.isPresent());
        assertEquals(treatment, foundAppointment.get().getTreatment());
        verify(appointmentRepository, times(1)).findById(1L);
    }

    @Test
    void testGetAppointmentById_NotFound() {
        when(appointmentRepository.findById(anyLong())).thenThrow(new AppointmentNotFoundException());

        assertThrows(AppointmentNotFoundException.class, () -> appointmentService.getAppointmentById(1L));
    }

    /**
     * Test creating a new appointment.
     * Ensures the appointment is saved and an event is published if valid.
     */
    @Test
    void testCreateAppointment() {
        LocalDateTime startDateTime = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(8, 0));
        LocalDateTime endDateTime = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(8, 20));

        AppointmentAddDTO appointmentAddDTO = new AppointmentAddDTO(startDateTime, endDateTime, treatment.getId(), salon.getId(), employee.getId(), customer.getId());

        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(salonRepository.findById(salon.getId())).thenReturn(Optional.of(salon));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        Optional<Appointment> createdAppointment = appointmentService.createAppointment(appointmentAddDTO);

        assertTrue(createdAppointment.isPresent());
        assertEquals(treatment, createdAppointment.get().getTreatment());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
        verify(applicationEventPublisher, times(1)).publishEvent(any(AppointmentCreatedEvent.class));
    }

    /**
     * Test creating an appointment with times not divisible by 20 minutes.
     * Verifies that an AppointmentNotDivisibleBy20Minutes exception is thrown.
     */
    @Test
    void testCreateAppointment_InvalidTime() {
        LocalDateTime startDateTime = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(8, 5));
        LocalDateTime endDateTime = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(8, 25));

        AppointmentAddDTO appointmentAddDTO = new AppointmentAddDTO(startDateTime, endDateTime, treatment.getId(), salon.getId(), employee.getId(), customer.getId());

        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(salonRepository.findById(salon.getId())).thenReturn(Optional.of(salon));

        assertThrows(AppointmentNotDivisibleBy20Minutes.class, () -> appointmentService.createAppointment(appointmentAddDTO));
    }

    /**
     * Test updating an appointment by ID.
     * Ensures the appointment details are updated correctly.
     */
    @Test
    void testUpdateAppointment() {
        LocalDateTime newFrom = LocalDateTime.now().plusDays(2);
        LocalDateTime newTo = LocalDateTime.now().plusDays(3);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(salonRepository.findById(salon.getId())).thenReturn(Optional.of(salon));
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(treatmentRepository.findById(treatment.getId())).thenReturn(Optional.of(treatment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        Optional<Appointment> updatedAppointment = appointmentService.updateAppointment(1L, newFrom, newTo, treatment.getId(), salon.getId(), employee.getId(), customer.getId());

        assertTrue(updatedAppointment.isPresent());
        assertEquals(newFrom, updatedAppointment.get().getDateFrom());
        assertEquals(newTo, updatedAppointment.get().getDateTo());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    /**
     * Test deleting an appointment by ID.
     * Verifies that the appointment is removed from the repository.
     */
    @Test
    void testDeleteAppointmentById() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        Optional<Appointment> deletedAppointment = appointmentService.deleteAppointmentById(1L);

        assertTrue(deletedAppointment.isPresent());
        verify(appointmentRepository, times(1)).deleteById(1L);
    }

    /**
     * Test changing the attendance status of an appointment.
     * Ensures the attendance status toggles correctly.
     */
    @Test
    void testChangeUserAttendanceAppointment() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        Optional<Appointment> updatedAppointment = appointmentService.changeUserAttendanceAppointment(1L);

        assertTrue(updatedAppointment.isPresent());
        assertTrue(updatedAppointment.get().isAttended()); // Assuming default is false and it should be toggled to true
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }
}
