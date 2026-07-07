package com.aratechmoveis.recursoshumanos.funcionarios.repository;

import com.aratechmoveis.recursoshumanos.funcionarios.entity.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    @Query(value = """
            SELECT * FROM funcionarios f
            WHERE unaccent(upper(f.nome)) LIKE unaccent(upper('%' || :nome || '%'))
            ORDER BY f.nome
            """, nativeQuery = true)
    List<Funcionario> findByNomeContainingIgnoreCase(@Param("nome") String nome);

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    boolean existsByEmailPessoal(String emailPessoal);

    boolean existsByEmailPessoalAndIdNot(String email, Long id);

    boolean existsByEmailCorporativoAndIdNot(String email, Long id);
}
