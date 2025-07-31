import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FragenKonfiguratorComponent } from './fragen-konfigurator.component';

describe('FragenKonfiguratorComponent', () => {
  let component: FragenKonfiguratorComponent;
  let fixture: ComponentFixture<FragenKonfiguratorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FragenKonfiguratorComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FragenKonfiguratorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
