package unipi.protal.countriesteach.screens.game;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Predicate;

import unipi.protal.countriesteach.callables.SolutionCallable;
import unipi.protal.countriesteach.database.CountryContentValues;
import unipi.protal.countriesteach.database.CountryDao;
import unipi.protal.countriesteach.database.Database;
import unipi.protal.countriesteach.database.QuestionDao;
import unipi.protal.countriesteach.database.QuestionQuizCrossRefDao;
import unipi.protal.countriesteach.database.QuizDao;
import unipi.protal.countriesteach.entities.Country;
import unipi.protal.countriesteach.entities.Question;
import unipi.protal.countriesteach.entities.QuestionQuizCrossRef;
import unipi.protal.countriesteach.entities.Quiz;

import static unipi.protal.countriesteach.database.CountryContentValues.AFRICA_END_INDEX;
import static unipi.protal.countriesteach.database.CountryContentValues.AFRICA_START_INDEX;
import static unipi.protal.countriesteach.database.CountryContentValues.AMERICA_END_INDEX;
import static unipi.protal.countriesteach.database.CountryContentValues.AMERICA_START_INDEX;
import static unipi.protal.countriesteach.database.CountryContentValues.ASIA_END_INDEX;
import static unipi.protal.countriesteach.database.CountryContentValues.ASIA_START_INDEX;
import static unipi.protal.countriesteach.database.CountryContentValues.EUROPE;
import static unipi.protal.countriesteach.database.CountryContentValues.EUROPE_END_INDEX;
import static unipi.protal.countriesteach.database.CountryContentValues.EUROPE_START_INDEX;
import static unipi.protal.countriesteach.database.CountryContentValues.OCEANIA_END_INDEX;
import static unipi.protal.countriesteach.database.CountryContentValues.OCEANIA_START_INDEX;
import static unipi.protal.countriesteach.database.CountryContentValues.WORLD_END_INDEX;
import static unipi.protal.countriesteach.database.CountryContentValues.WORLD_START_INDEX;

public class GameViewModel extends AndroidViewModel {
    private LiveData<List<Country>> europeanCountries, asianCountries, americanCountries, oceanianCountries, africanCountries, antarticaCountries, allCountries;
    public MutableLiveData<List<Question>> quizQuestions;
    public MutableLiveData<Integer> numberOfQuestion = new MutableLiveData<>();
    public MutableLiveData<Integer> countryIndex = new MutableLiveData<>();
    public MutableLiveData<Integer> firstAnswerIndex = new MutableLiveData<>();
    public MutableLiveData<Integer> secondAnswerIndex = new MutableLiveData<>();
    public MutableLiveData<Integer> thirdAnswerIndex = new MutableLiveData<>();
    public MutableLiveData<Integer> fourthAnswerIndex = new MutableLiveData<>();
    public LiveData<Integer> currentLevel;
    public MutableLiveData<Integer> nextLevel;
    public LiveData<Integer> quizLevel;
    public MutableLiveData<Long> _quizId = new MutableLiveData<>();
    public MutableLiveData<List<Country>> quizCountries = new MutableLiveData<>();
    private static final int NUMBER_OF_QUESTIONS = 10;
    private int numberOfCountries, questionIndex, startIndex, endIndex;
    private Random random = new Random();
    private CountryDao countryDao;
    private QuizDao quizDao;
    private QuestionDao questionDao;
    private QuestionQuizCrossRefDao questionQuizCrossRefDao;
    private final Executor executor = Executors.newSingleThreadExecutor();
    //    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Quiz quiz;
    private Long quizId;
    private List<Question> questions;

