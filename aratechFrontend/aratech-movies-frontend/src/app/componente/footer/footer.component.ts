import { Component } from '@angular/core';
import { environment } from '../../../environments/environment';
import packageJson from '../../../../package.json';

@Component({
  selector: 'app-footer',
  standalone: true,
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.scss'
})
export class FooterComponent {
  readonly anoAtual = new Date().getFullYear();
  readonly version = packageJson.version;
  readonly envName = environment.envName;

  get envBadgeClass(): string {
    switch (this.envName) {
      case 'PROD':    return 'env-badge env-badge--prod';
      case 'HOMOLOG': return 'env-badge env-badge--homolog';
      default:        return 'env-badge env-badge--dev';
    }
  }
}
