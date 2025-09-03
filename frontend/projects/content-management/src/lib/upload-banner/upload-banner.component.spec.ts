import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UploadBannerComponent } from './upload-banner.component';

describe('UploadBannerComponent', () => {
  let component: UploadBannerComponent;
  let fixture: ComponentFixture<UploadBannerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UploadBannerComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UploadBannerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
