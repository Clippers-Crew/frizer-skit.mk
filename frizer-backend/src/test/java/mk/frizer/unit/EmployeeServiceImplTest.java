package mk.frizer.unit;

import mk.frizer.domain.*;
import mk.frizer.domain.dto.EmployeeAddDTO;
import mk.frizer.domain.exceptions.EmployeeNotFoundException;
import mk.frizer.repository.*;
import mk.frizer.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceImplTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private BaseUserRepository baseUserRepository;

    @Mock
    private SalonRepository salonRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee employee;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        BaseUser baseUser = new BaseUser();
        baseUser.setId(1L);

        Salon salon = new Salon();
        salon.setId(1L);

        employee = new Employee(baseUser, salon);
        employee.setId(1L);

        appointment = new Appointment();
        appointment.setId(1L);
        appointment.setEmployee(employee);
    }

    /***
     * Test Case for `addActiveAppointmentForEmployee` Method
     */
    @Test
    void testAddActiveAppointmentForEmployee() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Optional<Employee> updatedEmployee = employeeService.addActiveAppointmentForEmployee(appointment);

        assertTrue(updatedEmployee.isPresent());
        assertTrue(updatedEmployee.get().getAppointmentsActive().contains(appointment));
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    /***
     * Test Case for `addHistoryAppointmentForEmployee` Method
     */
    @Test
    void testAddHistoryAppointmentForEmployee() {
        employee.getAppointmentsActive().add(appointment);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Optional<Employee> updatedEmployee = employeeService.addHistoryAppointmentForEmployee(appointment);

        assertTrue(updatedEmployee.isPresent());
        assertTrue(updatedEmployee.get().getAppointmentsHistory().contains(appointment));
        assertFalse(updatedEmployee.get().getAppointmentsActive().contains(appointment));
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    /***
     * Test Case for `getEmployeeById` Method
     */
    @Test
    void testGetEmployeeById() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));

        Optional<Employee> foundEmployee = employeeService.getEmployeeById(1L);

        assertTrue(foundEmployee.isPresent());
        assertEquals(1L, foundEmployee.get().getId());
        verify(employeeRepository, times(1)).findById(anyLong());
    }

    @Test
    void testGetEmployeeById_ThrowsEmployeeNotFoundException() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(1L));
        verify(employeeRepository, times(1)).findById(anyLong());
    }

    /***
     * Test Case for `createEmployee` Method
     */
    @Test
    void testCreateEmployee() {
        BaseUser baseUser = new BaseUser();
        baseUser.setId(1L);
        baseUser.setRoles(new HashSet<>());
        Salon salon = new Salon();
        salon.setId(1L);
        EmployeeAddDTO employeeAddDTO = new EmployeeAddDTO(1L, 1L);

        when(baseUserRepository.findById(1L)).thenReturn(Optional.of(baseUser));
        when(baseUserRepository.save(any(BaseUser.class))).thenReturn(baseUser);
        when(salonRepository.findById(1L)).thenReturn(Optional.of(salon));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Optional<Employee> createdEmployee = employeeService.createEmployee(employeeAddDTO);

        assertTrue(createdEmployee.isPresent());
        assertEquals(employee.getId(), createdEmployee.get().getId());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    /***
     * Test Case for `deleteEmployeeById` Method
     */
    @Test
    void testDeleteEmployeeById() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));

        Optional<Employee> deletedEmployee = employeeService.deleteEmployeeById(1L);

        assertTrue(deletedEmployee.isPresent());
        verify(employeeRepository, times(1)).deleteById(anyLong());
    }

    @Test
    void testDeleteEmployeeById_ThrowsEmployeeNotFoundException() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployeeById(1L));
        verify(employeeRepository, times(1)).findById(anyLong());
    }
}
