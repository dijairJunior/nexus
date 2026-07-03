import { Component, inject } from '@angular/core';
import { Auth } from '../../core/services/auth';

@Component({
  selector: 'app-header',
  templateUrl: './header.html',
  styleUrl: './header.scss',
})
export class Header {
  private auth = inject(Auth);

  userName = () => this.auth.getUserName() ?? '';

  onLogout(): void {
    this.auth.logout();
  }
}
