import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QuestionApprovalDetailComponent } from './question-approval-detail.component';

describe('QuestionApprovalDetailComponent', () => {
  let component: QuestionApprovalDetailComponent;
  let fixture: ComponentFixture<QuestionApprovalDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QuestionApprovalDetailComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(QuestionApprovalDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
