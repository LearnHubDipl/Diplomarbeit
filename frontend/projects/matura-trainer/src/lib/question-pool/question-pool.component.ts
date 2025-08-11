import {Component, inject} from '@angular/core';
import {NgClass, NgForOf, NgIf} from '@angular/common';
import {Router} from '@angular/router';
import {SubjectService} from '../../../../shared/src/lib/services/subject.service';
import {Subject} from '../../../../shared/src/lib/interfaces/subject';
import {QuestionService} from '../../../../shared/src/lib/services/question.service';
import {Question} from '../../../../shared/src/lib/interfaces/question';
import {QuestionPoolService} from '../../../../shared/src/lib/services/question-pool.service';
import {TopicPool} from '../../../../shared/src/lib/interfaces/topic-pool';
import {QuestionPoolEntry} from '../../../../shared/src/lib/interfaces/question-pool';
import {QuestionBrowsingViewComponent} from '../question-browsing-view/question-browsing-view.component';

/**
 * Currently just the question browser component copied. TODO: make changes necessary for editing the question pool
 */

@Component({
  selector: 'lib-question-pool',
  imports: [
    NgForOf,
    NgIf,
    NgClass,
    QuestionBrowsingViewComponent
  ],
  templateUrl: './question-pool.component.html',
  styleUrls: [
    './question-pool.component.css',
    '../styles/shared-styles.css'
  ]
})
export class QuestionPoolComponent {

}
