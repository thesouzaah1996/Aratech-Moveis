package com.aratechmoveis.manutencao.chamado.service.imp;

import com.aratechmoveis.manutencao.Response;
import com.aratechmoveis.manutencao.chamado.dto.AtribuirMecanicoDTO;
import com.aratechmoveis.manutencao.chamado.dto.ChamadoDTO;
import com.aratechmoveis.manutencao.chamado.entity.Chamado;
import com.aratechmoveis.manutencao.chamado.entity.StatusChamado;
import com.aratechmoveis.manutencao.chamado.repository.ChamadoRepository;
import com.aratechmoveis.manutencao.chamado.service.ChamadoService;
import com.aratechmoveis.manutencao.exceptions.NotFoundException;
import com.aratechmoveis.manutencao.exceptions.RecursoJaExistenteException;
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
public class ChamadoServiceImp implements ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Response addChamado(ChamadoDTO chamadoDTO) {

        Chamado chamadoParaSalvar = modelMapper.map(chamadoDTO, Chamado.class);
        chamadoParaSalvar.setId(null);
        chamadoParaSalvar.setStatus(StatusChamado.ABERTA);
        chamadoParaSalvar.setMecanico(null);

        chamadoRepository.save(chamadoParaSalvar);
        log.info("Chamado de manutenção aberto para equipamento={}", chamadoParaSalvar.getEquipamento());

        ChamadoDTO chamadoCriado = modelMapper.map(chamadoParaSalvar, ChamadoDTO.class);

        return Response.builder()
                .status(201)
                .message("Chamado de manutenção criado com sucesso")
                .chamado(chamadoCriado)
                .build();
    }

    @Override
    public Response getChamados(StatusChamado status) {
        List<Chamado> chamados = status != null
                ? chamadoRepository.findByStatus(status, Sort.by(Sort.Direction.DESC, "dataAbertura"))
                : chamadoRepository.findAll(Sort.by(Sort.Direction.DESC, "dataAbertura"));

        List<ChamadoDTO> chamadosDTO = modelMapper.map(chamados, new TypeToken<List<ChamadoDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("Chamados listados com sucesso")
                .chamados(chamadosDTO)
                .build();
    }

    @Override
    public Response getChamadoById(Long id) {
        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Chamado não encontrado, confira se o id está correto"));

        return Response.builder()
                .status(200)
                .message("Chamado listado com sucesso")
                .chamado(modelMapper.map(chamado, ChamadoDTO.class))
                .build();
    }

    @Override
    @Transactional
    public Response atribuirMecanico(Long id, AtribuirMecanicoDTO atribuirMecanicoDTO) {
        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Chamado não encontrado, confira se o id está correto"));

        if (chamado.getStatus() == StatusChamado.CONCLUIDA) {
            throw new RecursoJaExistenteException("Não é possível atribuir um mecânico a um chamado já concluído");
        }

        chamado.setMecanico(atribuirMecanicoDTO.getMecanico());
        chamado.setStatus(StatusChamado.EM_MANUTENCAO);
        chamadoRepository.save(chamado);
        log.info("Chamado id={} atribuído ao mecânico={}", id, chamado.getMecanico());

        return Response.builder()
                .status(200)
                .message("Chamado atribuído com sucesso")
                .chamado(modelMapper.map(chamado, ChamadoDTO.class))
                .build();
    }

    @Override
    @Transactional
    public Response concluirChamado(Long id) {
        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Chamado não encontrado, confira se o id está correto"));

        if (chamado.getStatus() != StatusChamado.EM_MANUTENCAO) {
            throw new RecursoJaExistenteException("Somente chamados em manutenção podem ser concluídos");
        }

        chamado.setStatus(StatusChamado.CONCLUIDA);
        chamadoRepository.save(chamado);
        log.info("Chamado id={} concluído", id);

        return Response.builder()
                .status(200)
                .message("Chamado concluído com sucesso")
                .chamado(modelMapper.map(chamado, ChamadoDTO.class))
                .build();
    }

    @Override
    @Transactional
    public Response deleteChamado(Long id) {
        chamadoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Chamado não encontrado, para deletar, confira se o id está correto"));

        chamadoRepository.deleteById(id);

        return Response.builder()
                .status(204)
                .message("Chamado deletado com sucesso")
                .build();
    }
}
