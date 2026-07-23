package com.aratechmoveis.portaria.controle_acesso.service.imp;

import com.aratechmoveis.portaria.controle_acesso.publisher.RegistroChegadaPublisher;
import com.aratechmoveis.portaria.controle_acesso.publisher.TipoRegistroChegada;
import com.aratechmoveis.portaria.response.Response;
import com.aratechmoveis.portaria.controle_acesso.dto.RegistroChegadaDTO;
import com.aratechmoveis.portaria.controle_acesso.entity.RegistroChegada;
import com.aratechmoveis.portaria.controle_acesso.entity.StatusCaminhao;
import com.aratechmoveis.portaria.controle_acesso.repository.RegistroChegadaRepository;
import com.aratechmoveis.portaria.controle_acesso.service.RegistroChegadaService;
import com.aratechmoveis.portaria.exceptions.NotFoundException;
import com.aratechmoveis.portaria.exceptions.RecursoJaExistenteException;
import com.aratechmoveis.portaria.exceptions.RegistroNaoAutorizadoException;
import jakarta.transaction.TransactionScoped;
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
    private final RegistroChegadaPublisher registroChegadaPublisher;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Response adicionarRegistroChegada(RegistroChegadaDTO registroChegadaDTO) {

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

        registroChegadaPublisher.publicarRegistroChegada(
                TipoRegistroChegada.CHEGADA_REGISTRADA,
                registroParaSalvar.getNotaFiscal(),
                registroParaSalvar.getEmpresa(),
                registroParaSalvar.getNomeMotorista(),
                registroParaSalvar.getPlaca(),
                registroParaSalvar.getDescricaoCarga(),
                registroParaSalvar.getSetorResponsavel());
        log.info("Enviando registro para o setor responsável.");

        RegistroChegadaDTO registroCriado = modelMapper.map(registroParaSalvar, RegistroChegadaDTO.class);

        return Response.builder()
                .status(201)
                .mensagem("Registro de chegada salvo com sucesso")
                .registroChegada(registroCriado)
                .build();
    }

    @Override
    public Response buscarFila() {
        List<RegistroChegada> fila = registroChegadaRepository.findByStatusNot(
                StatusCaminhao.FINALIZADO, Sort.by(Sort.Direction.ASC, "dataRecebimento"));

        List<RegistroChegadaDTO> filaDTO = modelMapper.map(fila, new TypeToken<List<RegistroChegadaDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .mensagem("Fila de espera listada com sucesso")
                .filaRegistroChegada(filaDTO)
                .build();
    }

    @Override
    public Response buscarHistorico() {
        List<RegistroChegada> historico = registroChegadaRepository.findByStatus(
                StatusCaminhao.FINALIZADO, Sort.by(Sort.Direction.DESC, "dataChegada"));

        List<RegistroChegadaDTO> historicoDTO = modelMapper.map(historico, new TypeToken<List<RegistroChegadaDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .mensagem("Histórico de entregas listado com sucesso")
                .historicoRegistroChegada(historicoDTO)
                .build();
    }

    @Override
    public Response listarAutorizados(StatusCaminhao statusCaminhao) {
        List<RegistroChegada> autorizados = registroChegadaRepository.findByStatus(
                statusCaminhao, Sort.by(Sort.Direction.ASC, "dataChegada"));

        List<RegistroChegadaDTO> autorizadosDTO = modelMapper.map(autorizados, new TypeToken<List<RegistroChegadaDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .mensagem("Cargas autorizadas listadas com sucesso")
                .autorizados(autorizadosDTO)
                .build();
    }

    @Override
    @Transactional
    public Response autorizarRecebimento(String notaFiscal) {
        RegistroChegada registro = registroChegadaRepository.findByNotaFiscal(notaFiscal)
                .orElseThrow(() -> new NotFoundException("Registro de chegada não encontrado para a nota fiscal: " + notaFiscal));

        registro.setStatus(StatusCaminhao.AUTORIZADO);
        registroChegadaRepository.save(registro);
        log.info("Registro de chegada notaFiscal={} autorizado para entrada", notaFiscal);

        return Response.builder()
                .status(200)
                .mensagem("Registro de chegada autorizado com sucesso")
                .registroChegada(modelMapper.map(registro, RegistroChegadaDTO.class))
                .build();
    }

    @Override
    @Transactional
    public Response atualizarRegistroChegada(RegistroChegadaDTO registroChegadaDTO) {
        RegistroChegada registro = registroChegadaRepository.findByNotaFiscal(registroChegadaDTO.getNotaFiscal())
                .orElseThrow(() -> new NotFoundException("Registro de chegada não encontrado para a nota fiscal: " + registroChegadaDTO.getNotaFiscal()));

        registro.setEmpresa(registroChegadaDTO.getEmpresa());
        registro.setNomeMotorista(registroChegadaDTO.getNomeMotorista());
        registro.setPlaca(registroChegadaDTO.getPlaca());
        registro.setDescricaoCarga(registroChegadaDTO.getDescricaoCarga());
        registro.setSetorResponsavel(registroChegadaDTO.getSetorResponsavel());

        registroChegadaRepository.save(registro);
        log.info("Registro de chegada notaFiscal={} atualizado", registro.getNotaFiscal());

        registroChegadaPublisher.publicarRegistroChegada(
                TipoRegistroChegada.DADOS_CORRIGIDOS,
                registro.getNotaFiscal(),
                registro.getEmpresa(),
                registro.getNomeMotorista(),
                registro.getPlaca(),
                registro.getDescricaoCarga(),
                registro.getSetorResponsavel());
        log.info("Enviando correção de dados para o setor responsável.");

        return Response.builder()
                .status(200)
                .mensagem("Registro de chegada atualizado com sucesso")
                .registroChegada(modelMapper.map(registro, RegistroChegadaDTO.class))
                .build();
    }

    @Override
    @Transactional
    public Response finalizarRegistro(String notaFiscal) {
        RegistroChegada registro = registroChegadaRepository.findByNotaFiscal(notaFiscal)
                .orElseThrow(() -> new NotFoundException("Registro de chegada não encontrado para a nota fiscal: " + notaFiscal));
        registro.setStatus(StatusCaminhao.FINALIZADO);

        registroChegadaRepository.save(registro);

        return Response.builder()
                .status(200)
                .mensagem("Registro de chegada finalizado com sucesso.")
                .registroChegada(modelMapper.map(registro, RegistroChegadaDTO.class))
                .build();
    }
}
