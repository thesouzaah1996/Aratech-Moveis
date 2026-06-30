package com.aratechmoveis.recursoshumanos.funcionarios.repository;

import com.aratechmoveis.recursoshumanos.funcionarios.model.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {
}
