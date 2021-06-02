package unipi.protal.countriesteach.screens.game;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

import unipi.protal.countriesteach.R;
import unipi.protal.countriesteach.database.CountryContentValues;
import unipi.protal.countriesteach.databinding.GameFragmentBinding;
import unipi.protal.countriesteach.entities.Country;
import unipi.protal.countriesteach.entities.Question;
import unipi.protal.countriesteach.genetic.exceptions.GeneticAlgorithmException;
import unipi.protal.countriesteach.genetic.service.GeneticAlgorithmService;
import unipi.protal.countriesteach.utils.NumberUtils;


import static unipi.protal.countriesteach.database.CountryContentValues.AFRICA;
import static unipi.protal.countriesteach.database.CountryContentValues.AMERICA;
import static unipi.protal.countriesteach.database.CountryContentValues.ASIA;
import static unipi.protal.countriesteach.database.CountryContentValues.EUROPE;
import static unipi.protal.countriesteach.database.CountryContentValues.NUMBER_OF_AFRICAN_COUNTRIES;
import static unipi.protal.countriesteach.database.CountryContentValues.NUMBER_OF_ALL_COUNTRIES;
import static unipi.protal.countriesteach.database.CountryContentValues.NUMBER_OF_AMERICAN_COUNTRIES;
import static unipi.protal.countriesteach.database.CountryContentValues.NUMBER_OF_ASIAN_COUNTRIES;
import static unipi.protal.countriesteach.database.CountryContentValues.NUMBER_OF_EUROPEAN_COUNTRIES;
import static unipi.protal.countriesteach.database.CountryContentValues.NUMBER_OF_OCEANIAN_COUNTRIES;
import static unipi.protal.countriesteach.database.CountryContentValues.OCEANIA;
import static unipi.protal.countriesteach.database.CountryContentValues.WORLD;

