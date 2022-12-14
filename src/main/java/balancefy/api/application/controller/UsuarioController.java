package balancefy.api.application.controller;

import balancefy.api.application.config.security.TokenService;
import balancefy.api.application.dto.request.UsuarioEditRequest;
import balancefy.api.application.dto.request.UsuarioRequest;
import balancefy.api.application.dto.request.UsuarioSenhaRequestDto;
import balancefy.api.application.dto.response.UploadResponseDto;
import balancefy.api.domain.exceptions.AlreadyExistsException;
import balancefy.api.domain.exceptions.NotFoundException;
import balancefy.api.application.dto.response.ListaUsuarioResponseDto;
import balancefy.api.application.dto.response.UsuarioResponseDto;
import balancefy.api.resources.entities.Usuario;
import balancefy.api.domain.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("/api/users")
public class UsuarioController {
    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private TokenService tokenService;

    @PostMapping
    public ResponseEntity<UsuarioResponseDto> create(@RequestBody @Valid UsuarioRequest usuario) {
        try {
            UsuarioResponseDto account = new UsuarioResponseDto(usuarioService.create(new Usuario(usuario)));
            return ResponseEntity.status(201).body(account);
        } catch (AlreadyExistsException ex) {
            return ResponseEntity.status(400).body(new UsuarioResponseDto(ex));
        } catch (HttpServerErrorException.InternalServerError ex) {
            return ResponseEntity.status(500).body(new UsuarioResponseDto(ex));
        } catch (Exception ex) {
            return ResponseEntity.status(400).body(new UsuarioResponseDto(ex));
        }
    }

    @PutMapping
    public ResponseEntity<UsuarioResponseDto> update(@RequestBody UsuarioEditRequest usuario, @RequestHeader(value = "Authorization") String token) {
        try {
            int id = tokenService.getIdUsuario(token.replace("Bearer ", ""));
            UsuarioResponseDto account = new UsuarioResponseDto(usuarioService.update(usuario, id));
            return ResponseEntity.status(200).body(account);
        } catch (NotFoundException ex) {
            return ResponseEntity.status(404).body(new UsuarioResponseDto(ex));
        } catch (HttpServerErrorException.InternalServerError ex) {
            return ResponseEntity.status(500).body(new UsuarioResponseDto(ex));
        } catch (Exception ex) {
            return ResponseEntity.status(400).body(new UsuarioResponseDto(ex));
        }
    }

    @PutMapping("/upload/avatar")
    public ResponseEntity<UploadResponseDto> updateAvatar(
            @RequestParam("image") MultipartFile multipartFile,
            @RequestHeader(value = "Authorization") String token
    ) {
        try {
            int id = tokenService.getIdUsuario(token.replace("Bearer ", ""));

            String fileName = usuarioService.updateAvatar(multipartFile, id);

            return ResponseEntity.ok(new UploadResponseDto(fileName));
        } catch (NotFoundException ex) {
            return ResponseEntity.status(404).body(new UploadResponseDto(ex.getMessage()));
        } catch (IOException ex) {
            return ResponseEntity.status(500).body(new UploadResponseDto(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(400).body(new UploadResponseDto(ex.getMessage()));
        }

    }

    @PutMapping("/upload/banner")
    public ResponseEntity updateBanner(
            @RequestParam("image") MultipartFile multipartFile,
            @RequestHeader(value = "Authorization") String token
    ) {
        try {
            int id = tokenService.getIdUsuario(token.replace("Bearer ", ""));

            usuarioService.updateBanner(multipartFile, id);

            return ResponseEntity.ok("Upload Ok");
        } catch (NotFoundException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        } catch (IOException ex) {
            return ResponseEntity.status(500).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(400).body(ex.getMessage());
        }
    }

    @CrossOrigin
    @PutMapping("/senha")
    public ResponseEntity updatePassword(@RequestBody UsuarioSenhaRequestDto request,
                                         @RequestHeader(value = "Authorization") String token) {
        try {
            int id = tokenService.getIdUsuario(token.replace("Bearer ", ""));
            usuarioService.updatePassword(id, request);
            return ResponseEntity.status(200).build();
        } catch (HttpServerErrorException.InternalServerError ex) {
            return ResponseEntity.status(500).build();
        } catch (Exception ex) {
            return ResponseEntity.status(400).body(ex.getMessage());
        }
    }
}
