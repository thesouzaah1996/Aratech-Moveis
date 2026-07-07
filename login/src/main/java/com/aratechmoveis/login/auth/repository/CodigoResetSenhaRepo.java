package com.aratechmoveis.login.auth.repository;

import com.aratechmoveis.login.auth.entity.CodigoResetSenha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodigoResetSenhaRepo extends JpaRepository<CodigoResetSenha, Long> {
    Optional<CodigoResetSenha> findByCode(String code);
    void deleteByUserId(Long idUsuario);
}
