package com.aratechmoveis.login.repository;

import com.aratechmoveis.login.entity.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {
    boolean existsByEmailCorporativoAndIdNot(String email, Long id);

}
