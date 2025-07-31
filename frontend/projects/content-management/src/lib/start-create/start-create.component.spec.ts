import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StartCreateComponent } from './start-create.component';

describe('StartCreateComponent', () => {
  let component: StartCreateComponent;
  let fixture: ComponentFixture<StartCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StartCreateComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StartCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
