package com.aratechmoveis.login.auth.controller;

import com.aratechmoveis.login.auth.request.LoginRequest;
import com.aratechmoveis.login.auth.request.PrimeiroAcessoRequest;
import com.aratechmoveis.login.auth.service.AuthService;
import com.aratechmoveis.login.response.LoginResponse;
import com.aratechmoveis.login.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import tools.jackson.databind.ObjectMapper;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    private LoginRequest umLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setEmailCorporativo("funcionario@aratech.com");
        request.setSenha("senha123");
        return request;
    }

    private PrimeiroAcessoRequest umPrimeiroAcessoRequest() {
        PrimeiroAcessoRequest request = new PrimeiroAcessoRequest();
        request.setIdFuncionario(1L);
        request.setEmailCorporativo("funcionario@aratech.com");
        request.setCodigo("ABC12");
        request.setNovaSenha("novaSenha123");
        return request;
    }

    @Nested
    @DisplayName("POST /login/auth/entrar")
    class Entrar {

        @Test
        @DisplayName("deve retornar 200 com token e perfis quando o login é realizado com sucesso")
        void deveRetornar200QuandoLoginComSucesso() throws Exception {
            LoginRequest request = umLoginRequest();
            LoginResponse loginResponse = LoginResponse.builder().token("jwt-token").perfis(List.of("ADMIN")).build();
            Response<LoginResponse> response = Response.<LoginResponse>builder()
                    .status(200)
                    .mensagem("Login realizado com sucesso")
                    .dados(loginResponse)
                    .build();

            given(authService.login(eq(request))).willReturn(response);

            mockMvc.perform(post("/login/auth/entrar")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.mensagem").value("Login realizado com sucesso"))
                    .andExpect(jsonPath("$.dados.token").value("jwt-token"))
                    .andExpect(jsonPath("$.dados.perfis[0]").value("ADMIN"));
        }
    }

    @Nested
    @DisplayName("POST /login/auth/primeiro-acesso/solicitar")
    class SolicitarPrimeiroAcesso {

        @Test
        @DisplayName("deve retornar 200 quando a solicitação é processada com sucesso")
        void deveRetornar200QuandoSolicitacaoComSucesso() throws Exception {
            Response response = Response.builder()
                    .status(200)
                    .mensagem("Codigo para primeiro acesso enviado com sucesso")
                    .build();

            given(authService.solicitarPrimeiroAcesso("funcionario@gmail.com")).willReturn(response);

            mockMvc.perform(post("/login/auth/primeiro-acesso/solicitar")
                            .param("email", "funcionario@gmail.com"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.mensagem").value("Codigo para primeiro acesso enviado com sucesso"));
        }
    }

    @Nested
    @DisplayName("POST /login/auth/primeiro-acesso/confirmar")
    class ConfirmarPrimeiroAcesso {

        @Test
        @DisplayName("deve retornar 200 quando o primeiro acesso é confirmado com sucesso")
        void deveRetornar200QuandoConfirmacaoComSucesso() throws Exception {
            PrimeiroAcessoRequest request = umPrimeiroAcessoRequest();
            Response response = Response.builder().status(200).mensagem("Sucesso").build();

            given(authService.confirmarPrimeiroAcesso(eq(request))).willReturn(response);

            mockMvc.perform(post("/login/auth/primeiro-acesso/confirmar")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.mensagem").value("Sucesso"));
        }
    }
}
