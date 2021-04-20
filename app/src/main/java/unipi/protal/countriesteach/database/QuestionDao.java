package unipi.protal.countriesteach.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;
import unipi.protal.countriesteach.entities.Question;

@Dao
public interface QuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertQuestion(Question question);

    @Insert
    List<Long> insertAllQuestions(List<Question> questions);

    @Query("SELECT * FROM question WHERE questionId=:id")
    LiveData<Question> findQuestionById(long id);

}
