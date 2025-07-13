package at.learnhub.repository;

import at.learnhub.model.Question;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class QuestionRepository {
    @Inject
    EntityManager em;

    public List<QuestionDto> findAll() {
        return em.createQuery("SELECT q FROM Question q", Question.class)
                .getResultList()
                .stream()
                .collect(Collectors.toList());
    }

        Question question = em.find(Question.class, id);
        }
    }

    }
}
