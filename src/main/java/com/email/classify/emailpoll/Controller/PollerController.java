package com.email.classify.emailpoll.Controller;

import com.email.classify.emailpoll.DAO.ProblemSet;
import com.email.classify.emailpoll.GmailService.AppService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.email.classify.emailpoll.Utils.APIConstants;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Controller
@RestController
@RequestMapping(APIConstants.REQUEST)
public class PollerController {


@Autowired
AppService appService;

@GetMapping(APIConstants.LOAD_QUES)
public Iterable<ProblemSet> entry()throws IOException, GeneralSecurityException {
    return appService.add();
}

@GetMapping(APIConstants.ALL_QUESTIONS)
public Iterable<ProblemSet> allQuesMethod(){
    return appService.allQues();
}

@GetMapping(APIConstants.EASY_QUES)
    public List<ProblemSet>allEasy(){
    return appService.easy();
}

@GetMapping(APIConstants.MEDIUM_QUES)
    public List<ProblemSet>allMedium(){
    return appService.medium();
}

@GetMapping(APIConstants.HARD_QUES)
    public List<ProblemSet>allHard(){
    return appService.hard();
}
}
