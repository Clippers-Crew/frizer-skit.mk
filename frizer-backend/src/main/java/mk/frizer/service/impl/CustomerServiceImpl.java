package mk.frizer.service.impl;

import jakarta.transaction.Transactional;
import mk.frizer.domain.*;
import mk.frizer.domain.enums.Role;
import mk.frizer.domain.exceptions.CustomerNotFoundException;
import mk.frizer.domain.exceptions.UserNotFoundException;
import mk.frizer.repository.AppointmentRepository;
import mk.frizer.repository.BaseUserRepository;
import mk.frizer.repository.CustomerRepository;
import mk.frizer.service.CustomerService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {
    private final AppointmentRepository appointmentRepository;
    private final CustomerRepository customerRepository;
    private final BaseUserRepository baseUserRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository, AppointmentRepository appointmentRepository, BaseUserRepository baseUserRepository) {
        this.customerRepository = customerRepository;
        this.appointmentRepository = appointmentRepository;
        this.baseUserRepository = baseUserRepository;
    }

    @Override
    public List<Customer> getCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public Optional<Customer> getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id).orElseThrow(CustomerNotFoundException::new);
        return Optional.of(customer);
    }

    @Override
    public Optional<Customer> getCustomerByBaseUserId(Long id) {
        Customer customer = customerRepository.findByBaseUserId(id)
                .orElse(null);
        if (customer != null) {
            return Optional.of(customer);
        }
        return Optional.empty();
    }


    @Override
    @Transactional
    public Optional<Customer> createCustomer(Long baseUserId) {
        return customerRepository.findByBaseUserId(baseUserId)
                .or(() -> {
                    BaseUser baseUser = baseUserRepository.findById(baseUserId)
                            .orElseThrow(UserNotFoundException::new);

                    Customer customer = new Customer(baseUser);
                    return Optional.of(customerRepository.save(customer));
                });
    }

    @Override
    @Transactional
    public Optional<Customer> deleteCustomerById(Long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isEmpty())
            throw new CustomerNotFoundException();
        customerRepository.deleteById(id);
        return customer;
    }

    @Override
    @Transactional
    public Optional<Customer> addActiveAppointmentForCustomer(Appointment appointment) {
//        Customer customer = getCustomerById(appointment.getCustomer().getId()).get();
        Customer customer = appointment.getCustomer();
        customer.getAppointmentsActive().add(appointment);
        return Optional.of(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public Optional<Customer> addHistoryAppointmentForCustomer(Appointment appointment) {
//        Customer customer = getCustomerById(appointment.getCustomer().getId()).get();
        Customer customer = appointment.getCustomer();
        customer.getAppointmentsActive().remove(appointment);
        customer.getAppointmentsHistory().add(appointment);
        appointment.setAttended(true);
        appointmentRepository.save(appointment);
        return Optional.of(customerRepository.save(customer));
    }

    @Override
    public Optional<Customer> getCustomerByEmail(String email) {
        return customerRepository
                .findByBaseUser_Email(email);
    }
}
