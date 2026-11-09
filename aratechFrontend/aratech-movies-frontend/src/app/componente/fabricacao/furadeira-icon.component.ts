import { Component } from '@angular/core';

@Component({
  selector: 'app-furadeira-icon',
  standalone: true,
  host: { 'aria-hidden': 'true' },
  template: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32" width="1em" height="1em" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" focusable="false">
    <path d="M4 5h14v11H4a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2Z"/>
    <path d="m18 7 5 1v5l-5 1M23 9h3v3h-3M26 10.5h5M28 9l1 3M6 16l-2 10h9l2-10M4 26h10v4H2v-4h2M13 17h5v4h-4M6 8v5M9 8v5"/>
  </svg>`,
})
export class FuradeiraIconComponent {}
