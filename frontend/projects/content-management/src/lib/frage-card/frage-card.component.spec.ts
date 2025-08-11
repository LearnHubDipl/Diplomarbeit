import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FrageCardComponent } from './frage-card.component';

describe('FrageCardComponent', () => {
  let component: FrageCardComponent;
  let fixture: ComponentFixture<FrageCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FrageCardComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FrageCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
