package com.aratechmoveis.login.funcionario.repository;

import com.aratechmoveis.login.funcionario.entity.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {
    boolean existsByIdFuncionario(Long idFuncionario);
    boolean existsByEmailCorporativo(String email);
    Optional<Funcionario> findByEmailCorporativo(String email);
    Optional<Funcionario> findByEmailPessoal(String email);
    Optional<Funcionario> findByIdFuncionario(Long id);
}
