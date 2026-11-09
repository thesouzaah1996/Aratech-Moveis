import { Injectable } from '@angular/core';
import { HttpBackend, HttpClient } from '@angular/common/http';
import { Observable, finalize, map, of, shareReplay, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class CsrfService {
  private http: HttpClient;
  private token = '';
  private request?: Observable<string>;
  constructor(backend: HttpBackend) { this.http = new HttpClient(backend); }
  obter(): Observable<string> {
    if (this.token) return of(this.token);
    return this.request ??= this.http.get<{ token: string }>('/aratech/auth/csrf').pipe(
      map(r => r.token), tap(token => this.token = token), finalize(() => this.request = undefined),
      shareReplay({ bufferSize: 1, refCount: false })
    );
  }
  limpar(): void { this.token = ''; this.request = undefined; }
}
