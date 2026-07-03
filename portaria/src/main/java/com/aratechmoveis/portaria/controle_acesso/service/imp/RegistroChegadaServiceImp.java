package com.aratechmoveis.portaria.controle_acesso.service.imp;

import com.aratechmoveis.portaria.response.Response;
import com.aratechmoveis.portaria.controle_acesso.dto.RegistroChegadaDTO;
import com.aratechmoveis.portaria.controle_acesso.entity.RegistroChegada;
import com.aratechmoveis.portaria.controle_acesso.entity.StatusCaminhao;
import com.aratechmoveis.portaria.controle_acesso.repository.RegistroChegadaRepository;
import com.aratechmoveis.portaria.controle_acesso.service.RegistroChegadaService;
import com.aratechmoveis.portaria.exceptions.NotFoundException;
import com.aratechmoveis.portaria.exceptions.RecursoJaExistenteException;
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
public class RegistroChegadaServiceImp implements RegistroChegadaService {

    private final RegistroChegadaRepository registroChegadaRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Response addRegistroChegada(RegistroChegadaDTO registroChegadaDTO) {

        if (registroChegadaRepository.existsByNotaFiscal(registroChegadaDTO.getNotaFiscal())) {
            throw new RecursoJaExistenteException(
                    "Já existe um registro de chegada para a nota fiscal: " + registroChegadaDTO.getNotaFiscal()
            );
        }

        RegistroChegada registroParaSalvar = modelMapper.map(registroChegadaDTO, RegistroChegada.class);
        registroParaSalvar.setId(null);
        registroParaSalvar.setStatus(StatusCaminhao.AGUARDANDO);

        registroChegadaRepository.save(registroParaSalvar);
        log.info("Registro de chegada criado para notaFiscal={}, placa={}",
                registroParaSalvar.getNotaFiscal(), registroParaSalvar.getPlaca());

        RegistroChegadaDTO registroCriado = modelMapper.map(registroParaSalvar, RegistroChegadaDTO.class);

        return Response.builder()
                .status(201)
                .message("Registro de chegada salvo com sucesso")
                .registroChegada(registroCriado)
                .build();
    }

    @Override
    public Response getFila() {
        List<RegistroChegada> fila = registroChegadaRepository.findByStatusNot(
                StatusCaminhao.FINALIZADO, Sort.by(Sort.Direction.ASC, "dataEntrada"));

        List<RegistroChegadaDTO> filaDTO = modelMapper.map(fila, new TypeToken<List<RegistroChegadaDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("Fila de espera listada com sucesso")
                .filaRegistroChegada(filaDTO)
                .build();
    }

    @Override
    public Response getHistorico() {
        List<RegistroChegada> historico = registroChegadaRepository.findByStatus(
                StatusCaminhao.FINALIZADO, Sort.by(Sort.Direction.DESC, "dataEntrada"));

        List<RegistroChegadaDTO> historicoDTO = modelMapper.map(historico, new TypeToken<List<RegistroChegadaDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("Histórico de entregas listado com sucesso")
                .historicoRegistroChegada(historicoDTO)
                .build();
    }

    @Override
    @Transactional
    public Response finalizarRegistro(Long id) {
        RegistroChegada registro = registroChegadaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Registro de chegada não encontrado, confira se o id está correto"));

        if (registro.getStatus() == StatusCaminhao.FINALIZADO) {
            throw new RecursoJaExistenteException("Este registro de chegada já está finalizado");
        }

        registro.setStatus(StatusCaminhao.FINALIZADO);
        registroChegadaRepository.save(registro);
        log.info("Registro de chegada id={} finalizado", id);

        return Response.builder()
                .status(200)
                .message("Registro de chegada finalizado com sucesso")
                .registroChegada(modelMapper.map(registro, RegistroChegadaDTO.class))
                .build();
    }
}
