import { normalizarChamado, validarChamado } from './chamado-form';
import { ChamadoForm } from './chamado.model';
const form: ChamadoForm = { equipamento: ' Prensa  01 ', tipo: 'CORRETIVA', prioridade: 'ALTA', solicitante: 'Ana Silva', setor: ' Producao ', telefone: '(43) 99999-1234', descricao: ' Falha no motor\nVerificar correia. ' };
describe('Validacao da solicitacao de manutencao', () => {
  it('normaliza os dados e preserva as linhas da descricao', () => {
    expect(validarChamado(form)).toBe('');
    const dados = normalizarChamado(form);
    expect(dados.equipamento).toBe('Prensa 01'); expect(dados.telefone).toBe('43999991234');
    expect(dados.descricao).toBe('Falha no motor\nVerificar correia.');
  });
  for (const telefone of ['', '(43) 3333-1234', '(43) 99999-1234']) {
    it('aceita telefone opcional ou com DDD: ' + telefone, () => expect(validarChamado({ ...form, telefone })).toBe(''));
  }
  for (const telefone of ['123', 'abc43999991234', '(00) 99999-1234']) {
    it('rejeita telefone invalido: ' + telefone, () => expect(validarChamado({ ...form, telefone })).toContain('telefone'));
  }
  it('rejeita campos obrigatorios, caracteres invisiveis e descricao muito longa', () => {
    expect(validarChamado({ ...form, tipo: '' })).toContain('tipo');
    expect(validarChamado({ ...form, equipamento: 'P' })).toContain('equipamento');
    expect(validarChamado({ ...form, solicitante: 'Ana\u200BSilva' })).toContain('solicitante');
    expect(validarChamado({ ...form, descricao: 'x'.repeat(3001) })).toContain('Descreva');
  });
});
