import { PecaEstoqueForm } from './peca-estoque.model';
export function quantidadePecaValida(q: number | null, permitirZero = false): boolean {
  return q !== null && Number.isFinite(q) && (permitirZero ? q >= 0 : q > 0) && q <= 999999999999.999 && /^[0-9]+(?:\.[0-9]{1,3})?$/.test(String(q));
}
export function validarPecaEstoque(f: PecaEstoqueForm, editando = false): boolean {
  const texto = (s: string, min: number, max: number) => s.trim().length >= min && s.length <= max && !/[\p{Cc}\p{Cf}]/u.test(s);
  return texto(f.nome,2,150) && texto(f.codigo,1,100) && ['un','kg','L','m','cx','par'].includes(f.unidade)
    && texto(f.localizacao,2,100) && f.descricao.length <= 2000 && !/[\p{Cc}\p{Cf}]/u.test(f.descricao.replace(/[\r\n\t]/g,''))
    && (editando || quantidadePecaValida(f.quantidade,true));
}
