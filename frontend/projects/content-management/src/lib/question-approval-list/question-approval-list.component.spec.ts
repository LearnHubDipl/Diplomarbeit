import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QuestionApprovalListComponent } from './question-approval-list.component';

describe('QuestionApprovalListComponent', () => {
  let component: QuestionApprovalListComponent;
  let fixture: ComponentFixture<QuestionApprovalListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QuestionApprovalListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(QuestionApprovalListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
