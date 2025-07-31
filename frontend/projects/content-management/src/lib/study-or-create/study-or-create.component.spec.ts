import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StudyOrCreateComponent } from './study-or-create.component';

describe('StudyOrCreateComponent', () => {
  let component: StudyOrCreateComponent;
  let fixture: ComponentFixture<StudyOrCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StudyOrCreateComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StudyOrCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
