package mk.frizer.service;

import mk.frizer.domain.Tag;

import java.util.List;
import java.util.Optional;

public interface TagService {
    List<Tag> getTags();
    Optional<Tag> getTagById(Long id);
    Optional<Tag> createTag(String name);
    List<Tag> getTagsForSalon(Long id);
    Optional<Tag> deleteTagById(Long id);
}
