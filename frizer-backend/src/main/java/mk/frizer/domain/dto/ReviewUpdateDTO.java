package mk.frizer.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReviewUpdateDTO {
    private Double rating;
    private String comment;
}
