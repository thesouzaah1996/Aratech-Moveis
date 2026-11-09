import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { EMPTY, Observable, expand, map, reduce } from 'rxjs';
import { Funcionario, FuncionarioForm, ArquivosFuncionario, CadastroFuncionarioResponse, FuncionariosPagina } from '../models/funcionario.model';

@Injectable({ providedIn: 'root' })
export class FuncionarioService {
  constructor(private http: HttpClient) {}

  getAll(): Observable<Funcionario[]> {
    const pagina = (numero: number) => this.http.get<FuncionariosPagina>('/aratech/rh/funcionarios', {
      params: new HttpParams().set('pagina',numero).set('tamanho',100)
    });
    return pagina(0).pipe(
      expand(p => p.funcionarios.length > 0 && (p.pagina+1)*p.tamanho < p.total ? pagina(p.pagina+1) : EMPTY),
      reduce((todos,p) => [...todos,...p.funcionarios.map(f => ({
        id:f.id,nome:f.nomeCompleto,email:f.email,cargo:f.cargo,setor:f.setor,telefone:f.telefoneFixo,ativo:f.ativo,perfis:[]
      }))], [] as Funcionario[])
    );
  }

  search(nome: string): Observable<Funcionario[]> {
    const termo=nome.trim().toLocaleLowerCase('pt-BR');
    return this.getAll().pipe(map(lista=>lista.filter(f=>f.nome.toLocaleLowerCase('pt-BR').includes(termo))));
  }

  add(form: FuncionarioForm, arquivos: ArquivosFuncionario): Observable<Funcionario> {
    const payload = new FormData();
    const nomes: Record<string, string> = {
      nome: 'nomeCompleto', pis: 'pisNitNis', telefone: 'telefoneFixo',
      celular: 'celularWhatsapp', jornadaHoras: 'jornadaSemanal', pix: 'chavePix'
    };
    const numericos = new Set(['cpf', 'pis', 'cep', 'telefone', 'celular']);
    Object.entries(form).forEach(([campo, valor]) => {
      if (valor === null || valor === undefined) return;
      let texto = String(valor).trim();
      if (numericos.has(campo)) texto = texto.replace(/\D/g, '');
      if (campo === 'email') texto = texto.toLowerCase();
      if (texto) payload.append(nomes[campo] ?? campo, texto);
    });
    const documentos: Record<keyof ArquivosFuncionario, string> = {
      rg: 'rgOuCnh', ctps: 'ctps', aso: 'aso', compResidencia: 'comprovanteResidencia',
      tituloEleitor: 'tituloEleitor', reservista: 'reservista', diplomaEscolar: 'diplomaCertificado',
      certidaoCasamento: 'certidaoCasamento', certidaoNascimento: 'certidaoNascimento'
    };
    (Object.keys(documentos) as (keyof ArquivosFuncionario)[]).forEach(campo => {
      const arquivo = arquivos[campo];
      if (arquivo) payload.append(documentos[campo], arquivo, arquivo.name);
    });
    // O navegador define Content-Type e boundary do multipart.
    return this.http.post<CadastroFuncionarioResponse>('/aratech/rh/funcionarios', payload).pipe(
      map(resposta => ({
        id: resposta.funcionarioId, nome: resposta.nomeCompleto, ativo: resposta.ativo,
        email: form.email.trim().toLowerCase(), cargo: form.cargo.trim(), setor: form.setor.trim(),
        telefone: form.telefone, perfis: []
      }))
    );
  }

  enable(id: number): Observable<Pick<Funcionario, 'id' | 'nome' | 'ativo'>> {
    return this.alterarStatus(id, 'ativar');
  }

  disable(id: number): Observable<Pick<Funcionario, 'id' | 'nome' | 'ativo'>> {
    return this.alterarStatus(id, 'desativar');
  }

  private alterarStatus(id: number, acao: 'ativar' | 'desativar'): Observable<Pick<Funcionario, 'id' | 'nome' | 'ativo'>> {
    return this.http.patch<CadastroFuncionarioResponse>(`/aratech/rh/funcionarios/${id}/${acao}`, {}).pipe(
      map(resposta => ({ id: resposta.funcionarioId, nome: resposta.nomeCompleto, ativo: resposta.ativo }))
    );
  }

  atribuirEmailCorporativo(id: number, emailCorporativo: string): Observable<{ emailCorporativo: string; senhaTemporaria: string }> {
    return this.http.post<{ emailCorporativo: string; senhaTemporaria: string }>(`/aratech/auth/usuarios/${id}/email-corporativo`, { emailCorporativo: emailCorporativo.trim().toLowerCase() });
  }
}
