import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChooseStudyTopicComponent } from './choose-study-topic.component';

describe('ChooseStudyTopicComponent', () => {
  let component: ChooseStudyTopicComponent;
  let fixture: ComponentFixture<ChooseStudyTopicComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChooseStudyTopicComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ChooseStudyTopicComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
