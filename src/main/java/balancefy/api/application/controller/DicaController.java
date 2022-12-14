package balancefy.api.application.controller;

import balancefy.api.application.dto.response.DicaResponseDto;
import balancefy.api.application.dto.response.ListaDicaResponseDto;
import balancefy.api.domain.exceptions.NotFoundException;
import balancefy.api.resources.entities.Dica;
import balancefy.api.domain.services.DicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/dicas")
public class DicaController {
    @Autowired
    private DicaService dicaService;

    @GetMapping
    public @ResponseBody
    Iterable<DicaResponseDto> getAllDicas() {
        return dicaService.getDicas();
    }

    @PostMapping
    public ResponseEntity<DicaResponseDto> create(@RequestBody Dica dica) {
        try {
            DicaResponseDto account = new DicaResponseDto(dicaService.create(dica));
            return ResponseEntity.status(201).body(account);
        } catch (HttpServerErrorException.InternalServerError ex) {
            return ResponseEntity.status(500).body(new DicaResponseDto(ex));
        } catch (Exception ex) {
            return ResponseEntity.status(400).body(new DicaResponseDto(ex));
        }
    }

    @GetMapping("/{titulo}")
    public ResponseEntity<ListaDicaResponseDto> getDicaByTitulo(@PathVariable String titulo) {
        try {
            return ResponseEntity.status(200).body(new ListaDicaResponseDto(dicaService.getDicaByTitulo(titulo)));
        }
        catch (HttpServerErrorException.InternalServerError ex) {
            return ResponseEntity.status(500).body(new ListaDicaResponseDto(ex));
        } catch (NotFoundException ex) {
            return ResponseEntity.status(404).body(new ListaDicaResponseDto(ex));
        }
        catch (Exception ex) {
            return ResponseEntity.status(400).body(new ListaDicaResponseDto(ex));
        }
    }

    @GetMapping("/export")
    public ResponseEntity exportCsv(){
        try {
            dicaService.exportCsv();

            File file = new File("dicas.csv");
            Path path = Paths.get(file.getAbsolutePath());
            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

            return ResponseEntity
                    .status(200)
                    .header("content-type", "text/csv")
                    .header("content-disposition", "filename=\"dicas.csv\"")
                    .body(resource);
        } catch (IOException ex) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}