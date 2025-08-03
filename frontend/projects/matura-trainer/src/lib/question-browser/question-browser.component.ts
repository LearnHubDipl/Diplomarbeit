import {Component, inject, Input, OnInit} from '@angular/core';
import {SubjectService} from '../../../../shared/src/lib/services/subject.service';
import { Subject } from '../../../../shared/src/lib/interfaces/subject';
import {NgClass, NgForOf, NgIf} from '@angular/common';
import {TopicPool} from '../../../../shared/src/lib/interfaces/topic-pool';
import {QuestionService} from '../../../../shared/src/lib/services/question.service';
import {Question} from '../../../../shared/src/lib/interfaces/question';
import {Router} from '@angular/router';
import {QuestionPoolService} from '../../../../shared/src/lib/services/question-pool.service';
import {QuestionPoolEntry, QuestionPoolEntryRequest} from '../../../../shared/src/lib/interfaces/question-pool';
import {QuestionBrowsingViewComponent} from '../question-browsing-view/question-browsing-view.component';

@Component({
  selector: 'lib-question-browser',
  imports: [
    NgForOf,
    NgIf,
    NgClass,
    QuestionBrowsingViewComponent
  ],
  templateUrl: './question-browser.component.html',
  styleUrls: [
    '../styles/shared-styles.css',
    './question-browser.component.css'
  ]
})
export class QuestionBrowserComponent {

}
