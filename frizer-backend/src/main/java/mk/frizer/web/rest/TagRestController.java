package mk.frizer.web.rest;

import mk.frizer.domain.Tag;
import mk.frizer.domain.dto.simple.TagSimpleDTO;
import mk.frizer.domain.exceptions.TagNotFoundException;
import mk.frizer.service.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping({"/api/tags", "/api/tag"})
@CrossOrigin(origins = {"localhost:3000","localhost:3001", "localhost:8080"})
public class TagRestController {
    private final TagService tagService;

    public TagRestController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping()
    public List<TagSimpleDTO> getTags() {
        return tagService.getTags().stream().map(Tag::toDto).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagSimpleDTO> getTag(@PathVariable Long id){
        return this.tagService.getTagById(id)
                .map(tag -> ResponseEntity.ok().body(tag.toDto()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/add")
    public ResponseEntity<TagSimpleDTO> createTag(@RequestParam String name) {
        return this.tagService.createTag(name)
                .map(tag -> ResponseEntity.ok().body(tag.toDto()))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<TagSimpleDTO> deleteTagById(@PathVariable Long id) {
        Optional<Tag> tag = this.tagService.deleteTagById(id);
        try{
            this.tagService.getTagById(id);
            return ResponseEntity.badRequest().build();
        }
        catch(TagNotFoundException exception){
            return ResponseEntity.ok().body(tag.get().toDto());
        }
    }
}
