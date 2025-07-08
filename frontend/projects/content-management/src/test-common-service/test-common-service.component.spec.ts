import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TestCommonServiceComponent } from './test-common-service.component';

describe('TestCommonServiceComponent', () => {
  let component: TestCommonServiceComponent;
  let fixture: ComponentFixture<TestCommonServiceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestCommonServiceComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TestCommonServiceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
