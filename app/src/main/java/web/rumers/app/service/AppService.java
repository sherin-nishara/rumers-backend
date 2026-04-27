package web.rumers.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import web.rumers.app.dto.CollegeDto;
import web.rumers.app.entity.College;
import web.rumers.app.repository.AppRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class AppService {
    private final AppRepository appRepository;

    public List<CollegeDto> getAllColleges() {
        return appRepository.findAll()
                .stream()
                .map(c -> {
                    CollegeDto dto = new CollegeDto();
                    dto.setId(c.getId());
                    dto.setName(c.getName());
                    dto.setCity(c.getCity());
                    dto.setLat(c.getLat());
                    dto.setLng(c.getLng());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public CollegeDto getCollege(Long id) {
        College c = appRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("College not found"));
        CollegeDto dto = new CollegeDto();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setCity(c.getCity());
        dto.setLat(c.getLat());
        dto.setLng(c.getLng());
        return dto;
    }
}
