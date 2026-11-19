import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(() => {
    localStorage.clear();
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AuthService,
        { provide: Router, useValue: routerSpy }
      ]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('login saves token and sets currentUser', () => {
    const mockResponse = {
      token: 'test-jwt-token',
      user: {
        id: '123',
        email: 'test@example.com',
        displayName: 'Test User',
        createdAt: '2026-01-01T00:00:00Z'
      }
    };

    service.login({ email: 'test@example.com', password: 'password123' }).subscribe(res => {
      expect(res.token).toBe('test-jwt-token');
      expect(service.currentUser()?.email).toBe('test@example.com');
      expect(service.getToken()).toBe('test-jwt-token');
      expect(service.isLoggedIn()).toBeTrue();
    });

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('logout clears token and navigates to /login', () => {
    localStorage.setItem('cloudshare_token', 'sample-token');
    service.currentUser.set({ id: '123', email: 'test@example.com', displayName: 'Test', createdAt: '' });

    service.logout(false);

    expect(service.getToken()).toBeNull();
    expect(service.currentUser()).toBeNull();
    expect(service.isLoggedIn()).toBeFalse();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
  });
});
