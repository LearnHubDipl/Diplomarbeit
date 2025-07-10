import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QuestionRunnerComponent } from './question-runner.component';

describe('QuestionRunnerComponent', () => {
  let component: QuestionRunnerComponent;
  let fixture: ComponentFixture<QuestionRunnerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QuestionRunnerComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(QuestionRunnerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
