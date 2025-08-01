import { Component } from '@angular/core';
import {NgForOf, NgIf,} from '@angular/common';
import {Router} from '@angular/router';

interface Answer {
  id: number;
  text: string;
}

interface Question {
  id: number;
  text: string;
  explanation: string;
  media: string | null;
  type: string;
  answers: Answer[];
}

@Component({
  selector: 'lib-frage-card',
  imports: [
    NgIf,
    NgForOf
  ],
  templateUrl: './frage-card.component.html',
  styleUrl: './frage-card.component.css'
})
export class FrageCardComponent {
  questions: Question[] = [
    {
      id: 1,
      text: 'Was versteht man in der Kombinatorik unter einer Variation ohne Wiederholung?',
      explanation: 'Eine Variation ohne Wiederholung ist eine Anordnung von Objekten, bei der die Reihenfolge wichtig ist und kein Objekt mehrfach verwendet wird.',
      media: null,
      type: 'MULTIPLE_CHOICE',
      answers: [
        { id: 3, text: 'Eine Auswahl von Objekten, bei der Reihenfolge wichtig ist und keine Wiederholung erlaubt ist.' },
        { id: 4, text: 'Eine Auswahl von Objekten, bei der Reihenfolge egal ist und keine Wiederholung erlaubt ist.' },
        { id: 1, text: 'Eine Auswahl von Objekten, bei der Reihenfolge egal ist und Wiederholung erlaubt ist.' },
        { id: 2, text: 'Eine Auswahl von Objekten, bei der Reihenfolge wichtig ist und Wiederholung erlaubt ist.' },
      ]
    },
    {
      id: 2,
      text: 'Welche der folgenden Funktionen ist keine lineare Funktion?',
      explanation: 'Die richtige Antwort lautet: C',
      media: '/assets/karte.png',
      type: 'MULTIPLE_CHOICE',
      answers: [
        { id: 1, text: 'A: f(x)=2x+3' },
        { id: 2, text: 'B: f(x)=-0,5x' },
        { id: 3, text: 'C: f(x)=x²+1' },
        { id: 4, text: 'D: f(x)=3x' },
      ]
    },
    {
      id: 3,
      text: 'Wie viele Permutationen gibt es bei n=4?',
      explanation: 'Die Anzahl ist 4! = 24 Permutationen.',
      media: null,
      type: 'MULTIPLE_CHOICE',
      answers: []
    }
  ];

  currentIndex = 0;
  showAnswer = false;

  constructor(private router: Router) {}

  toggleAnswer() {
    this.showAnswer = !this.showAnswer;
  }

  next() {
    if (this.currentIndex < this.questions.length - 1) {
      this.currentIndex++;
      this.showAnswer = false;
    }
  }

  prev() {
    if (this.currentIndex > 0) {
      this.currentIndex--;
      this.showAnswer = false;
    }
  }

  goToNext() {
    this.next();
  }

  finishLearning() {
    this.router.navigate(['/finished']);
  }
}
