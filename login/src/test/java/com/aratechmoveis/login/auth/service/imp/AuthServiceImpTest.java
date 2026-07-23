package com.aratechmoveis.login.auth.service.imp;

import com.aratechmoveis.login.auth.entity.CodigoResetSenha;
import com.aratechmoveis.login.auth.repository.CodigoResetSenhaRepo;
import com.aratechmoveis.login.auth.request.LoginRequest;
import com.aratechmoveis.login.auth.request.PrimeiroAcessoRequest;
import com.aratechmoveis.login.auth.service.GeradorCodigo;
import com.aratechmoveis.login.exceptions.BadRequestException;
import com.aratechmoveis.login.exceptions.NotFoundException;
import com.aratechmoveis.login.funcionario.entity.Funcionario;
import com.aratechmoveis.login.funcionario.entity.Perfil;
import com.aratechmoveis.login.funcionario.repository.FuncionarioRepository;
import com.aratechmoveis.login.funcionario.repository.PerfilRepository;
import com.aratechmoveis.login.notificacao.dto.NotificacaoDTO;
import com.aratechmoveis.login.notificacao.service.NotificacaoService;
import com.aratechmoveis.login.response.LoginResponse;
import com.aratechmoveis.login.response.Response;
import com.aratechmoveis.login.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImp")
class AuthServiceImpTest {

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private PerfilRepository perfilRepository;

    @Mock
    private NotificacaoService notificacaoService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private GeradorCodigo geradorCodigo;

    @Mock
    private CodigoResetSenhaRepo codigoResetSenhaRepo;

