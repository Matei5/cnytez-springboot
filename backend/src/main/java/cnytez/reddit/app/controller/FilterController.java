package cnytez.reddit.app.controller;

import cnytez.reddit.app.dto.ApiResponse;
import cnytez.reddit.app.dto.FilterDto;
import cnytez.reddit.app.service.FilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/filterId")
@RequiredArgsConstructor
public class FilterController {
    private final FilterService filterService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FilterDto>>> getFilters() {
        List<FilterDto> filters =
                filterService.getAllFilters();

        ApiResponse<List<FilterDto>> response =
                new ApiResponse<>(
                        true,
                        filters
                );

        return ResponseEntity.ok(response);
    }
}
