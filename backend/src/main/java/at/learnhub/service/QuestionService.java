package at.learnhub.service;

import at.learnhub.dto.request.CheckAnswersRequestDto;
import at.learnhub.dto.response.CheckAnswersResponseDto;
import at.learnhub.dto.simple.QuestionDto;
import at.learnhub.mapper.QuestionMapper;
import at.learnhub.model.Question;
import at.learnhub.model.TopicPool;
import at.learnhub.repository.AnswerRepository;
import at.learnhub.repository.QuestionRepository;
import at.learnhub.repository.TopicPoolRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;

import java.util.HashSet;
import java.util.List;

@ApplicationScoped
public class QuestionService {
    @Inject
    QuestionRepository questionRepository;
    @Inject
    TopicPoolRepository topicPoolRepository;

    public List<QuestionDto> getQuestionsByTopicPool(Long id) {
        TopicPool topicPool = topicPoolRepository.getTopicPoolById(id);
        return topicPool.getQuestions().stream().map(QuestionMapper::toDto).toList();
    }
}
