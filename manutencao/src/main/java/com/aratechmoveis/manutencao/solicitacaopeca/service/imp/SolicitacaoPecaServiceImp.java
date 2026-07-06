package com.aratechmoveis.manutencao.solicitacaopeca.service.imp;

import com.aratechmoveis.manutencao.Response;
import com.aratechmoveis.manutencao.exceptions.NotFoundException;
import com.aratechmoveis.manutencao.solicitacaopeca.dto.SolicitacaoPecaDTO;
import com.aratechmoveis.manutencao.solicitacaopeca.entity.SolicitacaoPeca;
import com.aratechmoveis.manutencao.solicitacaopeca.repository.SolicitacaoPecaRepository;
import com.aratechmoveis.manutencao.solicitacaopeca.service.SolicitacaoPecaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SolicitacaoPecaServiceImp implements SolicitacaoPecaService {

    private final SolicitacaoPecaRepository solicitacaoPecaRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Response adicionarSolicitacaoPeca(SolicitacaoPecaDTO solicitacaoPecaDTO) {

        SolicitacaoPeca solicitacaoParaSalvar = modelMapper.map(solicitacaoPecaDTO, SolicitacaoPeca.class);
        solicitacaoParaSalvar.setId(null);

        solicitacaoPecaRepository.save(solicitacaoParaSalvar);
        log.info("Solicitação de peça criada para equipamento={}, peça={}",
                solicitacaoParaSalvar.getEquipamento(), solicitacaoParaSalvar.getNomePeca());

        SolicitacaoPecaDTO solicitacaoCriada = modelMapper.map(solicitacaoParaSalvar, SolicitacaoPecaDTO.class);

        return Response.builder()
                .status(201)
                .message("Solicitação de peça criada com sucesso")
                .solicitacaoPeca(solicitacaoCriada)
                .build();
    }

    @Override
    public Response listarSolicitacoesPeca() {
        List<SolicitacaoPeca> solicitacoes = solicitacaoPecaRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<SolicitacaoPecaDTO> solicitacoesDTO = modelMapper.map(
                solicitacoes, new TypeToken<List<SolicitacaoPecaDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("Solicitações de peça listadas com sucesso")
                .solicitacoesPeca(solicitacoesDTO)
                .build();
    }

    @Override
    public Response buscarSolicitacaoPecaPorId(Long id) {
        SolicitacaoPeca solicitacao = solicitacaoPecaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Solicitação de peça não encontrada, confira se o id está correto"));

        return Response.builder()
                .status(200)
                .message("Solicitação de peça listada com sucesso")
                .solicitacaoPeca(modelMapper.map(solicitacao, SolicitacaoPecaDTO.class))
                .build();
    }

    @Override
    @Transactional
    public Response removerSolicitacaoPeca(Long id) {
        solicitacaoPecaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Solicitação de peça não encontrada, para deletar, confira se o id está correto"));

        solicitacaoPecaRepository.deleteById(id);

        return Response.builder()
                .status(204)
                .message("Solicitação de peça deletada com sucesso")
                .build();
    }
}