    public GameViewModel(@NonNull Application application, int continentId) {
        super(application);
        numberOfQuestion.setValue(1);
        questionIndex = 0;
        setIndex(continentId);
        Database db = Database.getDatabase(application);
        countryDao = db.countryDao();
        quizDao = db.quizDao();
        questionDao = db.questionDao();
        questionQuizCrossRefDao = db.questionQuizCrossRefDao();
        europeanCountries = countryDao.getEuropeanCountries();
        africanCountries = countryDao.getAfricanCountries();
        americanCountries = countryDao.getAmericanCountries();
        asianCountries = countryDao.getAsianCountries();
        oceanianCountries = countryDao.getOceanianCountries();
        antarticaCountries = countryDao.getAntarcticaCountries();
        allCountries = countryDao.getAllCountries();
        currentLevel = quizDao.getQuizLevel(continentId);
        if (continentId == CountryContentValues.EUROPE) {
            numberOfCountries = CountryContentValues.NUMBER_OF_EUROPEAN_COUNTRIES;
        } else if (continentId == CountryContentValues.AMERICA) {
            numberOfCountries = CountryContentValues.NUMBER_OF_AMERICAN_COUNTRIES;
        } else if (continentId == CountryContentValues.ASIA) {
            numberOfCountries = CountryContentValues.NUMBER_OF_ASIAN_COUNTRIES;
        } else if (continentId == CountryContentValues.AFRICA) {
            numberOfCountries = CountryContentValues.NUMBER_OF_AFRICAN_COUNTRIES;
        } else if (continentId == CountryContentValues.OCEANIA) {
            numberOfCountries = CountryContentValues.NUMBER_OF_OCEANIAN_COUNTRIES;
        } else if (continentId == CountryContentValues.WORLD) {
            numberOfCountries = CountryContentValues.NUMBER_OF_ALL_COUNTRIES;
        }
        List<Integer> countryIds = selectQuestions(continentId);
        quiz = new Quiz();
        quiz.setStartDateMillis(Calendar.getInstance().getTimeInMillis());
        quiz.setContinentId(continentId);
        quiz.setDifficultyLevel(1);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                quizId = quizDao.insertQuiz(quiz);
            }
        });
        questions = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_QUESTIONS; i++) {
            Question question = new Question(countryIds.get(i));
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Long questionId = questionDao.insertQuestion(question);
                    QuestionQuizCrossRef questionQuizCrossRef = new QuestionQuizCrossRef(quizId, questionId);
                    questionQuizCrossRefDao.insertQuestionQuizRef(questionQuizCrossRef);
                }
            });
            questions.add(question);
        }
        _quizId.setValue(quizId);
        quizQuestions = new MutableLiveData<List<Question>>(questions);
        nextCountryIndex();
    }


    @Override
    protected void onCleared() {
        super.onCleared();
    }

    public LiveData<List<Country>> getAllCountries() {
        return allCountries;
    }

    public LiveData<List<Country>> getQuizCountries(int id) {
        if (id == CountryContentValues.EUROPE) {
            return europeanCountries;
        } else if (id == CountryContentValues.AMERICA) {
            return americanCountries;
        } else if (id == CountryContentValues.ASIA) {
            return asianCountries;
        } else if (id == CountryContentValues.AFRICA) {
            return africanCountries;
        } else if (id == CountryContentValues.OCEANIA) {
            return oceanianCountries;
        } else if (id == CountryContentValues.WORLD) {
            return allCountries;
        }
        return allCountries;
    }

    public MutableLiveData<List<Question>> getQuizQuestions() {
        return quizQuestions;
    }

    public MutableLiveData<Long> getQuizId() {
        if (quizId != null) {
            _quizId.setValue(quizId);
        }
        return _quizId;
    }

    public LiveData<Integer> getCurrentLevel() {
            return currentLevel;
    }

    public void nextCountryIndex() {
        try {
            countryIndex.setValue((int) questions.get(questionIndex).getCountryId());
            if (questionIndex < 10) {
                questionIndex++;
            }
            getRandomAnswersIndex();
        } catch (IndexOutOfBoundsException ie) {
            ie.printStackTrace();
        }

    }

    public int getNumberOfQuestions() {
        return NUMBER_OF_QUESTIONS;
    }

    public void getRandomAnswersIndex() {
        List<Integer> possibleAnswers = new ArrayList<>();
        possibleAnswers.add(countryIndex.getValue());
        Log.e("index 3 ", String.valueOf(countryIndex.getValue()));
        while (possibleAnswers.size() < 4) {
            Integer randomAnswer = random.nextInt(endIndex - startIndex) + startIndex;
            Predicate<Integer> answers = i -> (possibleAnswers.contains(i));
            while (answers.test(randomAnswer)) {
                randomAnswer = random.nextInt(endIndex - startIndex) + startIndex;
            }
            possibleAnswers.add(randomAnswer);
        }
        Collections.shuffle(possibleAnswers);
        firstAnswerIndex.setValue(possibleAnswers.get(0));
        secondAnswerIndex.setValue(possibleAnswers.get(1));
        thirdAnswerIndex.setValue(possibleAnswers.get(2));
        fourthAnswerIndex.setValue(possibleAnswers.get(3));
    }

    private void setIndex(int id) {
        if (id == EUROPE) {
            startIndex = EUROPE_START_INDEX;
            endIndex = EUROPE_END_INDEX;
        } else if (id == CountryContentValues.AMERICA) {
            startIndex = AMERICA_START_INDEX;
            endIndex = AMERICA_END_INDEX;
        } else if (id == CountryContentValues.ASIA) {
            startIndex = ASIA_START_INDEX;
            endIndex = ASIA_END_INDEX;
        } else if (id == CountryContentValues.AFRICA) {
            startIndex = AFRICA_START_INDEX;
            endIndex = AFRICA_END_INDEX;
        } else if (id == CountryContentValues.OCEANIA) {
            startIndex = OCEANIA_START_INDEX;
            endIndex = OCEANIA_END_INDEX;
        } else if (id == CountryContentValues.WORLD) {
            startIndex = WORLD_START_INDEX;
            endIndex = WORLD_END_INDEX;
        }
    }

    public List<Integer> selectQuestions(int id) {
        setIndex(id);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        List<Integer> solution = null;
        int difficulty=1;
        try{
            difficulty=currentLevel.getValue();
        }catch (NullPointerException nullPointerException){
            nullPointerException.printStackTrace();
        }
        Future<List<Integer>> solutionFuture = executorService.submit(new SolutionCallable(id, startIndex, endIndex, questionDao,quizDao,questionQuizCrossRefDao, countryDao, difficulty));
        try {
            solution = solutionFuture.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
        return solution;
    }

    public LiveData<Integer> getDifficultyLevel(int id) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        MutableLiveData<Integer> level = new MutableLiveData<>(1);
        Future<LiveData<Integer>> solutionFuture = executorService.submit(new Callable<LiveData<Integer>>() {
            @Override
            public LiveData<Integer> call() throws Exception {
                LiveData<Integer> liveDataLevel = quizDao.getQuizLevel(id);
                return liveDataLevel;
            }
        });
        try {
            level = (MutableLiveData<Integer>) solutionFuture.get();
            Log.e("level is ",level+"");
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
        return level;
    }


    public void saveAnswer(long countryId, boolean answer,long selectedAnswer) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<Long> qq = questionQuizCrossRefDao.selectQQ(quizId);
                questionDao.updateQuizAnswer(qq, countryId, answer,selectedAnswer);
            }
        });
    }

    public void endQuiz(int score, int continentId, int currentLvl) {
        Long endDate = Calendar.getInstance().getTimeInMillis();
        int nextLvl = 1;
        if (score == NUMBER_OF_QUESTIONS) {
            Log.e("next level is ", currentLvl + " syn 1");
            nextLvl = getNextDifficultyLevel(currentLvl);
            Log.e("next level is ", nextLvl + " ");
        } else {
            nextLvl = currentLvl;
        }
        int finalNextLvl = nextLvl;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                quizDao.updateQuizEndDateAndScoreAndDifficultyLevel(quizId, endDate, score, finalNextLvl);
            }
        });
    }

    public int getNextDifficultyLevel(int level) {
        int nextLevel = 1;
        if (level == 1) {
            nextLevel = 2;
        }
        if (level == 2) {
            nextLevel = 3;
        }
        if (level == 3) {
            nextLevel = 4;
        }
        if(nextLevel ==4){
            nextLevel=4;
        }
        return nextLevel;
    }
}



