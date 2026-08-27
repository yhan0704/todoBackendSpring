package backend.user.controller;

import backend.user.dto.request.UpdateEmailRequest;
import backend.user.dto.response.UserResponse;
import backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}/email")
    public ResponseEntity<UserResponse> updateEmail(
            @PathVariable Long id,
            @RequestBody UpdateEmailRequest request) {
        return ResponseEntity.ok(userService.updateEmail(id, request));
    }
}