import { validarChegada } from './chegada-form';
import { RegistroChegadaForm } from './registro-chegada.model';
const form: RegistroChegadaForm = { notaFiscal: '123', empresa: 'Empresa', nomeMotorista: 'Motorista', placa: 'ABC1D23', descricaoCarga: 'Madeira', setorResponsavel: 'ALMOXARIFADO' };
describe('Validacao de chegada', () => {
  it('exige operacao para carregamento', () => expect(validarChegada({ ...form, setorResponsavel: 'CARREGAMENTO' })).toContain('carregar'));
  for (const tipoOperacao of ['CARREGAR', 'DESCARREGAR'] as const) {
    it('aceita ' + tipoOperacao, () => expect(validarChegada({ ...form, setorResponsavel: 'CARREGAMENTO', tipoOperacao })).toBe(''));
  }

  for (const notaFiscal of ['1', '000000001', '999999999', '35260806298134000117550050000624241178664682']) {
    it('aceita nota ' + notaFiscal, () => expect(validarChegada({ ...form, notaFiscal })).toBe(''));
  }
  for (const notaFiscal of ['0', '000000000', '1234567890', 'NF123', '35260806298134000117550050000624241178664683', '0'.repeat(44), '1'.repeat(44)]) {
    it('rejeita nota ' + notaFiscal, () => expect(validarChegada({ ...form, notaFiscal })).toContain('nota fiscal'));
  }
  for (const placa of ['ABC1D23', 'abc1d23', 'ABC1234', 'abc-1234']) {
    it('aceita placa ' + placa, () => expect(validarChegada({ ...form, placa })).toBe(''));
  }
  for (const placa of ['ABC12D3', 'AB12345', 'ABC1DD3', 'ABC_1234']) {
    it('rejeita placa ' + placa, () => expect(validarChegada({ ...form, placa })).toContain('placa'));
  }
});
