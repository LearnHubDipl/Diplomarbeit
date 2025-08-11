import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PersonalPlaceComponent } from './personal-place.component';

describe('PersonalPlaceComponent', () => {
  let component: PersonalPlaceComponent;
  let fixture: ComponentFixture<PersonalPlaceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PersonalPlaceComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PersonalPlaceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
