package at.learnhub.repository;

import at.learnhub.model.Teacher;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class TeacherRepository {

    private final Map<Long, Teacher> store = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(100L);

    @PostConstruct
    void init() {
        save(new Teacher(null, "Christian Aberger", "c.aberger@htl-leonding.ac.at"));
        save(new Teacher(null, "Gerald Aistleitner", "g.aistleitner@htl-leonding.ac.at"));
        save(new Teacher(null, "Herbert Aitenbichler", "h.aitenbichler@htl-leonding.ac.at"));
        save(new Teacher(null, "Benjamin Andraschko", "b.andraschko@htl-leonding.ac.at"));
        save(new Teacher(null, "Peter Anzenberger", "p.anzenberger@htl-leonding.ac.at"));
        save(new Teacher(null, "Eva-Maria Apollonio", "e.apollonio@htl-leonding.ac.at"));
        save(new Teacher(null, "Miriam Arzt", "m.arzt@htl-leonding.ac.at"));
        save(new Teacher(null, "Franz Auernig", "f.auernig@htl-leonding.ac.at"));
        save(new Teacher(null, "Günter Auzinger", "g.auzinger@htl-leonding.ac.at"));
        save(new Teacher(null, "Erich Baar", "e.baar@htl-leonding.ac.at"));
        save(new Teacher(null, "Andreas Bärnthaler", "a.baernthaler@htl-leonding.ac.at"));
        save(new Teacher(null, "Peter Bauer", "p.bauer@htl-leonding.ac.at"));
        save(new Teacher(null, "Daniel Baumann", "d.baumann@htl-leonding.ac.at"));
        save(new Teacher(null, "Vahidin Beluli", "v.beluli@htl-leonding.ac.at"));
        save(new Teacher(null, "Matthias Braun", "m.braun@htl-leonding.ac.at"));
        save(new Teacher(null, "Rosemarie Brenn", "r.brenn@htl-leonding.ac.at"));
        save(new Teacher(null, "Andreas Brückner", "a.brueckner@htl-leonding.ac.at"));
        save(new Teacher(null, "Margit Brückner", "m.brueckner@htl-leonding.ac.at"));
        save(new Teacher(null, "Michael Bucek", "m.bucek@htl-leonding.ac.at"));
        save(new Teacher(null, "Aisha Carrington", "a.carrington@htl-leonding.ac.at"));
        save(new Teacher(null, "Anna Christl", "a.christl@htl-leonding.ac.at"));
        save(new Teacher(null, "Franz Dellinger", "f.dellinger@htl-leonding.ac.at"));
        save(new Teacher(null, "Rainer Denkmair", "r.denkmair@htl-leonding.ac.at"));
        save(new Teacher(null, "Franz Dilly", "f.dilly@htl-leonding.ac.at"));
        save(new Teacher(null, "Josef Draxlbauer", "j.draxlbauer@htl-leonding.ac.at"));
        save(new Teacher(null, "Michael Dullnig", "m.dullnig@htl-leonding.ac.at"));
        save(new Teacher(null, "Günther Ehrenberger", "g.ehrenberger@htl-leonding.ac.at"));
        save(new Teacher(null, "Markus Ehrenmüller-Jensen", "m.ehrenmueller-jensen@htl-leonding.ac.at"));
        save(new Teacher(null, "Clemens Eisserer", "c.eisserer@htl-leonding.ac.at"));
        save(new Teacher(null, "Patricia Engleitner", "p.engleitner@htl-leonding.ac.at"));
        save(new Teacher(null, "Mario Enzenhofer", "m.enzenhofer@htl-leonding.ac.at"));
        save(new Teacher(null, "Christina Eppich", "c.eppich@htl-leonding.ac.at"));
        save(new Teacher(null, "Björn Ernecker", "b.ernecker@htl-leonding.ac.at"));
        save(new Teacher(null, "Anja Felsner", "a.felsner@htl-leonding.ac.at"));
        save(new Teacher(null, "Petre Floarea", "p.floarea@htl-leonding.ac.at"));
        save(new Teacher(null, "Peter Frey", "p.frey@htl-leonding.ac.at"));
        save(new Teacher(null, "Josef Fürlinger", "j.fuerlinger@htl-leonding.ac.at"));
        save(new Teacher(null, "Andreas Gallistl", "a.gallistl@htl-leonding.ac.at"));
        save(new Teacher(null, "Katharina Gallner-Holzmann", "k.gallner-holzmann@htl-leonding.ac.at"));
        save(new Teacher(null, "Gerhard Gehrer", "g.gehrer@htl-leonding.ac.at"));
        save(new Teacher(null, "Rudolf Giritzhofer", "r.giritzhofer@htl-leonding.ac.at"));
        save(new Teacher(null, "Markus Gruber", "m.gruber@htl-leonding.ac.at"));
        save(new Teacher(null, "Franz Gruber-Leitner", "f.gruber-leitner@htl-leonding.ac.at"));
        save(new Teacher(null, "Hans-Christian Hammer", "h.hammer@htl-leonding.ac.at"));
        save(new Teacher(null, "Klaus Haslinger", "k.haslinger@htl-leonding.ac.at"));
        save(new Teacher(null, "Markus Haslinger", "m.haslinger@htl-leonding.ac.at"));
        save(new Teacher(null, "Gerhard Höfer", "g.hoefer@htl-leonding.ac.at"));
        save(new Teacher(null, "Jürgen Holzleitner", "j.holzleitner@htl-leonding.ac.at"));
        save(new Teacher(null, "Michael Holzmann", "m.holzmann@htl-leonding.ac.at"));
        save(new Teacher(null, "Martin Huemer", "m.huemer@htl-leonding.ac.at"));
        save(new Teacher(null, "Sara Ivcevic", "s.ivcevic@htl-leonding.ac.at"));
        save(new Teacher(null, "Franz Jakob", "f.jakob@htl-leonding.ac.at"));
        save(new Teacher(null, "Philipp Jansch", "p.jansch@htl-leonding.ac.at"));
        save(new Teacher(null, "Richard Kainerstorfer", "r.kainerstorfer@htl-leonding.ac.at"));
        save(new Teacher(null, "Alexander Kaiser", "a.kaiser@htl-leonding.ac.at"));
        save(new Teacher(null, "Michal Karpowicz", "m.karpowicz@htl-leonding.ac.at"));
        save(new Teacher(null, "Johannes Kasberger", "j.kasberger@htl-leonding.ac.at"));
        save(new Teacher(null, "Christof Kaser", "c.kaser@htl-leonding.ac.at"));
        save(new Teacher(null, "Roland Kasmannhuber", "r.kasmannhuber@htl-leonding.ac.at"));
        save(new Teacher(null, "Edith Keplinger", "e.keplinger@htl-leonding.ac.at"));
        save(new Teacher(null, "Adrian Kern", "a.kern@htl-leonding.ac.at"));
        save(new Teacher(null, "Martin Kerschner", "m.kerschner@htl-leonding.ac.at"));
        save(new Teacher(null, "Maximilian Kiesenhofer", "m.kiesenhofer@htl-leonding.ac.at"));
        save(new Teacher(null, "David Klewein", "d.klewein@htl-leonding.ac.at"));
        save(new Teacher(null, "Kerstin Knogler", "k.knogler@htl-leonding.ac.at"));
        save(new Teacher(null, "Johann Köck", "j.koeck@htl-leonding.ac.at"));
        save(new Teacher(null, "Christina Kodre", "c.kodre@htl-leonding.ac.at"));
        save(new Teacher(null, "Alexander Kornfellner", "a.kornfellner@htl-leonding.ac.at"));
        save(new Teacher(null, "Matthias Kurz", "m.kurz@htl-leonding.ac.at"));
        save(new Teacher(null, "Harald Landvoigt", "h.landvoigt@htl-leonding.ac.at"));
        save(new Teacher(null, "Christian Lehenbauer", "c.lehenbauer@htl-leonding.ac.at"));
        save(new Teacher(null, "Helmut Leitner", "h.leitner@htl-leonding.ac.at"));
        save(new Teacher(null, "Markus Lüftner", "m.lueftner@htl-leonding.ac.at"));
        save(new Teacher(null, "Karin Lugmayr", "k.lugmayr@htl-leonding.ac.at"));
        save(new Teacher(null, "Christian Luttinger", "c.luttinger@htl-leonding.ac.at"));
        save(new Teacher(null, "Cornelia Mader", "c.mader@htl-leonding.ac.at"));
        save(new Teacher(null, "Barbara Matzinger", "b.matzinger@htl-leonding.ac.at"));
        save(new Teacher(null, "Gernot Mischitz", "g.mischitz@htl-leonding.ac.at"));
        save(new Teacher(null, "Wolfgang Mistelberger", "w.mistelberger@htl-leonding.ac.at"));
        save(new Teacher(null, "Anila Morina", "a.morina@htl-leonding.ac.at"));
        save(new Teacher(null, "Edith Möschl", "e.moeschl@htl-leonding.ac.at"));
        save(new Teacher(null, "Tamara Moser", "t.moser@htl-leonding.ac.at"));
        save(new Teacher(null, "Regina Mühleder", "r.muehleder@htl-leonding.ac.at"));
        save(new Teacher(null, "Philipp Mühlehner", "p.muehlehner@htl-leonding.ac.at"));
        save(new Teacher(null, "Lorenz Nitsch", "l.nitsch@htl-leonding.ac.at"));
        save(new Teacher(null, "Alois Oswald", "a.oswald@htl-leonding.ac.at"));
        save(new Teacher(null, "Cornelia Pachschwöll", "c.pachschwoell@htl-leonding.ac.at"));
        save(new Teacher(null, "Michael Palitsch-Infanger", "m.palitsch-infanger@htl-leonding.ac.at"));
        save(new Teacher(null, "Katharina Povacz", "k.povacz@htl-leonding.ac.at"));
        save(new Teacher(null, "Anton Prantl", "a.prantl@htl-leonding.ac.at"));
        save(new Teacher(null, "Natascha Rammelmüller", "n.rammelmueller@htl-leonding.ac.at"));
        save(new Teacher(null, "Robert Raschhofer", "r.raschhofer@htl-leonding.ac.at"));
        save(new Teacher(null, "Robert Reder", "r.reder@htl-leonding.ac.at"));
        save(new Teacher(null, "Josef Reichinger", "j.reichinger@htl-leonding.ac.at"));
        save(new Teacher(null, "Martina Reisenberger", "m.reisenberger@htl-leonding.ac.at"));
        save(new Teacher(null, "Hannes Reisinger", "h.reisinger@htl-leonding.ac.at"));
        save(new Teacher(null, "Riccarda Reiter", "r.reiter@htl-leonding.ac.at"));
        save(new Teacher(null, "Marion Riepler", "m.riepler@htl-leonding.ac.at"));
        save(new Teacher(null, "Helmut Rockenschaub", "h.rockenschaub@htl-leonding.ac.at"));
        save(new Teacher(null, "Elisabeth Rumetshofer", "e.rumetshofer@htl-leonding.ac.at"));
        save(new Teacher(null, "Silke Scheinecker", "s.scheinecker@htl-leonding.ac.at"));
        save(new Teacher(null, "Ralf Schlesinger", "r.schlesinger@htl-leonding.ac.at"));
        save(new Teacher(null, "Christian Schmidt", "c.schmidt@htl-leonding.ac.at"));
        save(new Teacher(null, "Silke Schmidt-Wirth", "s.schmidt-wirth@htl-leonding.ac.at"));
        save(new Teacher(null, "Claudia Schönegger", "c.schoenegger@htl-leonding.ac.at"));
        save(new Teacher(null, "Stefan Schraml", "s.schraml@htl-leonding.ac.at"));
        save(new Teacher(null, "Birgit Schröder", "b.schroeder@htl-leonding.ac.at"));
        save(new Teacher(null, "Elke Schumacher", "e.schumacher@htl-leonding.ac.at"));
        save(new Teacher(null, "Erik Sonnleitner", "e.sonnleitner@htl-leonding.ac.at"));
        save(new Teacher(null, "Dietmar Steiner", "d.steiner@htl-leonding.ac.at"));
        save(new Teacher(null, "Martin Steiner", "m.steiner@htl-leonding.ac.at"));
        save(new Teacher(null, "Martin Sternath", "m.sternath@htl-leonding.ac.at"));
        save(new Teacher(null, "Gerhard Stöpp", "g.stoepp@htl-leonding.ac.at"));
        save(new Teacher(null, "Robert Stöttinger", "r.stoettinger@htl-leonding.ac.at"));
        save(new Teacher(null, "Alexander Strecker", "a.strecker@htl-leonding.ac.at"));
        save(new Teacher(null, "Rainer Stropek", "r.stropek@htl-leonding.ac.at"));
        save(new Teacher(null, "Thomas Stütz", "t.stuetz@htl-leonding.ac.at"));
        save(new Teacher(null, "Günter Traunmüller", "g.traunmueller@htl-leonding.ac.at"));
        save(new Teacher(null, "Marie Tscherne", "m.tscherne@htl-leonding.ac.at"));
        save(new Teacher(null, "Johannes Tumfart", "j.tumfart@htl-leonding.ac.at"));
        save(new Teacher(null, "Gerald Unterrainer", "g.unterrainer@htl-leonding.ac.at"));
        save(new Teacher(null, "Bernhard Venzl", "b.venzl@htl-leonding.ac.at"));
        save(new Teacher(null, "Michael Wagner", "m.wagner@htl-leonding.ac.at"));
        save(new Teacher(null, "Florian Weber", "f.weber@htl-leonding.ac.at"));
        save(new Teacher(null, "Michael Weilguni", "m.weilguni@htl-leonding.ac.at"));
        save(new Teacher(null, "Alexandra Wellisch", "a.wellisch@htl-leonding.ac.at"));
        save(new Teacher(null, "Siegfried Wenigwieser", "s.wenigwieser@htl-leonding.ac.at"));
        save(new Teacher(null, "Alfred Wiedermann", "a.wiedermann@htl-leonding.ac.at"));
        save(new Teacher(null, "Edwin Wingert", "e.wingert@htl-leonding.ac.at"));
        save(new Teacher(null, "Lien Hochbichler", "l.hochbichler@students.htl-leonding.ac.at"));
    }

    public List<Teacher> listAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(Teacher::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public Teacher save(Teacher t) {
        if (t.getId() == null) {
            t.setId(seq.incrementAndGet());
        }
        store.put(t.getId(), t);
        return t;
    }

    public Optional<Teacher> update(Long id, Teacher patch) {
        Teacher current = store.get(id);
        if (current == null) return Optional.empty();
        if (patch.getName() != null && !patch.getName().isBlank()) current.setName(patch.getName());
        if (patch.getEmail() != null) current.setEmail(patch.getEmail());
        return Optional.of(current);
    }

    public boolean delete(Long id) {
        return store.remove(id) != null;
    }
}
