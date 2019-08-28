package pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="quiz_questions")
public class QuizQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "quizQuestion")
    private Set<QuestionAnswer> questionAnswers = new HashSet<>();

    private Integer sequence;

    public QuizQuestion(){

    }

    public QuizQuestion(Quiz quiz, Question question, Integer sequence) {
        this.quiz = quiz;
        this.quiz.addQuizQuestion(this);
        this.question = question;
        question.addQuizQuestion(this);
        this.sequence = sequence;
    }

    public void remove() {
        canRemove();

        quiz.getQuizQuestions().remove(this);
        quiz = null;
        question.getQuizQuestions().remove(this);
        question = null;
    }

    private void canRemove() {
        if (questionAnswers.size() != 0) {
            throw new TutorException(TutorException.ExceptionError.QUIZ_QUESTION_HAS_ANSWERS, sequence.toString());
        }
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Integer getSequence() {
        return sequence;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Set<QuestionAnswer> getQuestionAnswers() {
        return questionAnswers;
    }

    public void setQuestionAnswers(Set<QuestionAnswer> questionAnswers) {
        this.questionAnswers = questionAnswers;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }


    public void addQuestionAnswer(QuestionAnswer questionAnswer) {
        if (questionAnswers == null) {
            questionAnswers = new HashSet<>();
        }
        questionAnswers.add(questionAnswer);
    }

}