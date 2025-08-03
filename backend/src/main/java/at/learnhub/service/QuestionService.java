package at.learnhub.service;

import at.learnhub.dto.request.CheckAnswersRequestDto;
import at.learnhub.dto.request.QuestionCreationRequestDto;
import at.learnhub.dto.response.CheckAnswersResponseDto;
import at.learnhub.dto.simple.QuestionDto;
import at.learnhub.mapper.QuestionMapper;
import at.learnhub.model.Answer;
import at.learnhub.model.Question;
import at.learnhub.model.TopicPool;
import at.learnhub.model.User;
import at.learnhub.repository.AnswerRepository;
import at.learnhub.repository.QuestionRepository;
import at.learnhub.repository.TopicPoolRepository;
import at.learnhub.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@ApplicationScoped
public class QuestionService {
    @Inject
    QuestionRepository questionRepository;
    @Inject
    TopicPoolRepository topicPoolRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    AnswerRepository answerRepository;


    public List<QuestionDto> getQuestionsByTopicPool(Long id) {
        TopicPool topicPool = topicPoolRepository.getTopicPoolById(id);
        return topicPool.getQuestions().stream().map(QuestionMapper::toDto).toList();
    }

    public QuestionDto create(QuestionCreationRequestDto request) {
        Question question = new Question();
        TopicPool topicPool = topicPoolRepository.getTopicPoolById(request.topicPoolId());
        User user = userRepository.getUserById(request.userId());

        question.setTopicPool(topicPool);
        question.setUser(user);
        question.setText(request.text());
        question.setDifficulty(request.difficulty());
        question.setExplanation(request.explanation());
        question.setPublic(request.isPublic());
        question.setType(request.type());
        question.setSolutions(List.of());

        questionRepository.create(question);

        List<Answer> answers = new ArrayList<>();
        for (int i = 0; i < request.answers().size(); i++) {
            Answer answer = new Answer();
            answer.setText(request.answers().get(i).text());
            answer.setCorrect(request.answers().get(i).isCorrect());
            answer.setQuestion(question); // Verknüpfung zur Frage
            answerRepository.persist(answer); // Persistiere die Antwort
            answers.add(answer); // Zur Liste hinzufügen
        }
        question.setAnswers(answers);

        return QuestionMapper.toDto(question);
    }

}
