package mk.frizer.integration;


import com.fasterxml.jackson.databind.ObjectMapper;
import mk.frizer.domain.Tag;
import mk.frizer.domain.dto.simple.TagSimpleDTO;
import mk.frizer.domain.exceptions.TagNotFoundException;
import mk.frizer.service.TagService;
import mk.frizer.web.rest.TagRestController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(TagRestController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TagRestControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TagService tagService;

    @Autowired
    private ObjectMapper objectMapper;

    private Tag tag;

    @BeforeEach
    void setUp() {
        tag = new Tag("TagName");
        tag.setId(1L);
        tag.setSalonsWithTag(new ArrayList<>());
    }

    @Test
    void testGetTags() throws Exception {
        // Arrange
        TagSimpleDTO tagDTO = tag.toDto();

        // When
        when(tagService.getTags()).thenReturn(List.of(tag));

        // Act & Assert
        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(tagDTO.getId()))
                .andExpect(jsonPath("$[0].name").value(tagDTO.getName()))
                .andExpect(jsonPath("$[0].salonsWithTagIds").isEmpty());
    }

    @Test
    void testGetTagById() throws Exception {
        // Arrange
        Long tagId = 1L;
        TagSimpleDTO tagDTO = tag.toDto();

        // When
        when(tagService.getTagById(tagId)).thenReturn(Optional.of(tag));

        // Act & Assert
        mockMvc.perform(get("/api/tags/{id}", tagId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(tagDTO.getId()))
                .andExpect(jsonPath("$.name").value(tagDTO.getName()))
                .andExpect(jsonPath("$.salonsWithTagIds").isEmpty());
    }


    @Test
    void testGetTagNotFound() throws Exception {
        // Arrange
        Long tagId = 99L;

        // When
        when(tagService.getTagById(tagId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/tags/{id}", tagId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateTag() throws Exception {
        // Arrange
        String tagName = "NewTag";
        TagSimpleDTO tagDTO = tag.toDto();

        // When
        when(tagService.createTag(tagName)).thenReturn(Optional.of(tag));

        // Act & Assert
        mockMvc.perform(post("/api/tags/add")
                        .param("name", tagName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(tagDTO.getId()))
                .andExpect(jsonPath("$.name").value(tagDTO.getName()))
                .andExpect(jsonPath("$.salonsWithTagIds").isEmpty());
    }

    @Test
    void testCreateTagBadRequest() throws Exception {
        // Arrange
        String tagName = "NewTag";

        // When
        when(tagService.createTag(tagName)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/tags/add")
                        .param("name", tagName))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteTagById() throws Exception {
        // Arrange
        Long tagId = 1L;
        TagSimpleDTO tagDTO = tag.toDto();

        // When
        when(tagService.deleteTagById(tagId)).thenReturn(Optional.of(tag));
        when(tagService.getTagById(tagId)).thenThrow(TagNotFoundException.class);

        // Act & Assert
        mockMvc.perform(delete("/api/tags/delete/{id}", tagId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(tagDTO.getId()));
    }

    @Test
    void testDeleteTagNotFound() throws Exception {
        // Arrange
        Long tagId = 99L;

        // When
        when(tagService.deleteTagById(tagId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/api/tags/delete/{id}", tagId))
                .andExpect(status().isBadRequest());
    }
}
