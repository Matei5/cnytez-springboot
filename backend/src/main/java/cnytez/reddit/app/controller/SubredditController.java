package cnytez.reddit.app.controller;

import cnytez.reddit.app.dto.CreateSubredditRequest;
import cnytez.reddit.app.dto.SubredditDto;
import cnytez.reddit.app.service.SubredditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subreddits")
@RequiredArgsConstructor
public class SubredditController {

    private final SubredditService subredditService;

    // GET /api/subreddits
    @GetMapping
    public ResponseEntity<List<SubredditDto>> getAllSubreddits() {
        return ResponseEntity.ok(subredditService.getAllSubreddits());
    }

    // GET /api/subreddits/{id}
    @GetMapping("/{id}")
    public ResponseEntity<SubredditDto> getSubredditById(@PathVariable Long id) {
        return ResponseEntity.ok(subredditService.getSubredditById(id));
    }

    // GET /api/subreddits/by-name/{name}
    @GetMapping("/by-name/{name}")
    public ResponseEntity<SubredditDto> getSubredditByName(@PathVariable String name) {
        return ResponseEntity.ok(subredditService.getSubredditByName(name));
    }

    // POST /api/subreddits
    @PostMapping
    public ResponseEntity<SubredditDto> createSubreddit(@RequestBody CreateSubredditRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subredditService.createSubreddit(request));
    }

    // POST /api/subreddits/{id}/join?userId={userId}
    @PostMapping("/{id}/join")
    public ResponseEntity<SubredditDto> joinSubreddit(@PathVariable Long id,
                                                      @RequestParam Long userId) {
        return ResponseEntity.ok(subredditService.joinSubreddit(id, userId));
    }

    // DELETE /api/subreddits/{id}/leave?userId={userId}
    @DeleteMapping("/{id}/leave")
    public ResponseEntity<SubredditDto> leaveSubreddit(@PathVariable Long id,
                                                       @RequestParam Long userId) {
        return ResponseEntity.ok(subredditService.leaveSubreddit(id, userId));
    }
}
