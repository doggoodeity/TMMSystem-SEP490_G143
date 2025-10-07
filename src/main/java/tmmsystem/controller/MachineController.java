package tmmsystem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tmmsystem.dto.MachineDto;
import tmmsystem.mapper.MachineMapper;
import tmmsystem.service.MachineService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/machines")
public class MachineController {
    private final MachineService service;
    private final MachineMapper mapper;
    public MachineController(MachineService service, MachineMapper mapper) { this.service = service; this.mapper = mapper; }

    @GetMapping
    public List<MachineDto> list() { return service.findAll().stream().map(mapper::toDto).collect(Collectors.toList()); }

    @GetMapping("/{id}")
    public MachineDto get(@PathVariable Long id) { return mapper.toDto(service.findById(id)); }

    @PostMapping
    public MachineDto create(@RequestBody MachineDto body) { return mapper.toDto(service.create(mapper.toEntity(body))); }

    @PutMapping("/{id}")
    public MachineDto update(@PathVariable Long id, @RequestBody MachineDto body) { return mapper.toDto(service.update(id, mapper.toEntity(body))); }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}


