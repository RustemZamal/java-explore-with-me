package ru.practicum.main.compilation.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.main.compilation.dto.CompilationDto;
import ru.practicum.main.compilation.dto.NewCompilationDto;
import ru.practicum.main.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    CompilationDto createCompilationViaAdmin(NewCompilationDto newCompilationDto);

    CompilationDto patchCompilationViaAdmin(UpdateCompilationRequest updateCompilationRequest, Long compId);

    void deleteCompilationByIdViaAdmin(Long compId);

    List<CompilationDto> getAllCompilationsViaPublic(boolean pinned, Pageable pageRequest);

    CompilationDto getCompilationByIdViaPublic(Long compId);
}
