import { Component } from '@angular/core';

@Component({
  selector: 'app-serra-icon',
  standalone: true,
  host: { 'aria-hidden': 'true' },
  template: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32" width="1em" height="1em" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" focusable="false">
    <path d="M16 2 18 6 23 4 22 9 28 9 25 13 30 16 26 18 28 23 23 22 23 28 19 25 16 30 14 26 9 28 10 23 4 23 7 19 2 16 6 14 4 9 9 10 9 4 13 7Z"/>
    <circle cx="16" cy="16" r="3"/>
  </svg>`,
})
export class SerraIconComponent {}
