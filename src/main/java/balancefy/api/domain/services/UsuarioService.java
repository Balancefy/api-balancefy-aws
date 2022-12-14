package balancefy.api.domain.services;

import balancefy.api.application.dto.request.UsuarioEditRequest;
import balancefy.api.application.dto.request.UsuarioSenhaRequestDto;
import balancefy.api.application.utils.FileUploadUtil;
import balancefy.api.domain.exceptions.NotFoundException;
import balancefy.api.resources.enums.TypeUser;
import balancefy.api.resources.entities.Usuario;
import balancefy.api.domain.exceptions.AlreadyExistsException;
import balancefy.api.resources.repositories.ContaRepository;
import balancefy.api.resources.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@Service
public class UsuarioService {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @Autowired
    private ContaRepository contaRepository;

    public Usuario create(Usuario usuario) throws AlreadyExistsException {
        try {
            Optional<Usuario> foundUser = usuarioRepository.findByEmail(usuario.getEmail());
            if(!foundUser.isPresent()) {
                if(usuario.getTipo() == TypeUser.DEFAULT) {
                    usuario.setSenha(BCrypt.hashpw(usuario.getSenha(), BCrypt.gensalt()));
                }

                return usuarioRepository.save(usuario);
            }

            throw new AlreadyExistsException("Email já cadastrado");
        } catch (Exception ex) {
            throw ex;
        }
    }

    public Usuario update(UsuarioEditRequest usuarioRequest, int id) throws NotFoundException {
        try {
            if (usuarioRepository.existsById(id)) {
                Usuario usuario = usuarioRepository.findById(id).get();

                if(!usuarioRequest.getEmail().equals("")) {
                    usuario.setEmail(usuarioRequest.getEmail());
                }

                if(!usuarioRequest.getNome().equals("")) {
                    usuario.setNome(usuarioRequest.getNome());
                }

                return usuarioRepository.save(usuario);
            }

            throw new NotFoundException("Usuário não encontrado");
        } catch (Exception ex) {
            throw ex;
        }
    }

    public String  updateAvatar(MultipartFile multipartFile, Integer id) throws IOException, NotFoundException {
        try {
            if (usuarioRepository.existsById(id)) {
                String fileName = id+"avatar/"+StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));

                usuarioRepository.updateAvatar(fileName, id);

                fileUploadUtil.saveFile(fileName, multipartFile);
                return "https://balancefy-d.s3.amazonaws.com/"+fileName;
            }

            throw new NotFoundException("Usuário não encontrado");
        } catch (Exception ex) {
            throw ex;
        }
    }

    public void updateBanner(MultipartFile multipartFile, Integer id) throws IOException, NotFoundException {
        try {
            if (usuarioRepository.existsById(id)) {
                String fileName = id+"banner/"+StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));

                usuarioRepository.updateBanner(fileName, id);

                fileUploadUtil.saveFile(fileName, multipartFile);

                return;
            }

            throw new NotFoundException("Usuário não encontrado");
        } catch (Exception ex) {
            throw ex;
        }
    }

    public void updatePassword(Integer id, UsuarioSenhaRequestDto request) throws IOException, NotFoundException {
        try {
            Optional<Usuario> usuario = usuarioRepository.findById(id);
            if (usuario.isPresent()) {
                if (BCrypt.checkpw(request.getSenhaAtual(), usuario.get().getSenha())) {
                    usuario.get().setSenha(BCrypt.hashpw(request.getNovaSenha(), BCrypt.gensalt()));
                    usuarioRepository.save(usuario.get());
                    return;
                } else {
                    throw new NotFoundException("Senha Invalida");
                }
            }

            throw new NotFoundException("Usuário não encontrado");
        } catch (Exception ex) {
            throw ex;
        }
    }
}
