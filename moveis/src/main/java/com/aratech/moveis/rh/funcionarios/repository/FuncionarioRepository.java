package com.aratech.moveis.rh.funcionarios.repository;

import com.aratech.moveis.rh.funcionarios.model.Funcionario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    List<Funcionario> findByNomeCompletoContainingIgnoreCase(String nomeCompleto);

    List<Funcionario> findByAtivoFalse();

    Optional<Funcionario> findByCpf(String cpf);

    Optional<Funcionario> findByEmail(String email);

    boolean existsByCpf(String cpf);

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPisNitNis(String pisNitNis);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByPisNitNisAndIdNot(String pisNitNis, Long id);

    @org.springframework.data.jpa.repository.Lock(
            jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select f from Funcionario f where f.id = :id")
    Optional<Funcionario> findByIdForUpdate(
            @org.springframework.data.repository.query.Param("id") Long id);
}
