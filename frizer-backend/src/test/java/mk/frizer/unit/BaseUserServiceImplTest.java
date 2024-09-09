package mk.frizer.unit;

import mk.frizer.domain.BaseUser;
import mk.frizer.domain.Customer;
import mk.frizer.domain.dto.BaseUserAddDTO;
import mk.frizer.domain.dto.BaseUserUpdateDTO;
import mk.frizer.domain.exceptions.UserNotFoundException;
import mk.frizer.repository.BaseUserRepository;
import mk.frizer.repository.CustomerRepository;
import mk.frizer.service.impl.BaseUserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BaseUserServiceImplTest {

    @Mock
    private BaseUserRepository baseUserRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private BaseUserServiceImpl baseUserService;

    private BaseUser baseUser;
    private BaseUserAddDTO baseUserAddDTO;
    private BaseUserUpdateDTO baseUserUpdateDTO;

    @BeforeEach
    void setUp() {
        baseUser = new BaseUser("test@example.com", "encodedPassword", "John", "Doe", "1234567890");
        baseUserAddDTO = new BaseUserAddDTO("test@example.com", "password", "John", "Doe", "1234567890");
        baseUserUpdateDTO = new BaseUserUpdateDTO("John", "Doe", "0987654321");
    }

    /***
     * Test Case for `getBaseUsers` Method
     * Checks if the method returns all users
     * */
    @Test
    void testGetBaseUsers() {
        when(baseUserRepository.findAll()).thenReturn(List.of(baseUser));

        List<BaseUser> users = baseUserService.getBaseUsers();

        assertEquals(1, users.size());
        assertEquals(baseUser.getEmail(), users.get(0).getEmail());
        verify(baseUserRepository, times(1)).findAll();
    }

    /***
     * Test Case for `getBaseUserById` Method
     * These two tests verifies that the getBaseUserById method
     * correctly retrieves a user by ID or throws an exception if not found.
     * */
    @Test
    void testGetBaseUserById() {
        when(baseUserRepository.findById(anyLong())).thenReturn(Optional.of(baseUser));

        Optional<BaseUser> user = baseUserService.getBaseUserById(1L);

        assertTrue(user.isPresent());
        assertEquals(baseUser.getEmail(), user.get().getEmail());
        verify(baseUserRepository, times(1)).findById(1L);
    }

    @Test
    void testGetBaseUserById_UserNotFound() {
        when(baseUserRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> baseUserService.getBaseUserById(1L));
        verify(baseUserRepository, times(1)).findById(1L);
    }

    /***
     * Test Case for createBaseUser Method
     * This test checks that a new user is created and persisted correctly
     * */
    @Test
    void testCreateBaseUser() {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(baseUserRepository.save(any(BaseUser.class))).thenReturn(baseUser);
        when(customerRepository.save(any(Customer.class))).thenReturn(new Customer(baseUser));

        Optional<BaseUser> user = baseUserService.createBaseUser(baseUserAddDTO);

        assertTrue(user.isPresent());
        assertEquals("encodedPassword", user.get().getPassword());
        verify(baseUserRepository, times(1)).save(any(BaseUser.class));
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    /***
     *  Test Case for updateBaseUser Method
     *  This test checks that an existing user's information is updated.
     * */
    @Test
    void testUpdateBaseUser() {
        when(baseUserRepository.findById(anyLong())).thenReturn(Optional.of(baseUser));
        when(baseUserRepository.save(any(BaseUser.class))).thenReturn(baseUser);

        Optional<BaseUser> user = baseUserService.updateBaseUser(1L, baseUserUpdateDTO);

        assertTrue(user.isPresent());
        assertEquals(baseUserUpdateDTO.getPhoneNumber(), user.get().getPhoneNumber());
        assertEquals(baseUserUpdateDTO.getFirstName(), user.get().getFirstName());
        assertEquals(baseUserUpdateDTO.getLastName(), user.get().getLastName());
        verify(baseUserRepository, times(1)).save(any(BaseUser.class));
    }

    /***
     * Test Case for changeBaseUserPassword Method
     * This test verifies that the user's password is changed correctly.
     * */
    @Test
    void testChangeBaseUserPassword() {
        when(baseUserRepository.findById(anyLong())).thenReturn(Optional.of(baseUser));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(baseUserRepository.save(any(BaseUser.class))).thenReturn(baseUser);

        Optional<BaseUser> user = baseUserService.changeBaseUserPassword(1L, "newPassword");

        assertTrue(user.isPresent());
        assertEquals("newEncodedPassword", user.get().getPassword());
        verify(baseUserRepository, times(1)).save(any(BaseUser.class));
    }

    /***
     * Test Case for deleteBaseUserById Method
     * This test checks that a user is deleted correctly.
     * */
    @Test
    void testDeleteBaseUserById() {
        when(baseUserRepository.findById(anyLong())).thenReturn(Optional.of(baseUser));

        Optional<BaseUser> user = baseUserService.deleteBaseUserById(1L);

        assertTrue(user.isPresent());
        verify(baseUserRepository, times(1)).deleteById(1L);
    }
}