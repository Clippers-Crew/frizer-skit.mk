package mk.frizer.web.rest;

import mk.frizer.domain.Treatment;
import mk.frizer.domain.dto.TreatmentAddDTO;
import mk.frizer.domain.dto.TreatmentUpdateDTO;
import mk.frizer.domain.dto.simple.TreatmentSimpleDTO;
import mk.frizer.domain.exceptions.TreatmentNotFoundException;
import mk.frizer.service.TreatmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping({"/api/treatments", "/api/treatment"})
@CrossOrigin(origins = {"localhost:3000","localhost:3001", "localhost:8080"})
public class TreatmentRestController {
    private final TreatmentService treatmentService;

    public TreatmentRestController(TreatmentService treatmentService) {
        this.treatmentService = treatmentService;
    }

    @GetMapping()
    public List<TreatmentSimpleDTO> getTreatments(){
        return treatmentService.getTreatments().stream().map(Treatment::toDto).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TreatmentSimpleDTO> getTreatment(@PathVariable Long id){
        return this.treatmentService.getTreatmentById(id)
                .map(treatment -> ResponseEntity.ok().body(treatment.toDto()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/add")
    public ResponseEntity<TreatmentSimpleDTO> createTreatment(@RequestBody TreatmentAddDTO treatmentAddDTO) {
        System.out.println(treatmentAddDTO);
        return this.treatmentService.createTreatment(treatmentAddDTO)
                .map(treatment -> ResponseEntity.ok().body(treatment.toDto()))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<TreatmentSimpleDTO> updateTreatment(@PathVariable Long id, @RequestBody TreatmentUpdateDTO treatmentUpdateDTO) {
        return this.treatmentService.updateTreatment(id, treatmentUpdateDTO)
                .map(treatment -> ResponseEntity.ok().body(treatment.toDto()))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<TreatmentSimpleDTO> deleteTreatmentById(@PathVariable Long id) {
        Optional<Treatment> treatment = this.treatmentService.deleteTreatmentById(id);
        try{
            this.treatmentService.getTreatmentById(id);
            return ResponseEntity.notFound().build();
        }
        catch(TreatmentNotFoundException exception){
            return ResponseEntity.ok().body(treatment.get().toDto());
        }
    }
}
