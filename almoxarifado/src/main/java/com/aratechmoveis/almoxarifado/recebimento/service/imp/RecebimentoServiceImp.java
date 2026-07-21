package com.aratechmoveis.almoxarifado.recebimento.service.imp;

import com.aratechmoveis.almoxarifado.Response;
import com.aratechmoveis.almoxarifado.exceptions.NotFoundException;
import com.aratechmoveis.almoxarifado.exceptions.RecursoJaExistenteException;
import com.aratechmoveis.almoxarifado.recebimento.dto.RecebimentoDTO;
import com.aratechmoveis.almoxarifado.recebimento.entity.Recebimento;
import com.aratechmoveis.almoxarifado.recebimento.entity.StatusRecebimento;
import com.aratechmoveis.almoxarifado.recebimento.publisher.RecebimentoPublisher;
import com.aratechmoveis.almoxarifado.recebimento.repository.RecebimentoRepository;
import com.aratechmoveis.almoxarifado.recebimento.service.RecebimentoService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecebimentoServiceImp implements RecebimentoService {

    private final RecebimentoRepository recebimentoRepository;
    private final RecebimentoPublisher recebimentoPublisher;
    private final ModelMapper modelMapper;

    @Override
    public Response adicionarRecebimento(RecebimentoDTO recebimentoDTO) {

        if (recebimentoRepository.existsByNotaFiscal(recebimentoDTO.getNotaFiscal())) {
            throw new RecursoJaExistenteException("Carga já recebida. Nota fiscal={}" + recebimentoDTO.getNotaFiscal());
        }

        Recebimento novoRecebimento = modelMapper.map(recebimentoDTO, Recebimento.class);

        recebimentoRepository.save(novoRecebimento);

        return Response.builder()
                .status(201)
                .mensagem("Carregamento recebido")
                .carga(modelMapper.map(novoRecebimento, RecebimentoDTO.class))
                .build();
    }

    @Override
    public Response listarRecebimentos() {
        List<Recebimento> recebimentos = recebimentoRepository.findAll(Sort.by(Sort.Direction.ASC, "dataRecebimento"));

        List<RecebimentoDTO> recebimentosDTO = modelMapper.map(recebimentos, new TypeToken<List<RecebimentoDTO>>() {
        }.getType());

        return Response.builder()
                .status(200)
                .mensagem("Recebimentos")
                .recebimentos(recebimentosDTO)
                .build();
    }

    @Override
    public Response buscarHistorico() {
        List<Recebimento> historico = recebimentoRepository.findByStatusRecebimento(
                StatusRecebimento.FINALIZADO, Sort.by(Sort.Direction.DESC, "dataRecebimento"));

        List<RecebimentoDTO> historicoDTO = modelMapper.map(historico, new TypeToken<List<RecebimentoDTO>>() {
        }.getType());

        return Response.builder()
                .status(200)
                .mensagem("Histórico de recebimentos listado com sucesso")
                .historicoRecebimentos(historicoDTO)
                .build();
    }

    @Override
    @Transactional
    public Response autorizarRecebimento(String notaFiscal) {
        Recebimento recebimento = recebimentoRepository.findByNotaFiscal(notaFiscal)
                .orElseThrow(() -> new NotFoundException("Recebimento com nota fiscal " + notaFiscal + " não encontrado, confira se a nota fiscal está correta"));

        recebimento.setStatusRecebimento(StatusRecebimento.AUTORIZADO);
        recebimentoPublisher.publicarLiberacaoRecebimento(recebimento.getNotaFiscal(), recebimento.getStatusRecebimento());

        recebimento.setStatusRecebimento(StatusRecebimento.EM_RECEBIMENTO);
        recebimentoRepository.save(recebimento);

        return Response.builder()
                .status(200)
                .mensagem("Recebimento autorizado")
                .carga(modelMapper.map(recebimento, RecebimentoDTO.class))
                .build();
    }

    @Override
    public Response finalizarRecebimento(String notaFiscal) {
        Recebimento recebimento = recebimentoRepository.findByNotaFiscal(notaFiscal)
                .orElseThrow(() -> new NotFoundException("Recebimento com nota fiscal " + notaFiscal + " não encontrado, confira se a nota fiscal está correta"));

        recebimento.setStatusRecebimento(StatusRecebimento.FINALIZADO);
        recebimentoRepository.save(recebimento);

        return Response.builder()
                .status(200)
                .mensagem("Recebimento finalizado")
                .carga(modelMapper.map(recebimento, RecebimentoDTO.class))
                .build();
    }
}
