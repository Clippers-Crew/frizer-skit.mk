package mk.frizer.web.rest;

import mk.frizer.domain.BusinessOwner;
import mk.frizer.domain.dto.simple.BusinessOwnerSimpleDTO;
import mk.frizer.domain.exceptions.UserNotFoundException;
import mk.frizer.service.BusinessOwnerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping({"/api/owners", "/api/owner"})
@CrossOrigin(origins = {"localhost:3000","localhost:3001", "localhost:8080"})
public class BusinessOwnerRestController {
    private final BusinessOwnerService businessOwnerService;

    public BusinessOwnerRestController(BusinessOwnerService businessOwnerService) {
        this.businessOwnerService = businessOwnerService;
    }

    @GetMapping()
    public List<BusinessOwnerSimpleDTO> getAllOwners() {
        return businessOwnerService.getBusinessOwners().stream().map(BusinessOwner::toDto).toList();
    }

    @PostMapping("/add/{id}")
    public ResponseEntity<BusinessOwnerSimpleDTO> createBusinessOwner(@PathVariable Long id) {
        return this.businessOwnerService.createBusinessOwner(id)
                .map(owner -> ResponseEntity.ok().body(owner.toDto()))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusinessOwnerSimpleDTO> getBusinessOwners(@PathVariable Long id){
        return this.businessOwnerService.getBusinessOwnerById(id)
                .map(owner -> ResponseEntity.ok().body(owner.toDto()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<BusinessOwnerSimpleDTO> deleteBusinessOwnerById(@PathVariable Long id) {
        Optional<BusinessOwner> user = this.businessOwnerService.deleteBusinessOwnerById(id);
        try{
            this.businessOwnerService.getBusinessOwnerById(id);
            return ResponseEntity.notFound().build();
        }
        catch(UserNotFoundException exception){
            return ResponseEntity.ok().body(user.get().toDto());
        }
    }
}
