import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StatsExamsComponent } from './stats-exams.component';

describe('StatsExamsComponent', () => {
  let component: StatsExamsComponent;
  let fixture: ComponentFixture<StatsExamsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StatsExamsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StatsExamsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
