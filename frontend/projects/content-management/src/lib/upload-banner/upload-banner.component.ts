import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'lib-upload-banner',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './upload-banner.component.html'
})
export class UploadBannerComponent {
  @Input() title = 'Wollen Sie etwas hochladen?';
  @Input() subtitle = 'Wenn Fach oder Themenpool nicht existieren, können Sie das beim Upload anlegen.';
  @Input() buttonText = 'Upload starten';
  @Input() link = '/startCreate';
}
