package com.aratechmoveis.recursoshumanos.funcionarios.repository;

import com.aratechmoveis.recursoshumanos.funcionarios.entity.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    List<Funcionario> findByNomeContainingIgnoreCase(String nome);
}
