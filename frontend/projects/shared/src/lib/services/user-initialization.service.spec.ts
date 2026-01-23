import { TestBed } from '@angular/core/testing';

import { UserInitializationService } from './user-initialization.service';

describe('UserInitializationService', () => {
  let service: UserInitializationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UserInitializationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
