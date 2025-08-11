import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QuestionBrowsingViewComponent } from './question-browsing-view.component';

describe('QuestionBrowsingViewComponent', () => {
  let component: QuestionBrowsingViewComponent;
  let fixture: ComponentFixture<QuestionBrowsingViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QuestionBrowsingViewComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(QuestionBrowsingViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
