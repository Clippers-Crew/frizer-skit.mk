package mk.frizer.service.impl;

import mk.frizer.domain.Salon;
import jakarta.transaction.Transactional;
import mk.frizer.domain.Tag;
import mk.frizer.domain.exceptions.SalonNotFoundException;
import mk.frizer.domain.exceptions.TagNotFoundException;
import mk.frizer.repository.SalonRepository;
import mk.frizer.repository.TagRepository;
import mk.frizer.service.TagService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {
    private final TagRepository tagRepository;
    private final SalonRepository salonRepository;

    public TagServiceImpl(TagRepository tagRepository, SalonRepository salonRepository) {
        this.tagRepository = tagRepository;
        this.salonRepository = salonRepository;
    }

    @Override
    public List<Tag> getTags() {
        return tagRepository.findAll();
    }

    @Override
    public Optional<Tag> getTagById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(TagNotFoundException::new);
        return Optional.of(tag);
    }

    @Override
    @Transactional
    public Optional<Tag> createTag(String name) {
        return Optional.of(tagRepository.save(new Tag(name)));
    }

    @Override
    public List<Tag> getTagsForSalon(Long id) {
        Salon salon = salonRepository.findById(id)
                .orElseThrow(SalonNotFoundException::new);
         List<Tag> tagsForSalon = tagRepository.findAll().stream()
                .filter(t->t.getSalonsWithTag().contains(salon)).collect(Collectors.toList());
        return  tagsForSalon;
    }

    @Override
    @Transactional
    public Optional<Tag> deleteTagById(Long id) {
        Tag tag = getTagById(id).get();
        tagRepository.deleteById(id);
        return Optional.of(tag);
    }
}
