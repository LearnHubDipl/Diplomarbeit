import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Subject } from '../../../../shared/src/lib/interfaces/subject';

@Component({
  selector: 'lib-subject-card',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './subject-card.component.html'
})
export class SubjectCardComponent {
  @Input() subject!: Subject;
  @Output() edit = new EventEmitter<Subject>();
  @Output() remove = new EventEmitter<Subject>();

  menuOpen = false;

  toggleMenu(ev: MouseEvent) {
    ev.stopPropagation();
    this.menuOpen = !this.menuOpen;
  }

  closeMenu() {
    this.menuOpen = false;
  }

  onEdit(ev: MouseEvent) {
    ev.preventDefault();
    ev.stopPropagation();
    this.edit.emit(this.subject);
    this.closeMenu();
  }

  onDelete(ev: MouseEvent) {
    ev.preventDefault();
    ev.stopPropagation();
    this.remove.emit(this.subject);
    this.closeMenu();
  }
}
