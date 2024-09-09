package mk.frizer.service.impl;

import jakarta.transaction.Transactional;
import mk.frizer.domain.BaseUser;
import mk.frizer.domain.Customer;
import mk.frizer.domain.dto.BaseUserAddDTO;
import mk.frizer.domain.dto.BaseUserUpdateDTO;
import mk.frizer.domain.exceptions.UserNotFoundException;
import mk.frizer.repository.BaseUserRepository;
import mk.frizer.repository.CustomerRepository;
import mk.frizer.service.BaseUserService;
import mk.frizer.utilities.FormValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BaseUserServiceImpl implements BaseUserService {
    /*** Summary
     * This test suite covers the basic functionality of the BaseUserServiceImpl class.
     * Each method is tested for its expected behavior, including normal cases and edge
     * cases (e.g., user not found). By mocking dependencies, these tests are fast and isolated,
     * ensuring that they only test the service logic without external dependencies like the
     * database or actual password encoding.
     * */

    private final BaseUserRepository baseUserRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public BaseUserServiceImpl(BaseUserRepository baseUserRepository, CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.baseUserRepository = baseUserRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<BaseUser> getBaseUsers() {
        return baseUserRepository.findAll();
    }

    @Override
    public Optional<BaseUser> getBaseUserById(Long id) {
        BaseUser user = baseUserRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
        return Optional.of(user);
    }

    @Override
    @Transactional
    public Optional<BaseUser> createBaseUser(BaseUserAddDTO baseUserAddDTO) {
        BaseUser user = baseUserRepository.save(new BaseUser(baseUserAddDTO.getEmail(), passwordEncoder.encode(baseUserAddDTO.getPassword()), baseUserAddDTO.getFirstName(), baseUserAddDTO.getLastName(), baseUserAddDTO.getPhoneNumber()));
        Customer customer = customerRepository.save(new Customer(user));
        return Optional.of(user);
    }

    @Override
    @Transactional
    public Optional<BaseUser> updateBaseUser(Long id, BaseUserUpdateDTO baseUserUpdateDTO) {
        BaseUser user = getBaseUserById(id).get();

        if (FormValidator.isNameValid(baseUserUpdateDTO.getFirstName())) {
            user.setFirstName(baseUserUpdateDTO.getFirstName());
        }
        if (FormValidator.isNameValid(baseUserUpdateDTO.getLastName())) {
            user.setLastName(baseUserUpdateDTO.getLastName());
        }
        if (FormValidator.isPhoneNumberValid(baseUserUpdateDTO.getPhoneNumber())) {
            user.setPhoneNumber(baseUserUpdateDTO.getPhoneNumber());
        }

        user.setFirstName(baseUserUpdateDTO.getFirstName());
        user.setLastName(baseUserUpdateDTO.getLastName());
        user.setPhoneNumber(baseUserUpdateDTO.getPhoneNumber());

        return Optional.of(baseUserRepository.save(user));
    }

    @Override
    @Transactional
    public Optional<BaseUser> changeBaseUserPassword(Long id, String password) {
        BaseUser user = getBaseUserById(id).get();
        user.setPassword(passwordEncoder.encode(password));
        return Optional.of(baseUserRepository.save(user));
    }

    @Override
    @Transactional
    public Optional<BaseUser> deleteBaseUserById(Long id) {
        BaseUser user = getBaseUserById(id).get();
        baseUserRepository.deleteById(id);
        return Optional.of(user);
    }
}
