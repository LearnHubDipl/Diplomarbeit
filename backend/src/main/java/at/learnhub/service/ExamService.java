package at.learnhub.service;

import at.learnhub.dto.request.CreateExamRequestDto;
import at.learnhub.dto.response.CreatedExamResponseDto;
import at.learnhub.mapper.QuestionMapper;
import at.learnhub.model.*;
import at.learnhub.repository.QuestionPoolRepository;
import at.learnhub.repository.TopicPoolRepository;
import at.learnhub.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class ExamService {
    @Inject
    UserRepository userRepository;
    @Inject
    TopicPoolRepository topicPoolRepository;
    @Inject
    QuestionPoolRepository questionPoolRepository;
    @Inject
    EntityManager entityManager;

    @Transactional
    public CreatedExamResponseDto createExam(CreateExamRequestDto request) {
        // check if the specified user exists
        User user = userRepository.getUserById(request.userId());
        // check if all specified topic pools exist
        List<TopicPool> selectedTopicPools = topicPoolRepository.getTopicPoolListByIds(request.topicPoolIds());

        // get all entries of the question pool of the specified user
        List<QuestionPoolEntry> questionPoolEntries = questionPoolRepository.findEntityByUserId(request.userId()).getEntries();
        // filter through the entries to only get the entries of the specified topic pools
        List<QuestionPoolEntry> candidates = new ArrayList<>(questionPoolEntries.stream()
                .filter(e -> selectedTopicPools.contains(e.getQuestion().getTopicPool()))
                .toList());
        if (candidates.isEmpty()) {
            throw new BadRequestException("There are no entries in the question pool for the specified topic pools.");
        }
        Collections.shuffle(candidates);

        List<QuestionPoolEntry> chosenQuestions = new ArrayList<>();

        // Step 1: get minimum one question for each topic pool, if number of question suffices
        List<TopicPool> topicPoolsToUse = new ArrayList<>(selectedTopicPools);
        if (selectedTopicPools.size() > request.numberOfQuestions()) {
            Collections.shuffle(selectedTopicPools); // random order
            topicPoolsToUse = selectedTopicPools.subList(0, request.numberOfQuestions());
        }

        List<QuestionPoolEntry> chosen = new ArrayList<>();
        for (TopicPool pool : topicPoolsToUse) {
            candidates.stream()
                    .filter(e -> e.getQuestion().getTopicPool().equals(pool))
                    .findAny()
                    .ifPresent(chosen::add);
        }

        // Step 2: fill up the rest with random questions
        List<QuestionPoolEntry> remaining = new ArrayList<>(candidates);
        remaining.removeAll(chosen);

        while (chosen.size() < request.numberOfQuestions() && !remaining.isEmpty()) {
            chosen.add(remaining.removeFirst());
        }

        // Step 3: Build the Exam entity
        Exam exam = new Exam();
        exam.setUser(user);
        exam.setTopicPools(selectedTopicPools);
        exam.setTimeLimit(request.timeLimitMinutes());
        exam.setQuestionCount(chosen.size());
        exam.setStartedAt(LocalDateTime.now());

        List<ExamQuestion> examQuestions = chosen.stream().map(entry -> {
            ExamQuestion eq = new ExamQuestion();
            eq.setExam(exam);
            eq.setEntry(entry);
            return eq;
        }).toList();

        exam.setQuestions(examQuestions);

        entityManager.persist(exam);
        return new CreatedExamResponseDto(exam.getId(), exam.getTimeLimit(), exam.getStartedAt(),
                exam.getQuestionCount(), exam.getQuestions().stream().map(eq -> {
                    Question q = eq.getEntry().getQuestion();
                    return QuestionMapper.toSlimDto(q);
                }).toList()
        );
    }
}
