import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StatsTopicsComponent } from './stats-topics.component';

describe('StatsTopicsComponent', () => {
  let component: StatsTopicsComponent;
  let fixture: ComponentFixture<StatsTopicsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StatsTopicsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StatsTopicsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
