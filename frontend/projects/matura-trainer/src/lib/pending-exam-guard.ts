import {CanDeactivateFn} from '@angular/router';
import {ExamComponent} from './exam/exam.component';
import {map, of} from 'rxjs';
import {catchError} from 'rxjs/operators';

export const pendingExamGuard: CanDeactivateFn<ExamComponent> = (component) => {
  if (!component.exam || component.examSubmitted) {
    return true;
  }

  const confirmLeave = confirm(
    'Prüfung läuft noch! Wenn du jetzt gehst, wird dein aktueller Stand automatisch abgegeben. Fortfahren?'
  );

  if (confirmLeave) {
    component.runner.finish();
    return true;
  }

  return false;
};
