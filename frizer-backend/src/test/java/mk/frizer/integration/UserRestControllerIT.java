package mk.frizer.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import mk.frizer.domain.BaseUser;
import mk.frizer.domain.dto.BaseUserAddDTO;
import mk.frizer.domain.dto.BaseUserUpdateDTO;
import mk.frizer.domain.dto.simple.BaseUserSimpleDTO;
import mk.frizer.service.BaseUserService;
import mk.frizer.domain.exceptions.UserNotFoundException;
import mk.frizer.web.rest.TreatmentRestController;
import mk.frizer.web.rest.UserRestController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserRestController.class)
@AutoConfigureMockMvc(addFilters = false)public class UserRestControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BaseUserService baseUserService;

    private BaseUser user;
    private BaseUserSimpleDTO userDTO;

    @BeforeEach
    void setUp() {
        user = new BaseUser("user@example.com", "password", "First", "Last", "1234567890");
        user.setId(1L);

        userDTO = user.toDto();
    }

    @Test
    void testGetAllUsers() throws Exception {
        // Arrange
        List<BaseUser> users = List.of(user);
        List<BaseUserSimpleDTO> userDTOs = List.of(userDTO);

        // When
        when(baseUserService.getBaseUsers()).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(userDTO.getId()))
                .andExpect(jsonPath("$[0].email").value(userDTO.getEmail()))
                .andExpect(jsonPath("$[0].firstName").value(userDTO.getFirstName()))
                .andExpect(jsonPath("$[0].lastName").value(userDTO.getLastName()))
                .andExpect(jsonPath("$[0].phoneNumber").value(userDTO.getPhoneNumber()))
                .andExpect(jsonPath("$[0].roles[0]").value(userDTO.getRoles().get(0)));
    }

    @Test
    void testGetUserById() throws Exception {
        // Arrange
        Long userId = 1L;

        // When
        when(baseUserService.getBaseUserById(userId)).thenReturn(Optional.of(user));

        // Act & Assert
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userDTO.getId()))
                .andExpect(jsonPath("$.email").value(userDTO.getEmail()))
                .andExpect(jsonPath("$.firstName").value(userDTO.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(userDTO.getLastName()))
                .andExpect(jsonPath("$.phoneNumber").value(userDTO.getPhoneNumber()))
                .andExpect(jsonPath("$.roles[0]").value(userDTO.getRoles().get(0)));
    }

    @Test
    void testGetUserByIdNotFound() throws Exception {
        // Arrange
        Long userId = 1L;

        // When
        when(baseUserService.getBaseUserById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateUser() throws Exception {
        // Arrange
        BaseUserAddDTO userAddDTO = new BaseUserAddDTO("newuser@example.com", "newpassword", "New", "User", "0987654321");

        // When
        when(baseUserService.createBaseUser(userAddDTO)).thenReturn(Optional.of(user));

        // Act & Assert
        mockMvc.perform(post("/api/users/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userAddDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userDTO.getId()))
                .andExpect(jsonPath("$.email").value(userDTO.getEmail()))
                .andExpect(jsonPath("$.firstName").value(userDTO.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(userDTO.getLastName()))
                .andExpect(jsonPath("$.phoneNumber").value(userDTO.getPhoneNumber()))
                .andExpect(jsonPath("$.roles[0]").value(userDTO.getRoles().get(0)));
    }

    @Test
    void testCreateUserBadRequest() throws Exception {
        // Arrange
        BaseUserAddDTO invalidUserAddDTO = new BaseUserAddDTO("", "", "", "", "");

        // When
        when(baseUserService.createBaseUser(any(BaseUserAddDTO.class))).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/users/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(invalidUserAddDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateUser() throws Exception {
        // Arrange
        BaseUserUpdateDTO userUpdateDTO = new BaseUserUpdateDTO("Updated", "User", "0123456789");

        // When
        when(baseUserService.updateBaseUser(user.getId(), userUpdateDTO)).thenReturn(Optional.of(user));

        // Act & Assert
        mockMvc.perform(put("/api/users/edit/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userDTO.getId()))
                .andExpect(jsonPath("$.email").value(userDTO.getEmail()))
                .andExpect(jsonPath("$.firstName").value(userDTO.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(userDTO.getLastName()))
                .andExpect(jsonPath("$.phoneNumber").value(userDTO.getPhoneNumber()))
                .andExpect(jsonPath("$.roles[0]").value(userDTO.getRoles().get(0)));
    }

    @Test
    void testUpdateUserBadRequest() throws Exception {
        // Arrange
        BaseUserUpdateDTO invalidUserUpdateDTO = new BaseUserUpdateDTO("", "", "");

        // When
        when(baseUserService.updateBaseUser(anyLong(), any(BaseUserUpdateDTO.class))).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/api/users/edit/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(invalidUserUpdateDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdatePasswordForUser() throws Exception {
        // Arrange
        String newPassword = "newpassword";

        // When
        when(baseUserService.changeBaseUserPassword(user.getId(), newPassword)).thenReturn(Optional.of(user));

        // Act & Assert
        mockMvc.perform(post("/api/users/edit-password/{id}", user.getId())
                        .param("password", newPassword))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userDTO.getId()))
                .andExpect(jsonPath("$.email").value(userDTO.getEmail()))
                .andExpect(jsonPath("$.firstName").value(userDTO.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(userDTO.getLastName()))
                .andExpect(jsonPath("$.phoneNumber").value(userDTO.getPhoneNumber()))
                .andExpect(jsonPath("$.roles[0]").value(userDTO.getRoles().get(0)));
    }

    @Test
    void testUpdatePasswordForUserBadRequest() throws Exception {
        // Arrange
        String invalidPassword = "";

        // When
        when(baseUserService.changeBaseUserPassword(anyLong(), anyString())).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/users/edit-password/{id}", user.getId())
                        .param("password", invalidPassword))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteUserById() throws Exception {
        // Arrange
        Long userId = 1L;
        when(baseUserService.deleteBaseUserById(userId)).thenReturn(Optional.of(user));
        when(baseUserService.getBaseUserById(userId)).thenThrow(UserNotFoundException.class);

        // Act & Assert
        mockMvc.perform(delete("/api/users/delete/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userDTO.getId()))
                .andExpect(jsonPath("$.email").value(userDTO.getEmail()))
                .andExpect(jsonPath("$.firstName").value(userDTO.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(userDTO.getLastName()))
                .andExpect(jsonPath("$.phoneNumber").value(userDTO.getPhoneNumber()))
                .andExpect(jsonPath("$.roles[0]").value(userDTO.getRoles().get(0)));
    }

    @Test
    void testDeleteUserByIdNotFound() throws Exception {
        // Arrange
        Long userId = 1L;

        // When
        when(baseUserService.deleteBaseUserById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/api/users/delete/{id}", userId))
                .andExpect(status().isNotFound());
    }
}
