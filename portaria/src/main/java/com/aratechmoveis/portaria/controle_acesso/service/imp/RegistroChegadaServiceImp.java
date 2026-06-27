package com.aratechmoveis.portaria.controle_acesso.service.imp;

import com.aratechmoveis.portaria.Response;
import com.aratechmoveis.portaria.controle_acesso.dto.RegistroChegadaDTO;
import com.aratechmoveis.portaria.controle_acesso.model.RegistroChegada;
import com.aratechmoveis.portaria.controle_acesso.repository.RegistroChegadaRepository;
import com.aratechmoveis.portaria.controle_acesso.service.RegistroChegadaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistroChegadaServiceImp implements RegistroChegadaService {

    private final RegistroChegadaRepository registroChegadaRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response addRegistroChegada(RegistroChegadaDTO registroChegadaDTO) {
        try {

            RegistroChegada registro = modelMapper.map(registroChegadaDTO, RegistroChegada.class);
            RegistroChegadaDTO registroChegada = modelMapper.map(registro, RegistroChegadaDTO.class);

            registroChegadaRepository.save(registro);
            log.info("Registro salvo com sucesso.");

       return Response.builder()
                    .message("Registro de chegada salvo com sucesso.")
                    .status(201)
                    .registroChegada(registroChegada)
                    .build();

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return Response.builder().build();
    }

    @Override
    public Response getRegistrosChegada() {
       List<RegistroChegada> registroChegadas = registroChegadaRepository.findAll();
       List<RegistroChegadaDTO> registroChegadaDTO = registroChegadas.stream()
               .map(r -> modelMapper.map(r, RegistroChegadaDTO.class))
               .collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .message("Registros listados com sucesso.")
                .registrosChegada(registroChegadaDTO)
                .build();
    }
}
