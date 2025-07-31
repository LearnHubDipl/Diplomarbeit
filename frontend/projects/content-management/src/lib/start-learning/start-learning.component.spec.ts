import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StartLearningComponent } from './start-learning.component';

describe('StartLearningComponent', () => {
  let component: StartLearningComponent;
  let fixture: ComponentFixture<StartLearningComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StartLearningComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StartLearningComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
