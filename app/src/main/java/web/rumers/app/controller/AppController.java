package web.rumers.app.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.rumers.app.service.AppService;

@RestController
@RequestMapping("/colleges")
@RequiredArgsConstructor

public class AppController {

    private final AppService collegeService;

    // Get all colleges
    @GetMapping
    public ResponseEntity<?> getAllColleges() {
        return ResponseEntity.ok(collegeService.getAllColleges());
    }

    // Get single college
    @GetMapping("/{id}")
    public ResponseEntity<?> getCollege(@PathVariable Long id) {
        return ResponseEntity.ok(collegeService.getCollege(id));
    }
}
