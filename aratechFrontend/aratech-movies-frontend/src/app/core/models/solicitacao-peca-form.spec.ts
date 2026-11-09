import { validarSolicitacaoPeca } from './solicitacao-peca-form';
import { SolicitacaoPecaForm } from './solicitacao-peca.model';
const form: SolicitacaoPecaForm = { nomePeca: 'Rolamento', codigo: '', quantidade: 1, unidade: 'un', equipamento: 'Prensa', finalidade: 'MELHORIA', prioridade: 'ALTA', solicitante: 'Ana Silva', setor: 'Producao', telefone: '', observacoes: 'Trocar rolamento' };
describe('Validacao de solicitacao de peca', () => {
  for (const quantidade of [0.001, 1, 1.5, 999999999999.999]) it('aceita quantidade ' + quantidade, () => expect(validarSolicitacaoPeca({ ...form, quantidade })).toBe(''));
  for (const quantidade of [0, -1, 0.0001, NaN, Infinity, 1000000000000]) it('rejeita quantidade ' + quantidade, () => expect(validarSolicitacaoPeca({ ...form, quantidade })).toContain('quantidade'));
  it('exige unidade, finalidade e descricao e aceita codigo e telefone opcionais', () => {
    expect(validarSolicitacaoPeca(form)).toBe('');
    expect(validarSolicitacaoPeca({ ...form, unidade: '' })).toContain('unidade');
    expect(validarSolicitacaoPeca({ ...form, finalidade: '' })).toContain('finalidade');
    expect(validarSolicitacaoPeca({ ...form, observacoes: '' })).toContain('Descreva');
  });
});
