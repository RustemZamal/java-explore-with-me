package ru.practicum.main.compilation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.main.compilation.dto.CompilationDto;
import ru.practicum.main.compilation.service.CompilationService;
import ru.practicum.main.util.OffsetPageRequest;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
public class CompilationPublicController {

    private final CompilationService compilationService;

    /**
     * @param pinned параметр принимает true-закрепленные,false-не закрепленные подборки.
     * @param from количество элементов, которые нужно пропустить.
     * @param size количиство элементов.
     * @return Возвращает список подборок событий.
     */
    @GetMapping
    public List<CompilationDto> getAllCompilations(
            @RequestParam(defaultValue = "false")boolean pinned,
            @RequestParam(defaultValue = "0") @Min(0) int from,
            @RequestParam(defaultValue = "10") @Max(1) int size) {
        return compilationService.getAllCompilationsViaPublic(pinned, new OffsetPageRequest(from, size));
    }

    /**
     *
     * @param compId id подборки
     * @return Возвращает подборку собития по id.
     */
    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable Long compId) {
        return compilationService.getCompilationByIdViaPublic(compId);
    }
}
