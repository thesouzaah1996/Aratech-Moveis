package com.aratech.moveis.rh.ponto.service.imp;

import com.aratech.moveis.rh.ponto.dto.EscalaPontoDTO;
import com.aratech.moveis.rh.ponto.dto.MarcacaoPontoDTO;
import com.aratech.moveis.rh.ponto.repository.EscalaPontoRepository;

import com.aratech.moveis.rh.ponto.dto.DiaPontoDTO;
import com.aratech.moveis.rh.ponto.dto.EdicaoPontoDTO;
import com.aratech.moveis.rh.ponto.dto.FuncionarioPontoDTO;
import com.aratech.moveis.rh.ponto.dto.HistoricoPontoDTO;
import com.aratech.moveis.rh.ponto.repository.PontoRepository;
import com.aratech.moveis.rh.ponto.service.PontoService;
import com.aratech.moveis.security.authorization.Permissoes;
import com.aratech.moveis.security.dto.UsuarioSessao;
import com.aratech.moveis.security.exception.AuthException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PontoServiceImp implements PontoService {
    private final PontoRepository repository;
    private final EscalaPontoRepository escalas;

    public List<EscalaPontoDTO> escala(Long id) {
        funcionario(id);
        return escalas.listar(id);
    }

    @Transactional
    public void salvarEscala(Long id, List<EscalaPontoDTO> dias, UsuarioSessao autor) {
        if (autor == null || !autor.perfis().contains("#") || !Permissoes.permite(autor, "RH_PONTO.ALTERAR"))
            throw new AuthException(403, "A escala exige o perfil # e RH_PONTO.ALTERAR.");
        if (repository.bloquearFuncionario(id).isEmpty()) throw new AuthException(404, "Funcionário não encontrado.");
        if (dias == null || dias.size() != 7 || dias.stream().anyMatch(Objects::isNull) || dias.stream().map(d -> d.diaSemana()).distinct().count() != 7)
            throw new AuthException(400, "Informe os sete dias da semana, sem repetições.");
        int total = 0;
        for (var dia : dias) {
            if (dia.diaSemana() < 1 || dia.diaSemana() > 7 || dia.extraMinutos() < 0 || dia.extraMinutos() > 120)
                throw new AuthException(400, "Dia ou limite de horas extras inválido (0 a 120 minutos).");
            var horarios = dia.horarios();
            if (horarios == null || horarios.size() != 6) throw new AuthException(400, "Informe seis horários por dia.");
            int anterior = -1, quantidade = 0;
            boolean vazio = false;
            for (String h : horarios) {
                if (h == null || !h.isEmpty() && !h.matches("([01][0-9]|2[0-3]):[0-5][0-9]"))
                    throw new AuthException(400, "Horário inválido; use HH:mm ou vazio.");
                if (h.isEmpty()) { vazio = true; continue; }
                int atual = LocalTime.parse(h).toSecondOfDay() / 60;
                if (vazio || atual <= anterior) throw new AuthException(400, "Preencha pares em ordem crescente, no mesmo dia.");
                anterior = atual; quantidade++;
            }
            if (quantidade % 2 != 0) throw new AuthException(400, "A escala deve conter pares completos.");
            total += new DiaPontoDTO(LocalDate.now(), horarios, 0, false).totalMinutos();
        }
        if (total > escalas.jornada(id)) throw new AuthException(400, "A escala ultrapassa a jornada semanal cadastrada.");
        escalas.salvar(id, dias);
    }

    private DiaPontoDTO avaliar(DiaPontoDTO dia, List<EscalaPontoDTO> escala) {
        if (dia.horarios().stream().allMatch(String::isEmpty))
            return new DiaPontoDTO(dia.data(), dia.horarios(), dia.versao(), dia.ajustado(), "SEM_MARCACAO", "", 0);
        var previsto = escala.stream().filter(e -> e.diaSemana() == dia.data().getDayOfWeek().getValue()).findFirst();
        if (previsto.isEmpty()) return new DiaPontoDTO(dia.data(), dia.horarios(), dia.versao(), dia.ajustado(), "SEM_ESCALA", "Escala não definida. Solicite a configuração ao responsável pelo ponto.", dia.totalMinutos());
        var e = previsto.get();
        int jornada = new DiaPontoDTO(dia.data(), e.horarios(), 0, false).totalMinutos();
        boolean divergente = false;
        for (int i = 0; i < 6; i++) {
            String h = dia.horarios().get(i);
            if (!h.isEmpty() && !h.equals(e.horarios().get(i))) divergente = true;
        }
        String aviso = dia.totalMinutos() > jornada + e.extraMinutos()
                ? "Limite da jornada e das horas extras autorizadas ultrapassado. Procure o responsável pelo ponto."
                : dia.totalMinutos() > jornada ? "Jornada prevista ultrapassada; há horas extras dentro do limite autorizado." : "";
        return new DiaPontoDTO(dia.data(), dia.horarios(), dia.versao(), dia.ajustado(), divergente ? "FORA_HORARIO" : "NO_HORARIO", aviso, dia.totalMinutos());
    }

    @Transactional
    public DiaPontoDTO marcar(MarcacaoPontoDTO dto, UsuarioSessao usuario) {
        if (usuario == null || usuario.trocarSenha())
            throw new AuthException(401, "Sua sessão expirou. Entre novamente.");
        if (usuario.id() == null || usuario.id() <= 0)
            throw new AuthException(403, "A conta técnica de administração não possui ponto. Entre com a conta de um funcionário.");
        Long id = usuario.id();
        // A sessão já foi validada pelo filtro. O bloqueio serializa as marcações do funcionário.
        if (repository.bloquearFuncionario(id).isEmpty())
            throw new AuthException(401, "Sua sessão expirou. Entre novamente.");
        var agora = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"));
        if (!agora.toLocalDate().equals(dto.data()))
            throw new AuthException(400, "A data mudou. Atualize o relógio antes de marcar o ponto.");
        var existentes = repository.bloquearDia(id, dto.data());
        long versao = existentes.isEmpty() ? 0 : existentes.getFirst().getValue();
        if (dto.versao() != versao)
            throw new AuthException(409, "O ponto já foi atualizado. Confira os horários antes de marcar novamente.");
        String antes = existentes.isEmpty() ? ",,,,," : existentes.getFirst().getKey();
        var horarios = new ArrayList<>(Arrays.asList(antes.split(",", -1)));
        int indice = horarios.indexOf("");
        if (indice < 0) throw new AuthException(400, "Os seis horários deste dia já foram registrados.");
        String hora = agora.toLocalTime().truncatedTo(java.time.temporal.ChronoUnit.MINUTES).toString();
        if (indice > 0 && hora.compareTo(horarios.get(indice - 1)) <= 0)
            throw new AuthException(409, "Aguarde o próximo minuto para uma nova marcação.");
        horarios.set(indice, hora);
        String novos = String.join(",", horarios);
        repository.inserirDia(id, dto.data(), novos);
        repository.inserirHistorico(id, dto.data(), antes, novos, "Marcação pelo funcionário", id, usuario.nome());
        return avaliar(new DiaPontoDTO(dto.data(), List.copyOf(horarios), versao + 1, true), escalas.listar(id));
    }

    public FuncionarioPontoDTO funcionario(Long id) {
        return repository.consultarFuncionario(id).stream()
                .findFirst()
                .orElseThrow(() -> new AuthException(404, "Funcionário não encontrado."));
    }

    public List<DiaPontoDTO> listar(Long id, String mes) {
        funcionario(id);
        YearMonth periodo;
        try {
            periodo = YearMonth.parse(mes);
        } catch (Exception e) {
            throw new AuthException(400, "Informe um mês válido.");
        }
        if (periodo.getYear() < 1900 || periodo.getYear() > 2100)
            throw new AuthException(400, "Informe um mês entre 1900 e 2100.");
        var escala = escalas.listar(id);
        return repository.consultarDia(id, periodo.atDay(1), periodo.atEndOfMonth()).stream().map(d -> avaliar(d, escala)).toList();
    }

    public List<HistoricoPontoDTO> historico(Long id, LocalDate data) {
        funcionario(id);
        return repository.consultarHistorico(id, data);
    }

    @Transactional
    public DiaPontoDTO salvar(Long id, LocalDate data, EdicaoPontoDTO dto, UsuarioSessao autor) {
        if (autor == null || !autor.perfis().contains("#") || !Permissoes.permite(autor, "RH_PONTO.ALTERAR"))
            throw new AuthException(403, "Você não possui permissão para alterar marcações.");
        if (data == null || data.isAfter(LocalDate.now()) || data.getYear() < 1900)
            throw new AuthException(400, "Escolha uma data válida até hoje.");
        if (dto.motivo() == null
                || dto.motivo().strip().length() < 5
                || dto.motivo().length() > 500)
            throw new AuthException(400, "Informe o motivo da alteração (5 a 500 caracteres).");
        if (dto.horarios() == null || dto.horarios().size() != 6)
            throw new AuthException(400, "Informe os três pares de entrada e saída.");
        int anterior = -1;
        boolean vazio = false;
        for (int i = 0; i < 6; i++) {
            String hora = dto.horarios().get(i);
            if (hora == null || !hora.isEmpty() && !hora.matches("([01][0-9]|2[0-3]):[0-5][0-9]"))
                throw new AuthException(400, "Informe horários válidos no formato HH:mm.");
            if (hora.isEmpty()) {
                vazio = true;
                continue;
            }
            if (vazio)
                throw new AuthException(
                        400,
                        "Preencha os horários em sequência, sem campos intermediários vazios.");
            int minutos = LocalTime.parse(hora).toSecondOfDay() / 60;
            if (minutos <= anterior)
                throw new AuthException(
                        400, "Os horários devem estar em ordem crescente no mesmo dia.");
            anterior = minutos;
        }
        // O bloqueio do funcionário também serializa a primeira marcação de um dia.
        if (repository.bloquearFuncionario(id).isEmpty())
            throw new AuthException(404, "Funcionário não encontrado.");
        var existentes = repository.bloquearDia(id, data);
        long versao = existentes.isEmpty() ? 0 : existentes.getFirst().getValue();
        if (dto.versao() != versao)
            throw new AuthException(
                    409,
                    "Este dia foi alterado por outra pessoa. Recarregue os dados antes de editar.");
        String antes = existentes.isEmpty() ? ",,,,," : existentes.getFirst().getKey(),
                novos = String.join(",", dto.horarios());
        if (antes.equals(novos))
            throw new AuthException(400, "Altere pelo menos um horário antes de salvar.");
        repository.inserirDia(id, data, novos);
        repository.inserirHistorico(
                id, data, antes, novos, dto.motivo().strip(), autor.id(), autor.nome());
        return avaliar(new DiaPontoDTO(data, List.copyOf(dto.horarios()), versao + 1, true), escalas.listar(id));
    }
}
