package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.Dashboard;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.FailedAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.FailedAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.repository.DashboardRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.repository.FailedAnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FailedAnswerService {

    @Autowired
    private DashboardRepository dashboardRepository;

    @Autowired
    private FailedAnswerRepository failedAnswerRepository;


    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<FailedAnswerDto> updateFailedAnswers(int dashboardId) {

        Dashboard dashboard = dashboardRepository.findById(dashboardId).orElseThrow(() -> new TutorException(ErrorMessage.DASHBOARD_NOT_FOUND, dashboardId));
        Set<Integer> newFailedAnswers = failedAnswerRepository.findNewFailedAnswer(dashboardId, dashboard.getLastCheckFailedAnswers());

        List<FailedAnswerDto> failedAnswerDtos = new ArrayList<>();

        for(Integer failedAnswerId: newFailedAnswers){
            FailedAnswer toAdd = failedAnswerRepository.findById(failedAnswerId).orElseThrow(() -> new TutorException(ErrorMessage.FAILED_ANSWER_NOT_FOUND, dashboardId));
            dashboard.addFailedAnswer(toAdd);

            failedAnswerDtos.add(new FailedAnswerDto(toAdd));
        }

        return failedAnswerDtos;
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<FailedAnswerDto> getFailedAnswers(int dashboardId) {

        Dashboard dashboard = dashboardRepository.findById(dashboardId).orElseThrow(() -> new TutorException(ErrorMessage.DASHBOARD_NOT_FOUND, dashboardId));

        List<FailedAnswer> failedAnswers = dashboard.getFailedAnswers();
        List<FailedAnswerDto> failedAnswerDtos = new ArrayList<>();

        for(FailedAnswer fa: failedAnswers){
            failedAnswerDtos.add(new FailedAnswerDto(fa));
        }

        return failedAnswerDtos;
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeFailedAnswer(int failedAnswerId, int userId, int dashboardId) {

        Dashboard dashboard = dashboardRepository.findById(dashboardId).orElseThrow(() -> new TutorException(ErrorMessage.DASHBOARD_NOT_FOUND, dashboardId));

        FailedAnswer toRemove = failedAnswerRepository.findById(failedAnswerId).orElseThrow(() -> new TutorException(ErrorMessage.FAILED_ANSWER_NOT_FOUND, failedAnswerId));

        toRemove.setRemoved(true);
        dashboard.removeFailedAnswer(toRemove);
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<FailedAnswerDto> reAddFailedAnswers(int dashboardId, LocalDateTime startDate, LocalDateTime endDate) {

        Dashboard dashboard = dashboardRepository.findById(dashboardId).orElseThrow(() -> new TutorException(ErrorMessage.DASHBOARD_NOT_FOUND, dashboardId));

        Set<Integer> filteredFailedAnswers = failedAnswerRepository.findFailedAnswerFromDate(dashboardId, startDate, endDate);
        List<FailedAnswerDto> failedAnswerDtos = new ArrayList<>();

        for(Integer failedAnswerId: filteredFailedAnswers){
            FailedAnswer toAdd = failedAnswerRepository.findById(failedAnswerId).orElseThrow(() -> new TutorException(ErrorMessage.FAILED_ANSWER_NOT_FOUND, dashboardId));
            
            if(dashboard.hasFailedAnswer(toAdd)){
                dashboard.addFailedAnswer(toAdd);
                failedAnswerDtos.add(new FailedAnswerDto(toAdd));
            }
        }

        return failedAnswerDtos;
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<FailedAnswerDto> getFilteredFailedAnswers(int dashboardId, LocalDateTime startDate, LocalDateTime endDate) {

        Set<Integer> filteredFailedAnswers = failedAnswerRepository.findFailedAnswerFromDate(dashboardId, startDate, endDate);
        List<FailedAnswerDto> failedAnswerDtos = new ArrayList<>();

        for(Integer failedAnswerId: filteredFailedAnswers){
            FailedAnswer fa = failedAnswerRepository.findById(failedAnswerId).orElseThrow(() -> new TutorException(ErrorMessage.FAILED_ANSWER_NOT_FOUND, dashboardId));
            
            failedAnswerDtos.add(new FailedAnswerDto(fa));
        }

        return failedAnswerDtos;
    }
}
