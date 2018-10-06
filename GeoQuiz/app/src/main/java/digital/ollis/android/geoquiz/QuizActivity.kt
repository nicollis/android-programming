package digital.ollis.android.geoquiz

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast

private const val TAG = "QuizActivity"
private const val KEY_INDEX = "index"
private const val ANSWER_INDEX = "answers"
private const val CHEATER_INDEX = "cheater"
private const val REQUEST_CODE_CHEAT = 0

class QuizActivity : AppCompatActivity() {

    private lateinit var trueButton: Button
    private lateinit var falseButton: Button
    private lateinit var nextButton: ImageButton
    private lateinit var previousButton: ImageButton
    private lateinit var questionTextView: TextView
    private lateinit var cheatButton: Button

    private var currentIndex = 0
    private var isCheater = false

    private val questionBank = listOf(
            Question(R.string.question_australia, true),
            Question(R.string.question_oceans, true),
            Question(R.string.question_mideast, false),
            Question(R.string.question_africa, false),
            Question(R.string.question_americas, true),
            Question(R.string.question_asia, true)
    )

    private var answerBank = ByteArray(questionBank.size)

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        trueButton = findViewById(R.id.true_button)
        falseButton = findViewById(R.id.false_button)
        nextButton = findViewById(R.id.next_button)
        previousButton = findViewById(R.id.previous_button)
        questionTextView = findViewById(R.id.question_text_view)
        cheatButton = findViewById(R.id.cheat_button)

        trueButton.setOnClickListener {
            checkAnswer(true)
        }

        falseButton.setOnClickListener {
            checkAnswer(false)
        }

        previousButton.setOnClickListener {
            currentIndex = if (currentIndex == 0) questionBank.size - 1 else currentIndex - 1
            isCheater = false
            updateQuestion()
        }

        nextButton.setOnClickListener {
            currentIndex = (currentIndex + 1) % questionBank.size
            isCheater = false
            updateQuestion()
        }

        questionTextView.setOnClickListener {
            currentIndex = (currentIndex + 1) % questionBank.size
            updateQuestion()
        }

        cheatButton.setOnClickListener { view ->
            val answerIsTrue = questionBank[currentIndex].answer
            val intent = CheatActivity.newIntent(this@QuizActivity, answerIsTrue)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val options = ActivityOptions
                        .makeClipRevealAnimation(view, 0, 0, view.width, view.height)
                startActivityForResult(intent, REQUEST_CODE_CHEAT, options.toBundle())
            } else {
                startActivityForResult(intent, REQUEST_CODE_CHEAT)
            }
        }

        currentIndex = savedInstanceState?.getInt(KEY_INDEX, 0) ?: 0
        answerBank = savedInstanceState?.getByteArray(ANSWER_INDEX) ?: ByteArray(questionBank.size)
        isCheater = savedInstanceState?.getBoolean(CHEATER_INDEX, false) ?: false

        updateQuestion()

    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putInt(KEY_INDEX, currentIndex)
        outState?.putByteArray(ANSWER_INDEX, answerBank)
        outState?.putBoolean(CHEATER_INDEX, isCheater)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK)
            return

        if (requestCode == REQUEST_CODE_CHEAT)
            isCheater = data?.getBooleanExtra(EXTRA_ANSWER_SHOWN, false) ?: false
    }

    private fun updateQuestion() {
        if (answerBank[currentIndex].compareTo(0) != -1) {
            trueButton.isClickable = true
            falseButton.isClickable = true
        } else {
            trueButton.isClickable = false
            falseButton.isClickable = false
        }

        val questionTextResId = questionBank[currentIndex].textResId
        questionTextView.setText(questionTextResId)
    }

    private fun checkAnswer(userAnswer: Boolean) {
        val messageResId = when {
            isCheater -> R.string.judgment_toast
            userAnswer == questionBank[currentIndex].answer -> R.string.correct_toast
            else -> R.string.incorrect_toast
        }

        answerBank[currentIndex] = if (userAnswer == questionBank[currentIndex].answer) 1 else -1

        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show()

        if (answerBank.indexOf(0) == -1) gradeQuiz()

        updateQuestion()
    }

    private fun gradeQuiz() {
        val score = answerBank.count { it.compareTo(1) == 0 }

        Toast.makeText(this, "$score out of ${answerBank.size} correct!", Toast.LENGTH_LONG).show()

        answerBank = ByteArray(questionBank.size)

        currentIndex = 0
    }
}
