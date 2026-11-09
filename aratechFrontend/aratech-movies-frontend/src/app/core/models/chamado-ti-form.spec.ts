import { validarChamadoTi, normalizarChamadoTi } from './chamado-ti-form';
import { ChamadoTiForm } from './chamado-ti.model';
describe('Formulario TI',()=>{
 const form=():ChamadoTiForm=>({tipo:'Hardware',prioridade:'Alta',moduloAfetado:'',titulo:'  Monitor  parado ',solicitante:'Ana Silva',setor:'Compras',contato:'',descricao:'Sem imagem\nCabo testado',tentativasSolucao:''});
 it('preserva campos opcionais e aceita ramal ou telefone',()=>{for(const contato of ['','1234','(11) 3333-4444','(11) 98888-7777'])expect(validarChamadoTi({...form(),contato})).toBe('');});
 it('normaliza o envio e preserva a descricao multiline',()=>{const f=normalizarChamadoTi({...form(),contato:'(11) 98888-7777'});expect(f.titulo).toBe('Monitor parado');expect(f.contato).toBe('11988887777');expect(f.descricao).toContain('\n');});
 it('valida tipo, modulo, contato e limites',()=>{for(const f of [{...form(),tipo:'' as const},{...form(),moduloAfetado:'Inventado'},{...form(),contato:'1234567'},{...form(),titulo:'a'.repeat(151)},{...form(),descricao:'ab\u0000'}])expect(validarChamadoTi(f)).not.toBe('');});
});
