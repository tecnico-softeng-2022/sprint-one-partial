package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.course.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.TopicConjunction
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User

import java.time.LocalDateTime

@DataJpaTest
class TournamentTest extends SpockTest {
    public static final String STRING_DATE_TODAY = DateHandler.toISOString(DateHandler.now())

    def assessment
    def topic1
    def topic2
    def topics = new HashSet<Integer>()
    def user1

    def setup() {
        user1 = createUser(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, User.Role.STUDENT, externalCourseExecution)

        def topicDto1 = new TopicDto()
        topicDto1.setName(TOPIC_1_NAME)
        topic1 = new Topic(externalCourse, topicDto1)
        topicRepository.save(topic1)

        def topicDto2 = new TopicDto()
        topicDto2.setName(TOPIC_2_NAME)
        topic2 = new Topic(externalCourse, topicDto2)
        topicRepository.save(topic2)

        topics.add(topic1.getId())
        topics.add(topic2.getId())
    }

    def createUser(String name, String username, String email, User.Role role, CourseExecution courseExecution) {
        def user = new User(name, username, email, role, false, false)
        user.addCourse(courseExecution)
        userRepository.save(user)
        user.setKey(user.getId())

        return user
    }

    def createTournament(User user, String startTime, String endTime, Integer numberOfQuestions, boolean isCanceled) {
        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(startTime)
        tournamentDto.setEndTime(endTime)
        tournamentDto.setNumberOfQuestions(numberOfQuestions)
        tournamentDto.setCanceled(isCanceled)
        tournamentDto = tournamentService.createTournament(user.getId(), externalCourseExecution.getId(), topics, tournamentDto)

        return tournamentDto
    }

    def createPrivateTournament(User user, String startTime, String endTime, Integer numberOfQuestions, boolean isCanceled, String password) {
        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(startTime)
        tournamentDto.setEndTime(endTime)
        tournamentDto.setNumberOfQuestions(numberOfQuestions)
        tournamentDto.setCanceled(isCanceled)
        tournamentDto.setPrivateTournament(true)
        tournamentDto.setPassword(password)
        tournamentDto = tournamentService.createTournament(user.getId(), externalCourseExecution.getId(), topics, tournamentDto)

        return tournamentDto
    }

    def createAssessmentWithTopicConjunction(String title, Assessment.Status status, CourseExecution courseExecution) {
        assessment = new Assessment()
        assessment.setTitle(title)
        assessment.setStatus(status)
        assessment.setCourseExecution(courseExecution)

        def topicConjunction = new TopicConjunction()
        topicConjunction.addTopic(topic1)
        topicConjunction.addTopic(topic2)

        assessment.addTopicConjunction(topicConjunction)
        assessmentRepository.save(assessment)
    }

    def createQuestion(LocalDateTime creationDate, String content, String title, Question.Status status, Course course) {
        def question = new Question()
        question.setKey(1)
        question.setCreationDate(creationDate)
        question.setContent(content)
        question.setTitle(title)
        question.setStatus(status)
        question.setCourse(course)
        question.addTopic(topic1)
        question.addTopic(topic2)
        questionRepository.save(question)

        return question
    }

    def createOption(String content, Question question) {
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        question.setOptions(options)
        questionRepository.save(question)
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
