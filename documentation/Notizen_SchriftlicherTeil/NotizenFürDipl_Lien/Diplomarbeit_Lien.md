# Notiz nr. 1
### Startseite | 10.08. 2025

Achtung: Scroll - Elemente via Angular

Lösung:  `RevealOnScrollDirective` ist eine Angular Hilfsklasse, die schaut ob ein Element sich schon im zu sehenden Bereich ist. Wenn ja dann fügt es die visible klasse hinzu und das führt zur css- Animation. Wie Oberservable in js nur für Angular.

NICHT VERGESSEN IM TYPESCRIPT DES COMPONENTS ZU IMPORTIEREN.

``` typescript
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
````
``

--- 

# Notiz nr. 2

### TopicContent | 10.08.2025

private String title;

@Column(length = 2000)
private String description;

in Thomas vorgefertigter Klasse hinzugefügt 


=> **Titel / Beschreibung** – Damit du im Frontend einen aussagekräftigen Namen anzeigen kannst, ohne nur den Dateinamen zu verwenden.

---

# Notiz nr. 3
### TopicContent | 10.8.2025

  **`TopicContent`** ist kein CDN-Server – er ist eher wie ein „Daten-Container“ in deiner Datenbank, der beschreibt, **welche Inhalte zu welchem Fach/Themenpool gehören**, und **wer sie hochgeladen/unterrichtet/freigegeben** hat.

---


generelles backend schema: 
--
## **1. Übersicht 

### **Modelle (Entities)**

- `Subject`
    
- `TopicPool`
    
- `TopicContent`
    
- `MediaFile`
    
- `User`
    
- _(evtl. weitere wie Question etc., die schon vorher existierten)_
    

➡️ Diese sind direkt mit JPA/Hibernate an die Datenbank angebunden.  
➡️ Sie bilden die Tabellenstruktur.

---

### **DTOs (Data Transfer Objects)**

Zweck: Trennen von Datenbank-Entities und API-Datenmodell, damit die API nicht direkt DB-Struktur preisgibt.

- `SubjectDto` – detaillierte Fach-Infos inkl. TopicPools
    
- `SubjectSlimDto` – nur Basisinfos zum Fach
    
- `TopicPoolSlimDto` – abgespeckte Info zu TopicPools
    
- `TopicContentSlimDto` – Infos zu Mitschriften/Uploads
    
- `UserSlimDto` – komprimierte Lehrer-/Benutzerdaten
    
- Request-DTOs:
    
    - `CreateSubjectRequestDto`
        
    - `UpdateSubjectRequestDto`
        
    - `CreateTopicPoolRequestDto`
        
    - `CreateTopicPoolBatchRequestDto`
        
    - _(evtl. UploadRequestDto für Dateien)_
        

---

### **Mapper**

Zweck: Wandeln **Entities ↔ DTOs** um.  
Wir haben statische Utility-Klassen gemacht, z. B.:

- `SubjectMapper`
    
- `TopicPoolMapper`
    
- `TopicContentMapper`
    
- `UserMapper`
    
- `MediaFileMapper` (für abgespeckte Datei-Infos)
    

Diese Mapper werden von Repositories oder Resources aufgerufen, um saubere JSON-Ausgaben zu bekommen.

---

### **Repositories**

Zweck: Kapseln aller DB-Operationen mit `EntityManager`, **ohne Panache**.  
Alle Repos haben:

- **Finder-Methoden** (`findById`, `findAll...`)
    
- **Persistierungsmethoden** (`create`, `update`, `delete`)
    
- **DTO-Rückgabe** direkt über Mapper (spart Service-Layer, wenn simpel)
    

Erstellt:

- `SubjectRepository`
    
- `TopicPoolRepository`
    
- `TopicContentRepository`
    
- `MediaFileRepository`
    
- `UserRepository`
    

---

### **Resources (REST Controller)**

Zweck: API-Endpunkte für Frontend bereitstellen, ruft Repositories & Mapper auf.

Gemacht:

- `SubjectResource`
    
- `TopicPoolResource`
    
- `TopicContentResource` (inkl. Upload)
    
- `FileResource` (liefert PDF/Thumbnail direkt vom Server)
    
- `TeacherResource` (für aktive Lehrerlisten)
    

---

## **2. Wie alles miteinander verschachtelt ist**

```
[Frontend Angular] 
     ↓  HTTP JSON
[Resource-Klasse]  → ruft →  [Repository]  → arbeitet mit →  [EntityManager + Entities]
     ↑                              ↓
 [Mapper-Klasse]  ← wandelt DTOs ↔ Entities
```

Beispiel: **GET /api/subjects**

1. Angular ruft `/api/subjects`.
    
2. `SubjectResource.listAll()` ruft `subjectRepo.findAllOrderedByName()`.
    
3. Repo holt Entities aus DB (`EntityManager`), mappt mit `SubjectMapper.toDto(...)` → DTO-Liste.
    
4. Resource gibt JSON-Liste zurück.
    

---

## **3. Warum kein CDN, sondern FileResource**

Wir haben uns für einen **lokalen Datei-Serving-Ansatz** entschieden:

- Dateien (PDF, Thumbnails) liegen **auf deinem Server**.
    
- Abruf über API-Endpunkte wie `/api/files/thumbnail/{id}` oder `/api/files/pdf/{id}`.
    
- Vorteil:
    
    - **Zugriffskontrolle** (nur eingeloggte oder berechtigte Nutzer).
        
    - **Kein externes Hosting nötig** → Datenschutz / DSGVO-konform.
        
    - Einfacher Deployment-Flow (alles bleibt im Quarkus-Backend).
        
    - Einheitliche Authentifizierung (keine CORS-/Token-Probleme mit CDN).
        
- Nachteil:
    
    - Höhere Serverlast bei großen Dateien.
        
    - Kein global verteiltes CDN-Caching.
        



---
