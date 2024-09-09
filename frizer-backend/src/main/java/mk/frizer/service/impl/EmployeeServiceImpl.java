package mk.frizer.service.impl;

import jakarta.transaction.Transactional;
import mk.frizer.domain.*;
import mk.frizer.domain.dto.EmployeeAddDTO;
import mk.frizer.domain.enums.Role;
import mk.frizer.domain.exceptions.*;
import mk.frizer.repository.*;
import mk.frizer.service.EmployeeService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final BaseUserRepository baseUserRepository;
    private final SalonRepository salonRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, BaseUserRepository baseUserRepository, SalonRepository salonRepository) {
        this.employeeRepository = employeeRepository;
        this.baseUserRepository = baseUserRepository;
        this.salonRepository = salonRepository;
    }

    @Override
    public List<Employee> getEmployees() {
        return employeeRepository.findAll();
    }

    @Override
    public Optional<Employee> getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(EmployeeNotFoundException::new);
        return Optional.of(employee);
    }

    @Override
    public Optional<Employee> getEmployeeByBaseUserId(Long id) {
        Employee employee = employeeRepository.findByBaseUserId(id)
                .orElse(null);
        if (employee != null) {
            return Optional.of(employee);
        }
        return Optional.empty();
    }

    @Override
    public List<Employee> getEmployeesForSalon(Long id) {
        Salon salon = salonRepository.findById(id)
                .orElseThrow(SalonNotFoundException::new);

        return employeeRepository.findAll().stream().filter(e -> e.getSalon().equals(salon))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<Employee> createEmployee(EmployeeAddDTO employeeAddDTO) {
        Employee employee = employeeRepository.findAll()
                .stream().filter(e -> e.getBaseUser().getId().equals(employeeAddDTO.getUserId()))
                .findFirst().orElse(null);
        Salon salon = salonRepository.findById(employeeAddDTO.getSalonId())
                .orElseThrow(SalonNotFoundException::new);
        if (employee == null) {
            BaseUser baseUser = baseUserRepository.findById(employeeAddDTO.getUserId())
                    .orElseThrow(UserNotFoundException::new);

            employee = new Employee(baseUser, salon);
            baseUser.getRoles().add(Role.ROLE_EMPLOYEE);
            baseUserRepository.save(baseUser);
        }
        employee.setSalon(salon);
        return Optional.of(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public Optional<Employee> deleteEmployeeById(Long id) {
        Optional<Employee> employee = employeeRepository.findById(id);
        if (employee.isEmpty())
            throw new EmployeeNotFoundException();
        employeeRepository.deleteById(id);
        return employee;
    }

    @Override
    @Transactional
    public Optional<Employee> deleteEmployeeByIdFromSalon(Long id, Long salonId) {
        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(SalonNotFoundException::new);
        Optional<Employee> employee = salon.getEmployees().stream()
                .filter(e -> e.getId().equals(id)).findFirst();
        if (employee.isPresent()) {
            return deleteEmployeeById(id);
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public Optional<Employee> addActiveAppointmentForEmployee(Appointment appointment) {
        Employee employee = appointment.getEmployee();
        employee.getAppointmentsActive().add(appointment);
        return Optional.of(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public Optional<Employee> addHistoryAppointmentForEmployee(Appointment appointment) {
        Employee employee = appointment.getEmployee();
        employee.getAppointmentsActive().remove(appointment);
        employee.getAppointmentsHistory().add(appointment);
        return Optional.of(employeeRepository.save(employee));
    }
}