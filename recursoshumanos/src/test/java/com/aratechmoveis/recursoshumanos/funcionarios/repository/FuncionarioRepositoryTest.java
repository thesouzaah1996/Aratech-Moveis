package com.aratechmoveis.recursoshumanos.funcionarios.repository;

import com.aratechmoveis.recursoshumanos.funcionarios.entity.Funcionario;
import com.aratechmoveis.recursoshumanos.funcionarios.entity.TipoFuncionario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("FuncionarioRepository")
class FuncionarioRepositoryTest {

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void registrarFuncaoUnaccent() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(
                    "CREATE ALIAS IF NOT EXISTS UNACCENT FOR \"" + H2UnaccentFunction.class.getName() + ".unaccent\""
            );
        }
    }

    private Funcionario umFuncionario(String nome, String cpf, String email) {
        Funcionario funcionario = new Funcionario();
        funcionario.setNome(nome);
        funcionario.setCpf(cpf);
        funcionario.setDataNascimento(LocalDate.of(1990, 1, 1));
        funcionario.setSexo("M");
        funcionario.setNomeMae("Mãe Teste");
        funcionario.setCep("12345-678");
        funcionario.setLogradouro("Rua Teste");
        funcionario.setNumero("100");
        funcionario.setBairro("Centro");
        funcionario.setCidade("Sao Paulo");
        funcionario.setUf("SP");
        funcionario.setDataAdmissao(LocalDate.now());
        funcionario.setTipoContrato("CLT");
        funcionario.setCargo("Analista");
        funcionario.setTipoFuncionario(TipoFuncionario.ADMINISTRATIVO);
        funcionario.setSetor("TI");
        funcionario.setSalario(new BigDecimal("3000.00"));
        funcionario.setEmailPessoal(email);
        funcionario.setBanco("Banco Teste");
        funcionario.setAgencia("0001");
        funcionario.setConta("123456");
        funcionario.setTipoConta("corrente");
        funcionario.setAtivo(true);
        return funcionario;
    }

    @Test
    @DisplayName("deve encontrar funcionário salvo ao buscar por parte do nome (case-insensitive)")
    void deveEncontrarFuncionarioPorNome() {
        funcionarioRepository.save(umFuncionario("Fulano da Silva", "111.111.111-11", "fulano@teste.com"));

        List<Funcionario> resultado = funcionarioRepository.findByNomeContainingIgnoreCase("fulano");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("Fulano da Silva");
    }

    @Test
    @DisplayName("deve encontrar funcionário buscando por trecho no meio do nome")
    void deveEncontrarFuncionarioPorTrechoDoNome() {
        funcionarioRepository.save(umFuncionario("Fulano da Silva", "111.111.111-11", "fulano@teste.com"));

        List<Funcionario> resultado = funcionarioRepository.findByNomeContainingIgnoreCase("da silva");

        assertThat(resultado).hasSize(1);
    }

    @Test
    @DisplayName("não deve encontrar funcionário quando o nome não corresponde")
    void naoDeveEncontrarFuncionarioComNomeDiferente() {
        funcionarioRepository.save(umFuncionario("Fulano da Silva", "111.111.111-11", "fulano@teste.com"));

        List<Funcionario> resultado = funcionarioRepository.findByNomeContainingIgnoreCase("Beltrano");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("deve encontrar funcionário com nome acentuado buscando sem acento")
    void deveEncontrarFuncionarioComNomeAcentuadoBuscandoSemAcento() {
        funcionarioRepository.save(umFuncionario("José André", "111.111.111-11", "jose@teste.com"));

        List<Funcionario> resultado = funcionarioRepository.findByNomeContainingIgnoreCase("jose andre");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("José André");
    }

    @Test
    @DisplayName("deve encontrar funcionário buscando com acento mesmo se o nome cadastrado não tiver")
    void deveEncontrarFuncionarioBuscandoComAcentoQuandoNomeSemAcento() {
        funcionarioRepository.save(umFuncionario("Jose Andre", "111.111.111-11", "jose@teste.com"));

        List<Funcionario> resultado = funcionarioRepository.findByNomeContainingIgnoreCase("José");

        assertThat(resultado).hasSize(1);
    }

    @Test
    @DisplayName("deve trazer todos os que começam com o mesmo primeiro nome e filtrar conforme mais é digitado")
    void deveFiltrarProgressivamenteConformeNomeCompletoInformado() {
        funcionarioRepository.save(umFuncionario("Joao da Silva", "111.111.111-11", "joao.silva@teste.com"));
        funcionarioRepository.save(umFuncionario("Joao Pereira", "222.222.222-22", "joao.pereira@teste.com"));
        funcionarioRepository.save(umFuncionario("Maria Souza", "333.333.333-33", "maria@teste.com"));

        List<Funcionario> todosOsJoao = funcionarioRepository.findByNomeContainingIgnoreCase("joao");
        List<Funcionario> apenasJoaoSilva = funcionarioRepository.findByNomeContainingIgnoreCase("joao da silva");

        assertThat(todosOsJoao).hasSize(2);
        assertThat(apenasJoaoSilva).hasSize(1);
        assertThat(apenasJoaoSilva.get(0).getNome()).isEqualTo("Joao da Silva");
    }
}
