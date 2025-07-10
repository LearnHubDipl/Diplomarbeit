import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MaturaTrainerComponent } from './matura-trainer.component';

describe('MaturaTrainerComponent', () => {
  let component: MaturaTrainerComponent;
  let fixture: ComponentFixture<MaturaTrainerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MaturaTrainerComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MaturaTrainerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
