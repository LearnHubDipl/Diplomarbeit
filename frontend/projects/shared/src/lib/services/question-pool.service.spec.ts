import { TestBed } from '@angular/core/testing';

import { QuestionPoolService } from './question-pool.service';

describe('QuestionPoolService', () => {
  let service: QuestionPoolService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(QuestionPoolService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