    @InjectMocks
    private AuthServiceImp authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "resetLink", "https://aratech.com/reset?codigo=");
        ReflectionTestUtils.setField(authService, "primeiroAcessoLink", "https://aratech.com/primeiro-acesso?codigo=");
    }

    private Funcionario umFuncionario() {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(1L);
        funcionario.setIdFuncionario(10L);
        funcionario.setNomeFuncionario("Maria Silva");
        funcionario.setEmailPessoal("maria@gmail.com");
        funcionario.setEmailCorporativo("maria@aratech.com");
        funcionario.setSenha("senhaCodificada");
        funcionario.setPerfis(List.of(Perfil.builder().id(1L).nome("ADMIN").ativo(true).build()));
        funcionario.setPrimeiroLogin(false);
        funcionario.setAtivo(true);
        return funcionario;
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("deve retornar token e perfis quando as credenciais são válidas")
        void deveRetornarTokenQuandoCredenciaisValidas() {
            LoginRequest request = new LoginRequest();
            request.setEmailCorporativo("maria@aratech.com");
            request.setSenha("senha123");

            Funcionario funcionario = umFuncionario();

            given(funcionarioRepository.findByEmailCorporativo("maria@aratech.com")).willReturn(Optional.of(funcionario));
            given(passwordEncoder.matches("senha123", funcionario.getSenha())).willReturn(true);
            given(jwtService.gerarToken(funcionario.getEmailCorporativo(), List.of("ADMIN"))).willReturn("jwt-token");

            Response<LoginResponse> response = authService.login(request);

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getDados().getToken()).isEqualTo("jwt-token");
            assertThat(response.getDados().getPerfis()).containsExactly("ADMIN");
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando o funcionário não é encontrado")
        void deveLancarExcecaoQuandoFuncionarioNaoEncontrado() {
            LoginRequest request = new LoginRequest();
            request.setEmailCorporativo("inexistente@aratech.com");
            request.setSenha("senha123");

            given(funcionarioRepository.findByEmailCorporativo("inexistente@aratech.com")).willReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request)).isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("deve lançar BadRequestException quando a senha não confere")
        void deveLancarExcecaoQuandoSenhaInvalida() {
            LoginRequest request = new LoginRequest();
            request.setEmailCorporativo("maria@aratech.com");
            request.setSenha("senhaErrada");

            Funcionario funcionario = umFuncionario();

            given(funcionarioRepository.findByEmailCorporativo("maria@aratech.com")).willReturn(Optional.of(funcionario));
            given(passwordEncoder.matches("senhaErrada", funcionario.getSenha())).willReturn(false);

            assertThatThrownBy(() -> authService.login(request)).isInstanceOf(BadRequestException.class);
        }
    }

    @Nested
    @DisplayName("solicitarRedefinicaoSenha")
    class SolicitarRedefinicaoSenha {

        @Test
        @DisplayName("deve gerar código, salvar e enviar email com sucesso")
        void deveGerarCodigoEEnviarEmailComSucesso() {
            Funcionario funcionario = umFuncionario();

            given(funcionarioRepository.findByEmailCorporativo(funcionario.getEmailCorporativo())).willReturn(Optional.of(funcionario));
            given(geradorCodigo.gerarCodigoUnico()).willReturn("CODIG");

            Response<?> response = authService.redefinirSenha(funcionario.getEmailCorporativo());

            assertThat(response.getStatus()).isEqualTo(200);
            then(codigoResetSenhaRepo).should().deleteByFuncionarioIdFuncionario(funcionario.getIdFuncionario());
            then(codigoResetSenhaRepo).should().save(any(CodigoResetSenha.class));
            then(notificacaoService).should().enviarEmail(any(NotificacaoDTO.class), eq(funcionario));
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando o funcionário não é encontrado")
        void deveLancarExcecaoQuandoFuncionarioNaoEncontrado() {
            given(funcionarioRepository.findByEmailCorporativo("inexistente@aratech.com")).willReturn(Optional.empty());

            assertThatThrownBy(() -> authService.redefinirSenha("inexistente@aratech.com"))
                    .isInstanceOf(NotFoundException.class);

            then(codigoResetSenhaRepo).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("solicitarPrimeiroAcesso")
    class SolicitarPrimeiroAcesso {

        @Test
        @DisplayName("deve gerar código, salvar e enviar email com sucesso")
        void deveGerarCodigoEEnviarEmailComSucesso() {
            Funcionario funcionario = umFuncionario();

            given(funcionarioRepository.findByEmailPessoal(funcionario.getEmailPessoal())).willReturn(Optional.of(funcionario));
            given(geradorCodigo.gerarCodigoUnico()).willReturn("CODIG");

            Response<?> response = authService.solicitarPrimeiroAcesso(funcionario.getEmailPessoal());

            assertThat(response.getStatus()).isEqualTo(200);
            then(codigoResetSenhaRepo).should().deleteByFuncionarioIdFuncionario(funcionario.getIdFuncionario());
            then(codigoResetSenhaRepo).should().save(any(CodigoResetSenha.class));
            then(notificacaoService).should().enviarEmail(any(NotificacaoDTO.class), eq(funcionario));
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando o funcionário não é encontrado")
        void deveLancarExcecaoQuandoFuncionarioNaoEncontrado() {
            given(funcionarioRepository.findByEmailPessoal("inexistente@gmail.com")).willReturn(Optional.empty());

            assertThatThrownBy(() -> authService.solicitarPrimeiroAcesso("inexistente@gmail.com"))
                    .isInstanceOf(NotFoundException.class);

            then(codigoResetSenhaRepo).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("confirmarPrimeiroAcesso")
    class ConfirmarPrimeiroAcesso {

        private PrimeiroAcessoRequest umRequest(String codigo) {
            PrimeiroAcessoRequest request = new PrimeiroAcessoRequest();
            request.setIdFuncionario(10L);
            request.setEmailCorporativo("maria@aratech.com");
            request.setCodigo(codigo);
            request.setNovaSenha("novaSenha123");
            return request;
        }

        @Test
        @DisplayName("deve concluir o primeiro acesso com sucesso")
        void deveConcluirComSucesso() {
            Funcionario funcionario = umFuncionario();
            PrimeiroAcessoRequest request = umRequest("CODIG");
            CodigoResetSenha codigoResetSenha = CodigoResetSenha.builder()
                    .id(1L)
                    .codigo("CODIG")
                    .usado(false)
                    .dataExpiracao(LocalDateTime.now().plusHours(1))
                    .funcionario(funcionario)
                    .build();

            given(funcionarioRepository.findByIdFuncionario(10L)).willReturn(Optional.of(funcionario));
            given(codigoResetSenhaRepo.findByCodigo("CODIG")).willReturn(Optional.of(codigoResetSenha));
            given(passwordEncoder.encode("novaSenha123")).willReturn("novaSenhaCodificada");

            Response<?> response = authService.confirmarPrimeiroAcesso(request);

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(funcionario.getSenha()).isEqualTo("novaSenhaCodificada");
            assertThat(funcionario.isPrimeiroLogin()).isFalse();
            assertThat(codigoResetSenha.isUsado()).isTrue();
            then(funcionarioRepository).should().save(funcionario);
            then(codigoResetSenhaRepo).should().save(codigoResetSenha);
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando o funcionário não é encontrado")
        void deveLancarExcecaoQuandoFuncionarioNaoEncontrado() {
            PrimeiroAcessoRequest request = umRequest("CODIG");
            given(funcionarioRepository.findByIdFuncionario(10L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> authService.confirmarPrimeiroAcesso(request)).isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando o código não pertence ao funcionário")
        void deveLancarExcecaoQuandoCodigoNaoPertenceAoFuncionario() {
            Funcionario funcionario = umFuncionario();
            Funcionario outroFuncionario = umFuncionario();
            outroFuncionario.setIdFuncionario(99L);
            PrimeiroAcessoRequest request = umRequest("CODIG");
            CodigoResetSenha codigoResetSenha = CodigoResetSenha.builder()
                    .codigo("CODIG")
                    .usado(false)
                    .dataExpiracao(LocalDateTime.now().plusHours(1))
                    .funcionario(outroFuncionario)
                    .build();

            given(funcionarioRepository.findByIdFuncionario(10L)).willReturn(Optional.of(funcionario));
            given(codigoResetSenhaRepo.findByCodigo("CODIG")).willReturn(Optional.of(codigoResetSenha));

            assertThatThrownBy(() -> authService.confirmarPrimeiroAcesso(request)).isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando o código não é encontrado")
        void deveLancarExcecaoQuandoCodigoNaoEncontrado() {
            Funcionario funcionario = umFuncionario();
            PrimeiroAcessoRequest request = umRequest("INVALIDO");

            given(funcionarioRepository.findByIdFuncionario(10L)).willReturn(Optional.of(funcionario));
            given(codigoResetSenhaRepo.findByCodigo("INVALIDO")).willReturn(Optional.empty());

            assertThatThrownBy(() -> authService.confirmarPrimeiroAcesso(request)).isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando o código já foi utilizado")
        void deveLancarExcecaoQuandoCodigoJaUtilizado() {
            Funcionario funcionario = umFuncionario();
            PrimeiroAcessoRequest request = umRequest("CODIG");
            CodigoResetSenha codigoResetSenha = CodigoResetSenha.builder()
                    .codigo("CODIG")
                    .usado(true)
                    .dataExpiracao(LocalDateTime.now().plusHours(1))
                    .funcionario(funcionario)
                    .build();

            given(funcionarioRepository.findByIdFuncionario(10L)).willReturn(Optional.of(funcionario));
            given(codigoResetSenhaRepo.findByCodigo("CODIG")).willReturn(Optional.of(codigoResetSenha));

            assertThatThrownBy(() -> authService.confirmarPrimeiroAcesso(request)).isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando o código está expirado")
        void deveLancarExcecaoQuandoCodigoExpirado() {
            Funcionario funcionario = umFuncionario();
            PrimeiroAcessoRequest request = umRequest("CODIG");
            CodigoResetSenha codigoResetSenha = CodigoResetSenha.builder()
                    .codigo("CODIG")
                    .usado(false)
                    .dataExpiracao(LocalDateTime.now().minusHours(1))
                    .funcionario(funcionario)
                    .build();

            given(funcionarioRepository.findByIdFuncionario(10L)).willReturn(Optional.of(funcionario));
            given(codigoResetSenhaRepo.findByCodigo("CODIG")).willReturn(Optional.of(codigoResetSenha));

            assertThatThrownBy(() -> authService.confirmarPrimeiroAcesso(request)).isInstanceOf(NotFoundException.class);
        }
    }
}
