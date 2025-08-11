import { TestBed } from '@angular/core/testing';

import { TopicPoolService } from './topic-pool.service';

describe('TopicPoolService', () => {
  let service: TopicPoolService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TopicPoolService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
