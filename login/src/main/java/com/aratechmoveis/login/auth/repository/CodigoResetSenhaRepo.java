package com.aratechmoveis.login.auth.repository;

import com.aratechmoveis.login.auth.entity.CodigoResetSenha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodigoResetSenhaRepo extends JpaRepository<CodigoResetSenha, Long> {
    Optional<CodigoResetSenha> findByCodigo(String codigo);
    void deleteByFuncionarioIdFuncionario(Long idFuncionario);
}
