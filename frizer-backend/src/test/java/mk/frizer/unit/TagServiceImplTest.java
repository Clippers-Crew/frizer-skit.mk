package mk.frizer.unit;

import mk.frizer.domain.Salon;
import mk.frizer.domain.Tag;
import mk.frizer.domain.exceptions.SalonNotFoundException;
import mk.frizer.domain.exceptions.TagNotFoundException;
import mk.frizer.repository.SalonRepository;
import mk.frizer.repository.TagRepository;
import mk.frizer.service.impl.TagServiceImpl;
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
class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private SalonRepository salonRepository;

    @InjectMocks
    private TagServiceImpl tagService;

    private Tag tag;
    private Salon salon;

    @BeforeEach
    void setUp() {
        tag = new Tag("Haircut");
        salon = new Salon();
    }

    /**
     * Test retrieving all tags.
     * Ensures that the service returns the list of tags from the repository.
     */
    @Test
    void testGetTags() {
        when(tagRepository.findAll()).thenReturn(List.of(tag));

        List<Tag> tags = tagService.getTags();

        assertEquals(1, tags.size());
        assertEquals("Haircut", tags.get(0).getName());
        verify(tagRepository, times(1)).findAll();
    }

    /**
     * Test retrieving a tag by ID.
     * Ensures the correct tag is returned, or an exception is thrown if not found.
     */
    @Test
    void testGetTagById() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        Optional<Tag> foundTag = tagService.getTagById(1L);

        assertTrue(foundTag.isPresent());
        assertEquals("Haircut", foundTag.get().getName());
        verify(tagRepository, times(1)).findById(1L);
    }

    @Test
    void testGetTagById_NotFound() {
        when(tagRepository.findById(anyLong())).thenThrow(new TagNotFoundException());

        assertThrows(TagNotFoundException.class, () -> tagService.getTagById(1L));
    }

    /**
     * Test creating a new tag.
     * Ensures the tag is saved correctly in the repository.
     */
    @Test
    void testCreateTag() {
        when(tagRepository.save(any(Tag.class))).thenReturn(tag);

        Optional<Tag> createdTag = tagService.createTag("Haircut");

        assertTrue(createdTag.isPresent());
        assertEquals("Haircut", createdTag.get().getName());
        verify(tagRepository, times(1)).save(any(Tag.class));
    }

    /**
     * Test retrieving tags for a specific salon.
     * Verifies the service filters tags by the given salon.
     */
    @Test
    void testGetTagsForSalon() {
        when(salonRepository.findById(1L)).thenReturn(Optional.of(salon));
        when(tagRepository.findAll()).thenReturn(List.of(tag));
        tag.getSalonsWithTag().add(salon); // Simulate that the tag is linked to the salon

        List<Tag> tagsForSalon = tagService.getTagsForSalon(1L);

        assertEquals(1, tagsForSalon.size());
        assertTrue(tagsForSalon.contains(tag));
        verify(salonRepository, times(1)).findById(1L);
        verify(tagRepository, times(1)).findAll();
    }

    @Test
    void testGetTagsForSalon_SalonNotFound() {
        when(salonRepository.findById(anyLong())).thenThrow(new SalonNotFoundException());

        assertThrows(SalonNotFoundException.class, () -> tagService.getTagsForSalon(1L));
    }

    /**
     * Test deleting a tag by ID.
     * Ensures the tag is deleted from the repository.
     */
    @Test
    void testDeleteTagById() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        Optional<Tag> deletedTag = tagService.deleteTagById(1L);

        assertTrue(deletedTag.isPresent());
        verify(tagRepository, times(1)).deleteById(1L);
    }
}
