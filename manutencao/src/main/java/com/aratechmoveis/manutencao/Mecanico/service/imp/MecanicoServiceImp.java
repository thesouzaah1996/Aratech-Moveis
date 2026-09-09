package com.aratechmoveis.manutencao.Mecanico.service.imp;

import com.aratechmoveis.manutencao.Mecanico.dto.MecanicoDTO;
import com.aratechmoveis.manutencao.Mecanico.service.MecanicoService;
import com.aratechmoveis.manutencao.Response;
import com.aratechmoveis.manutencao.exceptions.NotFoundException;
import com.aratechmoveis.manutencao.exceptions.RecursoJaExistenteException;
import com.aratechmoveis.manutencao.Mecanico.dto.MecanicoLookupDTO;
import com.aratechmoveis.manutencao.Mecanico.entity.Mecanico;
import com.aratechmoveis.manutencao.Mecanico.repository.MecanicoRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MecanicoServiceImp implements MecanicoService {

    private final MecanicoRepository mecanicoRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Response adicionarMecanico(MecanicoDTO mecanicoDTO) {
        if (mecanicoRepository.existsByNomeIgnoreCase(mecanicoDTO.getNome())) {
            throw new RecursoJaExistenteException("Já existe um mecânico com esse nome");
        }

        Mecanico mecanicoParaSalvar = modelMapper.map(mecanicoDTO, Mecanico.class);
        mecanicoParaSalvar.setId(null);
        mecanicoParaSalvar.setAtivo(true);

        Mecanico mecanicoSalvo = mecanicoRepository.save(mecanicoParaSalvar);

        return Response.builder()
                .status(201)
                .message("Mecânico criado com sucesso")
                .mecanico(modelMapper.map(mecanicoSalvo, MecanicoDTO.class))
                .build();
    }

    @Override
    public Response listarMecanicos() {
        List<Mecanico> mecanicos = mecanicoRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<MecanicoDTO> mecanicosDTO = modelMapper.map(mecanicos, new TypeToken<List<MecanicoDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("Mecânicos listados com sucesso")
                .mecanicos(mecanicosDTO)
                .build();
    }

    @Override
    @Transactional
    public Response atualizarMecanico(Long id, MecanicoDTO mecanicoDTO) {
        Mecanico mecanicoExistente = mecanicoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Mecânico não encontrado, confira se o id está correto"));

        if (mecanicoDTO.getNome() != null && !mecanicoDTO.getNome().isBlank()) {
            if (mecanicoRepository.existsByNomeIgnoreCase(mecanicoDTO.getNome())
                    && !mecanicoExistente.getNome().equalsIgnoreCase(mecanicoDTO.getNome())) {
                throw new RecursoJaExistenteException("Já existe um mecânico com esse nome");
            }
            mecanicoExistente.setNome(mecanicoDTO.getNome());
        }

        if (mecanicoDTO.getEspecialidades() != null && !mecanicoDTO.getEspecialidades().isEmpty()) {
            mecanicoExistente.setEspecialidades(mecanicoDTO.getEspecialidades());
        }

        if (mecanicoDTO.getTurno() != null) {
            mecanicoExistente.setTurno(mecanicoDTO.getTurno());
        }

        mecanicoRepository.save(mecanicoExistente);

        return Response.builder()
                .status(200)
                .message("Mecânico atualizado com sucesso")
                .mecanico(modelMapper.map(mecanicoExistente, MecanicoDTO.class))
                .build();
    }

    @Override
    @Transactional
    public Response desativarMecanico(Long id) {
        Mecanico mecanico = mecanicoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Mecânico não encontrado, confira se o id está correto"));

        mecanico.setAtivo(false);
        mecanicoRepository.save(mecanico);

        return Response.builder()
                .status(200)
                .message("Mecânico desativado com sucesso")
                .mecanico(modelMapper.map(mecanico, MecanicoDTO.class))
                .build();
    }

    @Override
    @Transactional
    public Response ativarMecanico(Long id) {
        Mecanico mecanico = mecanicoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Mecânico não encontrado, confira se o id está correto"));

        mecanico.setAtivo(true);
        mecanicoRepository.save(mecanico);

        return Response.builder()
                .status(200)
                .message("Mecânico ativado com sucesso")
                .mecanico(modelMapper.map(mecanico, MecanicoDTO.class))
                .build();
    }

    @Override
    public Response buscarOpcoesMecanico() {
        List<MecanicoLookupDTO> lookup = mecanicoRepository.findByAtivoTrue()
                .stream()
                .map(mecanico -> new MecanicoLookupDTO(mecanico.getId(), mecanico.getNome(),
                        mecanico.getEspecialidades(), mecanico.getTurno()))
                .toList();

        return Response.builder()
                .status(200)
                .message("Lookup de mecânicos carregado com sucesso")
                .mecanicosLookup(lookup)
                .build();
    }
}
