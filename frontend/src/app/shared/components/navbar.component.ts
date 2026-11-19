import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  template: `
    <header class="sticky top-0 z-40 w-full border-b border-slate-800/80 bg-slate-950/80 backdrop-blur-md">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <div class="flex items-center gap-8">
          <a routerLink="/dashboard" class="flex items-center gap-3 group">
            <div class="w-10 h-10 rounded-xl bg-gradient-to-tr from-indigo-600 via-indigo-500 to-sky-400 p-0.5 shadow-lg shadow-indigo-500/25 group-hover:shadow-indigo-500/40 transition-all duration-300">
              <div class="w-full h-full bg-slate-950 rounded-[10px] flex items-center justify-center">
                <svg class="w-5 h-5 text-indigo-400 group-hover:scale-110 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"/>
                </svg>
              </div>
            </div>
            <span class="text-xl font-bold bg-gradient-to-r from-white via-slate-100 to-slate-400 bg-clip-text text-transparent tracking-tight">CloudShare</span>
          </a>

          @if (authService.isLoggedIn()) {
            <nav class="hidden md:flex items-center gap-1">
              <a
                routerLink="/dashboard"
                routerLinkActive="bg-slate-800/80 text-white font-medium"
                [routerLinkActiveOptions]="{ exact: true }"
                class="px-3.5 py-1.5 rounded-lg text-sm text-slate-300 hover:text-white hover:bg-slate-900 transition-colors"
              >
                Dashboard
              </a>
              <a
                routerLink="/analytics"
                routerLinkActive="bg-slate-800/80 text-white font-medium"
                class="px-3.5 py-1.5 rounded-lg text-sm text-slate-300 hover:text-white hover:bg-slate-900 transition-colors"
              >
                Analytics
              </a>
            </nav>
          }
        </div>

        <div class="flex items-center gap-3">
          @if (authService.isLoggedIn()) {
            <div class="hidden sm:flex flex-col text-right">
              <span class="text-sm font-medium text-slate-200">{{ authService.currentUser()?.displayName }}</span>
              <span class="text-xs text-slate-400">{{ authService.currentUser()?.email }}</span>
            </div>
            <button
              (click)="logout()"
              class="px-3.5 py-1.5 rounded-lg text-sm text-slate-300 hover:text-white hover:bg-slate-800/80 border border-slate-800 transition-colors flex items-center gap-2"
              title="Sign out"
            >
              <svg class="w-4 h-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/>
              </svg>
              <span class="hidden sm:inline">Sign out</span>
            </button>
          } @else {
            <a
              routerLink="/login"
              class="px-4 py-2 rounded-lg text-sm font-medium text-slate-300 hover:text-white hover:bg-slate-800/80 transition-colors"
            >
              Sign In
            </a>
            <a
              routerLink="/register"
              class="px-4 py-2 rounded-lg text-sm font-medium bg-indigo-600 hover:bg-indigo-500 text-white shadow-lg shadow-indigo-600/30 transition-all hover:scale-[1.02]"
            >
              Get Started
            </a>
          }
        </div>
      </div>
    </header>
  `
})
export class NavbarComponent {
  authService = inject(AuthService);
  private router = inject(Router);

  logout() {
    this.authService.logout();
  }
}