public class GameFragment extends Fragment implements View.OnClickListener {
    private GameViewModel gameViewModel;
    private GameViewModelFactory gameViewModelFactory;
    private GameFragmentBinding binding;
    private Country country;
    private int countryIndex, firstAnswerIndex, secondAnswerIndex, thirdAnswerIndex, fourthAnswerIndex;
    private int continentId, numberOfQuestion;
    private NavController navController;
    private List<Country> allCountries;
    private List<Question> quizQuestions;
    public static final int NUMBER_OF_QUESTIONS = 10;
    private Long quizId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.game_fragment, container, false);
        // Specify the current activity as the lifecycle owner of the binding. This is used so that
        // the binding can observe LiveData updates
        binding.setLifecycleOwner(this);
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        Resources resources = this.getContext().getResources();
        continentId = GameFragmentArgs.fromBundle(getArguments()).getContinentId();
        Drawable unwrappedDrawable = AppCompatResources.getDrawable(getContext(), R.drawable.button_next);
        if (continentId == EUROPE) {
            binding.firstAnswerRadioButton.setBackgroundResource(R.drawable.button_europe);
            binding.secondAnswerRadioButton.setBackgroundResource(R.drawable.button_europe);
            binding.thirdAnswerRadioButton.setBackgroundResource(R.drawable.button_europe);
            binding.fourthAnswerRadioButton.setBackgroundResource(R.drawable.button_europe);
            unwrappedDrawable.setTint(resources.getColor(R.color.color_europe_dark));
        } else if (continentId == CountryContentValues.AMERICA) {
            binding.firstAnswerRadioButton.setBackgroundResource(R.drawable.button_america);
            binding.secondAnswerRadioButton.setBackgroundResource(R.drawable.button_america);
            binding.thirdAnswerRadioButton.setBackgroundResource(R.drawable.button_america);
            binding.fourthAnswerRadioButton.setBackgroundResource(R.drawable.button_america);
            unwrappedDrawable.setTint(resources.getColor(R.color.color_america_dark));
        } else if (continentId == CountryContentValues.ASIA) {
            binding.firstAnswerRadioButton.setBackgroundResource(R.drawable.button_asia);
            binding.secondAnswerRadioButton.setBackgroundResource(R.drawable.button_asia);
            binding.thirdAnswerRadioButton.setBackgroundResource(R.drawable.button_asia);
            binding.fourthAnswerRadioButton.setBackgroundResource(R.drawable.button_asia);
            unwrappedDrawable.setTint(resources.getColor(R.color.color_asia_dark));
        } else if (continentId == CountryContentValues.AFRICA) {
            binding.firstAnswerRadioButton.setBackgroundResource(R.drawable.button_africa);
            binding.secondAnswerRadioButton.setBackgroundResource(R.drawable.button_africa);
            binding.thirdAnswerRadioButton.setBackgroundResource(R.drawable.button_africa);
            binding.fourthAnswerRadioButton.setBackgroundResource(R.drawable.button_africa);
            unwrappedDrawable.setTint(resources.getColor(R.color.color_africa_dark));
        } else if (continentId == CountryContentValues.OCEANIA) {
            binding.firstAnswerRadioButton.setBackgroundResource(R.drawable.button_oceania);
            binding.secondAnswerRadioButton.setBackgroundResource(R.drawable.button_oceania);
            binding.thirdAnswerRadioButton.setBackgroundResource(R.drawable.button_oceania);
            binding.fourthAnswerRadioButton.setBackgroundResource(R.drawable.button_oceania);
            unwrappedDrawable.setTint(resources.getColor(R.color.color_oceania_dark));
        } else if (continentId == CountryContentValues.WORLD) {
            binding.firstAnswerRadioButton.setBackgroundResource(R.drawable.button_all_continents);
            binding.secondAnswerRadioButton.setBackgroundResource(R.drawable.button_all_continents);
            binding.thirdAnswerRadioButton.setBackgroundResource(R.drawable.button_all_continents);
            binding.fourthAnswerRadioButton.setBackgroundResource(R.drawable.button_all_continents);
            unwrappedDrawable.setTint(resources.getColor(R.color.color_wolrd_dark));
        }
        binding.skipButton.setBackground(unwrappedDrawable);
        gameViewModelFactory = new GameViewModelFactory(getActivity().getApplication(), continentId);
        gameViewModel = new ViewModelProvider(this, gameViewModelFactory).get(GameViewModel.class);
        gameViewModel.getAllCountries().observe(getViewLifecycleOwner(), countries -> {
            allCountries = new ArrayList<>(gameViewModel.getAllCountries().getValue());
            if (!(allCountries.size() < NUMBER_OF_ALL_COUNTRIES) && gameViewModel.countryIndex.getValue() != null) {
                binding.flagImage.setImageResource(resources.getIdentifier("ic_" + gameViewModel.countryIndex.getValue(), "drawable",
                        this.getContext().getPackageName()));
                binding.firstAnswerRadioButton.setText(gameViewModel.getAllCountries().getValue().get(gameViewModel.firstAnswerIndex.getValue() - 1).getCountryName());
                binding.secondAnswerRadioButton.setText(gameViewModel.getAllCountries().getValue().get(gameViewModel.secondAnswerIndex.getValue() - 1).getCountryName());
                binding.thirdAnswerRadioButton.setText(gameViewModel.getAllCountries().getValue().get(gameViewModel.thirdAnswerIndex.getValue() - 1).getCountryName());
                binding.fourthAnswerRadioButton.setText(gameViewModel.getAllCountries().getValue().get(gameViewModel.fourthAnswerIndex.getValue() - 1).getCountryName());
                binding.firstAnswerRadioButton.setOnClickListener(this::onClick);
                binding.secondAnswerRadioButton.setOnClickListener(this::onClick);
                binding.thirdAnswerRadioButton.setOnClickListener(this::onClick);
                binding.fourthAnswerRadioButton.setOnClickListener(this::onClick);
            }
        });
        gameViewModel.getQuizQuestions().observe(getViewLifecycleOwner(), questions -> {
            quizQuestions = new ArrayList(gameViewModel.getQuizQuestions().getValue());
            binding.questionText.setText(gameViewModel.numberOfQuestion.getValue() + getString(R.string.number_of_question));
        });
        gameViewModel.numberOfQuestion.observe(getViewLifecycleOwner(), integer -> {
            numberOfQuestion = gameViewModel.numberOfQuestion.getValue();
            if (numberOfQuestion > NUMBER_OF_QUESTIONS) {
                gameViewModel.endQuiz();
                navController.navigate(GameFragmentDirections.actionGameFragmentToGameEnd().setQuizId(gameViewModel.getQuizId().getValue()));
            }
        });
        gameViewModel._quizId.observe(getViewLifecycleOwner(), lng -> {
            quizId = gameViewModel._quizId.getValue();
        });
        gameViewModel.countryIndex.observe(getViewLifecycleOwner(), integer -> {
            countryIndex = gameViewModel.countryIndex.getValue();
        });

        gameViewModel.firstAnswerIndex.observe(getViewLifecycleOwner(), integer -> {
            firstAnswerIndex = gameViewModel.firstAnswerIndex.getValue();
        });
        gameViewModel.secondAnswerIndex.observe(getViewLifecycleOwner(), integer -> {
            secondAnswerIndex = gameViewModel.secondAnswerIndex.getValue();
        });
        gameViewModel.thirdAnswerIndex.observe(getViewLifecycleOwner(), integer -> {
            thirdAnswerIndex = gameViewModel.thirdAnswerIndex.getValue();
        });
        gameViewModel.fourthAnswerIndex.observe(getViewLifecycleOwner(), integer -> {
            fourthAnswerIndex = gameViewModel.fourthAnswerIndex.getValue();
        });

        binding.skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameViewModel.saveAnswer(gameViewModel.countryIndex.getValue(), false);
                nextQuestion();
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onClick(View v) {
        if (v == binding.firstAnswerRadioButton) {
            if (gameViewModel.getAllCountries().getValue().get(firstAnswerIndex).getCountryId() == gameViewModel.getAllCountries().getValue().get(countryIndex).getCountryId()) {
                Toast.makeText(getContext(), "Correct answer", Toast.LENGTH_SHORT).show();
                gameViewModel.saveAnswer(gameViewModel.countryIndex.getValue(), true);
            } else {
                Toast.makeText(getContext(), "Wrong answer", Toast.LENGTH_SHORT).show();
                gameViewModel.saveAnswer(gameViewModel.countryIndex.getValue(), false);
            }
        } else if (v == binding.secondAnswerRadioButton) {
            if (gameViewModel.getAllCountries().getValue().get(secondAnswerIndex).getCountryId() == gameViewModel.getAllCountries().getValue().get(countryIndex).getCountryId()) {
                Toast.makeText(getContext(), "Correct answer", Toast.LENGTH_SHORT).show();
                gameViewModel.saveAnswer(gameViewModel.countryIndex.getValue(), true);
            } else {
                Toast.makeText(getContext(), "Wrong answer", Toast.LENGTH_SHORT).show();
                gameViewModel.saveAnswer(gameViewModel.countryIndex.getValue(), false);
            }
        } else if (v == binding.thirdAnswerRadioButton) {
            if (gameViewModel.getAllCountries().getValue().get(thirdAnswerIndex).getCountryId() == gameViewModel.getAllCountries().getValue().get(countryIndex).getCountryId()) {
                Toast.makeText(getContext(), "Correct answer", Toast.LENGTH_SHORT).show();
                gameViewModel.saveAnswer(gameViewModel.countryIndex.getValue(), true);
            } else {
                Toast.makeText(getContext(), "Wrong answer", Toast.LENGTH_SHORT).show();
                gameViewModel.saveAnswer(gameViewModel.countryIndex.getValue(), false);
            }
        } else if (v == binding.fourthAnswerRadioButton) {
            if (gameViewModel.getAllCountries().getValue().get(fourthAnswerIndex).getCountryId() == gameViewModel.getAllCountries().getValue().get(countryIndex).getCountryId()) {
                Toast.makeText(getContext(), "Correct answer", Toast.LENGTH_SHORT).show();
                gameViewModel.saveAnswer(gameViewModel.countryIndex.getValue(), true);
            } else {
                Toast.makeText(getContext(), "Wrong answer", Toast.LENGTH_SHORT).show();
                gameViewModel.saveAnswer(gameViewModel.countryIndex.getValue(), false);
            }
        }
        nextQuestion();
    }

    private void nextQuestion() {
        gameViewModel.numberOfQuestion.setValue(numberOfQuestion + 1);
        Resources resources = this.getContext().getResources();
        gameViewModel.nextCountryIndex();
        if(gameViewModel.numberOfQuestion.getValue()<=NUMBER_OF_QUESTIONS){
            binding.questionText.setText(gameViewModel.numberOfQuestion.getValue() + getString(R.string.number_of_question));
        }
        binding.flagImage.setImageResource(resources.getIdentifier("ic_" + gameViewModel.getAllCountries().getValue().get(countryIndex - 1).getCountryId(), "drawable",
                getContext().getPackageName()));
        binding.firstAnswerRadioButton.setText(gameViewModel.getAllCountries().getValue().get(firstAnswerIndex - 1).getCountryName());
        binding.secondAnswerRadioButton.setText(gameViewModel.getAllCountries().getValue().get(secondAnswerIndex - 1).getCountryName());
        binding.thirdAnswerRadioButton.setText(gameViewModel.getAllCountries().getValue().get(thirdAnswerIndex - 1).getCountryName());
        binding.fourthAnswerRadioButton.setText(gameViewModel.getAllCountries().getValue().get(fourthAnswerIndex - 1).getCountryName());
    }

    private int getnumberOfCountires(int id) {
        int num = 0;
        if (id == EUROPE) {
            num = NUMBER_OF_EUROPEAN_COUNTRIES;
        } else if (id == AMERICA) {
            num = NUMBER_OF_AMERICAN_COUNTRIES;
        } else if (id == AFRICA) {
            num = NUMBER_OF_AFRICAN_COUNTRIES;
        } else if (id == ASIA) {
            num = NUMBER_OF_ASIAN_COUNTRIES;
        } else if (id == OCEANIA) {
            num = NUMBER_OF_OCEANIAN_COUNTRIES;
        } else if (id == WORLD) {
            num = NUMBER_OF_ALL_COUNTRIES;
        }
        return num;
    }

}
