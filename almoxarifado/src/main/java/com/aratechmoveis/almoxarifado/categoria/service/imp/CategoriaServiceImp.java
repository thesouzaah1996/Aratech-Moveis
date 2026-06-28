package com.aratechmoveis.almoxarifado.categoria.service.imp;

import com.aratechmoveis.almoxarifado.response.Response;
import com.aratechmoveis.almoxarifado.categoria.dto.CategoriaDTO;
import com.aratechmoveis.almoxarifado.categoria.dto.CategoriaLookupDTO;
import com.aratechmoveis.almoxarifado.categoria.modelo.Categoria;
import com.aratechmoveis.almoxarifado.categoria.repository.CategoriaRepository;
import com.aratechmoveis.almoxarifado.categoria.service.CategoriaService;
import com.aratechmoveis.almoxarifado.exceptions.NotFoundException;
import com.aratechmoveis.almoxarifado.exceptions.RecursoJaExistenteException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoriaServiceImp implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Response criarCategoria(CategoriaDTO categoriaDTO) {

        if (categoriaRepository.existsByNomeIgnoreCase(categoriaDTO.getNome())) {
            throw new RecursoJaExistenteException("Desculpe, mas essa categoria já existe");
        }

        Categoria categoriaParaSalvar = modelMapper.map(categoriaDTO, Categoria.class);
        categoriaRepository.save(categoriaParaSalvar);
        log.info("Categoria criada com nome={}", categoriaParaSalvar.getNome());

        CategoriaDTO categoriaCriadaDTO = modelMapper.map(categoriaParaSalvar, CategoriaDTO.class);

        return Response.builder()
                .status(201)
                .message("Categoria criada com sucesso")
                .categoria(categoriaCriadaDTO)
                .build();
    }

    @Override
    public Response getCategorias() {
        List<Categoria> categorias = categoriaRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<CategoriaDTO> categoriasDTO = modelMapper.map(categorias, new TypeToken<List<CategoriaDTO>>(){}.getType());

        return Response.builder()
                .message("Categorias listadas com sucesso")
                .status(200)
                .categorias(categoriasDTO)
                .build();
    }

    @Override
    @Transactional
    public Response updateCategoria(Long id, CategoriaDTO categoriaDTO) {
        Categoria categoriaExistente = categoriaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Categoria não encontrada, para prosseguir com a atualização, confira o id informado"));

        if (categoriaDTO.getNome() != null && !categoriaDTO.getNome().isBlank()) {
            if (categoriaRepository.existsByNomeIgnoreCase(categoriaDTO.getNome())
                    && !categoriaExistente.getNome().equalsIgnoreCase(categoriaDTO.getNome())) {
                throw new RecursoJaExistenteException("Já existe uma categoria com esse nome");
            }
            categoriaExistente.setNome(categoriaDTO.getNome());
        }

        categoriaRepository.save(categoriaExistente);
        log.info("Categoria com id={} alterada com sucesso", id);

        CategoriaDTO novaCategoria = modelMapper.map(categoriaExistente, CategoriaDTO.class);

        return Response.builder()
                .status(200)
                .message("Categoria alterada com sucesso")
                .categoria(novaCategoria)
                .build();
    }

    @Override
    @Transactional
    public Response deleteCategoria(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Categoria não encontrada, para prosseguir com a exclusão, verificar o id"));

        if (categoria.getProdutos() != null && !categoria.getProdutos().isEmpty()) {
            throw new RecursoJaExistenteException(
                    "Não é possível deletar: categoria possui " + categoria.getProdutos().size() + " produto(s) vinculado(s)");
        }

        categoriaRepository.deleteById(id);

        return Response.builder()
                .status(204)
                .message("Categoria deletada com sucesso")
                .build();
    }

    @Override
    public Response lookupCategoria() {
        List<CategoriaLookupDTO> lookup = categoriaRepository.findAll(Sort.by(Sort.Direction.ASC, "nome"))
                .stream()
                .map(categoria -> new CategoriaLookupDTO(categoria.getId(), categoria.getNome()))
                .toList();

        return Response.builder()
                .status(200)
                .message("Lookup de categorias carregado com sucesso")
                .categoriaLookup(lookup)
                .build();
    }
}
