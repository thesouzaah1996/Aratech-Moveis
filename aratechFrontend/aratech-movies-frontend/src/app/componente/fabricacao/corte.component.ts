import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { BreadcrumbComponent } from '../breadcrumb/breadcrumb.component';
import { PermissaoDirective } from '../../core/permissao.directive';
import { SerraIconComponent } from './serra-icon.component';
@Component({selector:'app-corte',standalone:true,imports:[RouterLink,NavbarComponent,BreadcrumbComponent,PermissaoDirective,SerraIconComponent],template:`<app-navbar /><main class="app-main"><app-breadcrumb [items]="[{label:'Início',route:'/dashboard'},{label:'Corte'}]" /><div class="container py-5"><div class="row g-4 justify-content-center"><div class="col-md-6 col-lg-5" *appPermissao="'CORTE_LOTES.CONSULTAR'"><a routerLink="/corte/ordens" class="card card-menu h-100"><app-serra-icon class="icon-size" /><h4 class="fw-bold mb-2">Lotes</h4><p class="text-muted text-center mb-0">Receber os lotes do PCP e cortar as chapas conforme a ficha técnica</p></a></div></div></div></main>`})
export class CorteComponent {}
