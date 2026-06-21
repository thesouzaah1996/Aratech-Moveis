package com.aratechmoveis.almoxarifado;

import com.aratechmoveis.almoxarifado.fornecedor.dto.FornecedorDTO;
import com.aratechmoveis.almoxarifado.fornecedor.model.Fornecedor;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {

    private int status;
    private String message;

    private String token;
//    private PerfilAcesso perfilAcesso;
    private String expirationTime;

    private Fornecedor fornecedor;
    private FornecedorDTO fornecedorDTO;
    private List<FornecedorDTO> fornecedores;

    private final LocalDateTime timestamp = LocalDateTime.now();
}
