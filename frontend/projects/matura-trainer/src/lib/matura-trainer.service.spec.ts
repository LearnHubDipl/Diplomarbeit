import { TestBed } from '@angular/core/testing';

import { MaturaTrainerService } from './matura-trainer.service';

describe('MaturaTrainerService', () => {
  let service: MaturaTrainerService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MaturaTrainerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
