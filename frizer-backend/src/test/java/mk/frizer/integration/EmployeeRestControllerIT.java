package mk.frizer.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import mk.frizer.domain.BaseUser;
import mk.frizer.domain.Employee;
import mk.frizer.domain.Salon;
import mk.frizer.domain.dto.EmployeeAddDTO;
import mk.frizer.domain.dto.simple.EmployeeSimpleDTO;
import mk.frizer.domain.enums.Role;
import mk.frizer.service.EmployeeService;
import mk.frizer.web.rest.EmployeeRestController;
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
@WebMvcTest(EmployeeRestController.class)
@AutoConfigureMockMvc(addFilters = false)
public class EmployeeRestControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private Employee employee;
    private BaseUser baseUser;
    private Salon salon;

    @BeforeEach
    void setUp() {
        baseUser = new BaseUser("michael@example.com", "password", "Michael", "Smith", "9876543210", Role.ROLE_USER);
        baseUser.setId(1L);
        salon = new Salon();
        salon.setId(1L);
        employee = new Employee(baseUser, salon);
        employee.setId(1L);
    }

    @Test
    void testGetAllEmployees() throws Exception {
        // Arrange
        EmployeeSimpleDTO employeeDTO = employee.toDto();

        // When
        when(employeeService.getEmployees()).thenReturn(List.of(employee));

        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(employeeDTO.getId()))
                .andExpect(jsonPath("$[0].baseUserId").value(employeeDTO.getBaseUserId()))
                .andExpect(jsonPath("$[0].email").value(employeeDTO.getEmail()));
    }

    @Test
    void testGetEmployeeById() throws Exception {
        // Arrange
        Long employeeId = 1L;
        EmployeeSimpleDTO employeeDTO = employee.toDto();

        // When
        when(employeeService.getEmployeeById(employeeId)).thenReturn(Optional.of(employee));

        // Act & Assert
        mockMvc.perform(get("/api/employees/{id}", employeeId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(employeeDTO.getId()))
                .andExpect(jsonPath("$.baseUserId").value(employeeDTO.getBaseUserId()))
                .andExpect(jsonPath("$.email").value(employeeDTO.getEmail()));
    }

    @Test
    void testGetEmployeeNotFound() throws Exception {
        // Arrange
        Long employeeId = 99L;

        // When
        when(employeeService.getEmployeeById(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/employees/{id}", employeeId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateEmployee() throws Exception {
        // Arrange
        EmployeeAddDTO employeeAddDTO = new EmployeeAddDTO(1L, 1L); // Adjust as needed
        EmployeeSimpleDTO employeeDTO = employee.toDto();

        // When
        when(employeeService.createEmployee(employeeAddDTO)).thenReturn(Optional.of(employee));

        // Act & Assert
        mockMvc.perform(post("/api/employees/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeAddDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(employeeDTO.getId()))
                .andExpect(jsonPath("$.baseUserId").value(employeeDTO.getBaseUserId()))
                .andExpect(jsonPath("$.email").value(employeeDTO.getEmail()));
    }

    @Test
    void testCreateEmployeeBadRequest() throws Exception {
        // Arrange
        EmployeeAddDTO invalidEmployeeAddDTO = new EmployeeAddDTO(99L, 99L); // Adjust as needed

        // When
        when(employeeService.createEmployee(invalidEmployeeAddDTO)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/employees/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmployeeAddDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteEmployeeById() throws Exception {
        // Arrange
        Long employeeId = 1L;
        EmployeeSimpleDTO employeeDTO = employee.toDto();

        // When
        when(employeeService.deleteEmployeeById(employeeId)).thenReturn(Optional.of(employee));

        // Act & Assert
        mockMvc.perform(delete("/api/employees/delete/{id}", employeeId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(employeeDTO.getId()));
    }

    @Test
    void testDeleteEmployeeNotFound() throws Exception {
        // Arrange
        Long employeeId = 99L;

        // When
        when(employeeService.deleteEmployeeById(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/api/employees/delete/{id}", employeeId))
                .andExpect(status().isNotFound());
    }
}

