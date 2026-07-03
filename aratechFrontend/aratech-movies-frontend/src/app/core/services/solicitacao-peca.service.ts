import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { SolicitacaoPeca, SolicitacaoPecaForm } from '../models/solicitacao-peca.model';
import { environment } from '../../../environments/environment';

interface ApiResponse {
  status: number;
  message: string;
  solicitacaoPeca?: SolicitacaoPeca;
  solicitacoesPeca?: SolicitacaoPeca[];
}

@Injectable({ providedIn: 'root' })
export class SolicitacaoPecaService {
  private readonly api = `${environment.apiUrl}/manutencao/solicitacao-peca`;

  constructor(private http: HttpClient) {}

  add(form: SolicitacaoPecaForm): Observable<SolicitacaoPeca> {
    return this.http
      .post<ApiResponse>(`${this.api}/add`, form)
      .pipe(map(res => res.solicitacaoPeca!));
  }
}
