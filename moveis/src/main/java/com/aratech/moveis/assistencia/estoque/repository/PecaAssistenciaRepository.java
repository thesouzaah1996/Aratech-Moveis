package com.aratech.moveis.assistencia.estoque.repository;

import com.aratech.moveis.assistencia.estoque.model.PecaAssistencia;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PecaAssistenciaRepository extends JpaRepository<PecaAssistencia, Long> {
    boolean existsByCodigoIgnoreCase(String codigo);

    @Query(
            "select p from PecaAssistencia p where p.ativo=true and (:abaixoMinimo=false or"
                    + " p.quantidade<=p.quantidadeMinima) and (:busca='' or lower(p.nome) like"
                    + " lower(concat('%',:busca,'%')) escape '!' or lower(p.codigo) like"
                    + " lower(concat('%',:busca,'%')) escape '!' or lower(p.modelo) like"
                    + " lower(concat('%',:busca,'%')) escape '!' or lower(p.localizacao) like"
                    + " lower(concat('%',:busca,'%')) escape '!' or lower(p.descricao) like"
                    + " lower(concat('%',:busca,'%')) escape '!')")
    Page<PecaAssistencia> buscar(
            @Param("busca") String busca,
            @Param("abaixoMinimo") boolean abaixoMinimo,
            Pageable pageable);

    @Query(
            "select count(p) from PecaAssistencia p where p.ativo=true and"
                    + " p.quantidade<=p.quantidadeMinima")
    long contarAbaixoMinimo();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PecaAssistencia p where p.id=:id")
    Optional<PecaAssistencia> findByIdForUpdate(@Param("id") Long id);
}
