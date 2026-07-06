package com.aratechmoveis.manutencao.pecaestoque.service.imp;

import com.aratechmoveis.manutencao.Response;
import com.aratechmoveis.manutencao.exceptions.NotFoundException;
import com.aratechmoveis.manutencao.exceptions.RecursoJaExistenteException;
import com.aratechmoveis.manutencao.pecaestoque.dto.PecaEstoqueDTO;
import com.aratechmoveis.manutencao.pecaestoque.entity.PecaEstoque;
import com.aratechmoveis.manutencao.pecaestoque.repository.PecaEstoqueRepository;
import com.aratechmoveis.manutencao.pecaestoque.service.PecaEstoqueService;
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
public class PecaEstoqueServiceImp implements PecaEstoqueService {

    private final PecaEstoqueRepository pecaEstoqueRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Response adicionarPecaEstoque(PecaEstoqueDTO pecaEstoqueDTO) {

        if (pecaEstoqueRepository.existsByCodigoIgnoreCase(pecaEstoqueDTO.getCodigo())) {
            throw new RecursoJaExistenteException(
                    "Já existe uma peça cadastrada com o código: " + pecaEstoqueDTO.getCodigo()
            );
        }

        PecaEstoque pecaParaSalvar = modelMapper.map(pecaEstoqueDTO, PecaEstoque.class);
        pecaEstoqueRepository.save(pecaParaSalvar);
        log.info("Peça de estoque criada com código={}", pecaParaSalvar.getCodigo());

        PecaEstoqueDTO pecaCriada = modelMapper.map(pecaParaSalvar, PecaEstoqueDTO.class);

        return Response.builder()
                .status(201)
                .message("Peça cadastrada com sucesso")
                .pecaEstoque(pecaCriada)
                .build();
    }

    @Override
    @Transactional
    public Response atualizarPecaEstoque(Long id, PecaEstoqueDTO pecaEstoqueDTO) {
        PecaEstoque pecaExistente = pecaEstoqueRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Peça não encontrada"));

        if (pecaEstoqueDTO.getNome() != null && !pecaEstoqueDTO.getNome().isBlank()) {
            pecaExistente.setNome(pecaEstoqueDTO.getNome());
        }

        if (pecaEstoqueDTO.getCodigo() != null && !pecaEstoqueDTO.getCodigo().isBlank()) {
            if (pecaEstoqueRepository.existsByCodigoIgnoreCaseAndIdNot(pecaEstoqueDTO.getCodigo(), id)) {
                throw new RecursoJaExistenteException(
                        "Já existe uma peça cadastrada com o código: " + pecaEstoqueDTO.getCodigo()
                );
            }
            pecaExistente.setCodigo(pecaEstoqueDTO.getCodigo());
        }

        if (pecaEstoqueDTO.getQuantidade() != null && pecaEstoqueDTO.getQuantidade() >= 0) {
            pecaExistente.setQuantidade(pecaEstoqueDTO.getQuantidade());
        }

        if (pecaEstoqueDTO.getUnidade() != null && !pecaEstoqueDTO.getUnidade().isBlank()) {
            pecaExistente.setUnidade(pecaEstoqueDTO.getUnidade());
        }

        if (pecaEstoqueDTO.getLocalizacao() != null && !pecaEstoqueDTO.getLocalizacao().isBlank()) {
            pecaExistente.setLocalizacao(pecaEstoqueDTO.getLocalizacao());
        }

        if (pecaEstoqueDTO.getDescricao() != null && !pecaEstoqueDTO.getDescricao().isBlank()) {
            pecaExistente.setDescricao(pecaEstoqueDTO.getDescricao());
        }

        pecaEstoqueRepository.save(pecaExistente);
        log.info("Peça de estoque id={} atualizada com sucesso", id);

        return Response.builder()
                .status(200)
                .message("Peça atualizada com sucesso")
                .pecaEstoque(modelMapper.map(pecaExistente, PecaEstoqueDTO.class))
                .build();
    }

    @Override
    public Response listarPecasEstoque() {
        List<PecaEstoque> pecas = pecaEstoqueRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<PecaEstoqueDTO> pecasDTO = modelMapper.map(pecas, new TypeToken<List<PecaEstoqueDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("Peças listadas com sucesso")
                .pecasEstoque(pecasDTO)
                .build();
    }

    @Override
    public Response buscarPecaEstoquePorId(Long id) {
        PecaEstoque peca = pecaEstoqueRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Peça não encontrada, confira se o id está correto"));

        return Response.builder()
                .status(200)
                .message("Peça listada com sucesso")
                .pecaEstoque(modelMapper.map(peca, PecaEstoqueDTO.class))
                .build();
    }

    @Override
    @Transactional
    public Response removerPecaEstoque(Long id) {
        pecaEstoqueRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Peça não encontrada, para deletar, confira se o id está correto"));

        pecaEstoqueRepository.deleteById(id);

        return Response.builder()
                .status(204)
                .message("Peça deletada com sucesso")
                .build();
    }
}
