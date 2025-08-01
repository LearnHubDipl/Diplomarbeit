import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FinishedCardComponent } from './finished-card.component';

describe('FinishedCardComponent', () => {
  let component: FinishedCardComponent;
  let fixture: ComponentFixture<FinishedCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FinishedCardComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FinishedCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
