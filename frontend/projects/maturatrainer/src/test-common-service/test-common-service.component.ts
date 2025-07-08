import {Component} from '@angular/core';
import {TestService} from 'shared-services';

@Component({
  selector: 'app-test-common-service',
  imports: [],
  templateUrl: './test-common-service.component.html',
  styleUrl: './test-common-service.component.css'
})
export class TestCommonServiceComponent {
  message = '';

  constructor(private commonService: TestService) {
  }

  ngOnInit() {
    this.message = this.commonService.getTestMessage();
  }
}
