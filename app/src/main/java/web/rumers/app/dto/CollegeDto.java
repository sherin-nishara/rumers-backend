package web.rumers.app.dto;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class CollegeDto {
    private Long id;
    private String name;
    private String city;

    private Double lat;
    private Double lng;
}