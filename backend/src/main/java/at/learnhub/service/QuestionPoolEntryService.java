package at.learnhub.service;

import at.learnhub.model.QuestionPoolEntry;
import at.learnhub.repository.QuestionPoolEntryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@ApplicationScoped
public class QuestionPoolEntryService {

    @Inject
    QuestionPoolEntryRepository repository;

    @Transactional
    public void increaseCorrectCount(Long questionId, Long userId) {
        QuestionPoolEntry entry = repository.findByQuestionIdAndUserId(questionId, userId);

        if (entry != null) {
            entry.setCorrectCount((entry.getCorrectCount() == null ? 0 : entry.getCorrectCount()) + 1);
            entry.setAnsweredAt(LocalDateTime.now());
            entry.setLastAnsweredCorrectly(true);
        }
    }

    @Transactional
    public void markAsIncorrect(Long questionId, Long userId) {
        QuestionPoolEntry entry = repository.findByQuestionIdAndUserId(questionId, userId);
        if (entry != null) {
            entry.setCorrectCount(0);
            entry.setLastAnsweredCorrectly(false);
            entry.setAnsweredAt(LocalDateTime.now());
        }
    }



    public List<QuestionPoolEntry> getAllEntries(Long userId, Long topicPoolId) {
        return repository.findAllByUserAndOptionalTopicPool(userId, topicPoolId);
    }
}