import { Directive, ElementRef, OnInit, OnDestroy, Renderer2 } from '@angular/core';

@Directive({
  selector: '[appRevealOnScroll]'
})
export class RevealOnScrollDirective implements OnInit, OnDestroy {
  private observer!: IntersectionObserver;

  constructor(private el: ElementRef, private renderer: Renderer2) {}

  ngOnInit() {
    this.observer = new IntersectionObserver(entries => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          this.renderer.addClass(this.el.nativeElement, 'visible');

          if (this.el.nativeElement.classList.contains('pulse')) {
            this.el.nativeElement.classList.add('pulse');
            setTimeout(() => {
              this.el.nativeElement.classList.remove('pulse');
            }, 1500);
          }

          this.observer.unobserve(this.el.nativeElement);
        }
      });
    }, { threshold: 0.5 });

    this.observer.observe(this.el.nativeElement);
  }

  ngOnDestroy() {
    if (this.observer) {
      this.observer.disconnect();
    }
  }
}
