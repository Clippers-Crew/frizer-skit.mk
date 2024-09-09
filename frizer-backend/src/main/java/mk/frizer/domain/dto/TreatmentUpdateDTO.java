package mk.frizer.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TreatmentUpdateDTO {
    private String name;
    private Double price;
    private Integer duration;
}
