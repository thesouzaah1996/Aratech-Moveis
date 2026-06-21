import { Component, ElementRef, ViewChild } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

declare const bootstrap: any;

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent {
  @ViewChild('logoutModal') logoutModalEl!: ElementRef;

  constructor(private router: Router) {}

  performLogout(): void {
    const modal = bootstrap.Modal.getInstance(this.logoutModalEl.nativeElement);
    modal?.hide();
    this.router.navigate(['/login']);
  }
}
