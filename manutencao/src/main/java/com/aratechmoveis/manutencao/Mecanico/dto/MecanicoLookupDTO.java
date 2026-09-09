package com.aratechmoveis.manutencao.Mecanico.dto;

import com.aratechmoveis.manutencao.Mecanico.entity.Especialidade;
import com.aratechmoveis.manutencao.Mecanico.entity.Turno;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MecanicoLookupDTO {
    @EqualsAndHashCode.Include
    private Long id;
    private String nome;
    private List<Especialidade> especialidades;
    private Turno turno;
}
