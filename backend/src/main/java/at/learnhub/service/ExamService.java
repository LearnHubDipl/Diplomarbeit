package at.learnhub.service;

import at.learnhub.dto.request.CheckAnswersRequestDto;
import at.learnhub.dto.request.CreateExamRequestDto;
import at.learnhub.dto.request.SubmitExamRequestDto;
import at.learnhub.dto.response.CheckAnswersResponseDto;
import at.learnhub.dto.response.CreatedExamResponseDto;
import at.learnhub.dto.response.SubmittedExamResponseDto;
import at.learnhub.dto.simple.ExamQuestionSlimDto;
import at.learnhub.mapper.ExamQuestionMapper;
import at.learnhub.mapper.QuestionMapper;
import at.learnhub.model.*;
import at.learnhub.repository.*;
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
    ExamRepository examRepository;
    @Inject
    ExamQuestionRepository examQuestionRepository;
    @Inject
    AnswerRepository answerRepository;

    @Inject
    AnswerService answerService;
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
            eq.setCorrect(false);
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




    @Transactional
    public SubmittedExamResponseDto submitExam(SubmitExamRequestDto request) {
        Exam exam = examRepository.getEntityById(request.examId());

        // Prevent multiple submissions
        if (exam.isSubmitted()) {
            throw new BadRequestException("Exam has already been submitted.");
        }

        // Iterate over submitted answers
        for (CheckAnswersRequestDto answerDto : request.answers()) {
            // Find the ExamQuestion in this exam corresponding to the original Question ID
            ExamQuestion examQuestion = exam.getQuestions().stream()
                    .filter(eq -> eq.getEntry().getQuestion().getId().equals(answerDto.questionId()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException(
                            "Question ID " + answerDto.questionId() + " is not part of this exam."));

            // grading answers in the answer service
            CheckAnswersResponseDto result = answerService.checkAnswers(answerDto);

            // Persist selected answers if any
            if (answerDto.selectedAnswerIds() != null && !answerDto.selectedAnswerIds().isEmpty()) {
                List<Answer> selectedAnswers = answerDto.selectedAnswerIds().stream()
                        .map(answerRepository::getAnswerById)
                        .toList();
                examQuestion.setSelectedAnswers(selectedAnswers);
            }
            examQuestion.setFreeTextAnswer(answerDto.freeTextAnswer());
            examQuestion.setCorrect(result.correct());

            entityManager.persist(examQuestion);
        }

        // Compute total score
        long totalCorrect = exam.getQuestions().stream()
                .filter(ExamQuestion::getCorrect)
                .count();
        double score = (double) totalCorrect / exam.getQuestions().size() * 100;

        // Update exam with final score and finished timestamp
        exam.setScore(score);
        exam.setFinishedAt(LocalDateTime.now());

        entityManager.persist(exam);

        // Map ExamQuestions to SlimDtos for response
        List<ExamQuestionSlimDto> questionDtos = exam.getQuestions().stream()
                .map(ExamQuestionMapper::toSlimDto)
                .toList();

        return new SubmittedExamResponseDto(exam.getId(), score, questionDtos);
    }

}