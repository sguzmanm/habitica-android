package com.habitrpg.android.habitica.ui.activities

import com.habitrpg.android.habitica.databinding.ActivityTaskFormBinding


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.forEachIndexed
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.TagRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.extensions.OnChangeTextWatcher
import com.habitrpg.android.habitica.extensions.addCancelButton
import com.habitrpg.android.habitica.extensions.dpToPx
import com.habitrpg.android.habitica.extensions.getThemeColor
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.helpers.TaskAlarmManager
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.models.tasks.HabitResetOption
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.tasks.form.*
import io.reactivex.functions.Consumer
import io.realm.RealmList
import java.util.*
import javax.inject.Inject


class TaskFormActivity : BaseActivity() {
    
    private var binding:ActivityTaskFormBinding?=null

    private var userScrolled: Boolean = false
    private var isSaving: Boolean = false
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var taskRepository: TaskRepository
    @Inject
    lateinit var tagRepository: TagRepository
    @Inject
    lateinit var taskAlarmManager: TaskAlarmManager

    
    /*
    private val binding!!.toolbar: binding!!.toolbar by bindView(R.id.binding!!.toolbar)
    private val binding!!.scrollView: NestedScrollView by bindView(R.id.scroll_view)
    private val binding!!.upperTextWrapper: LinearLayout by bindView(R.id.upper_text_wrapper)
    private val binding!!.textEditText: EditText by bindView(R.id.text_edit_text)
    private val binding!!.notesEditText: EditText by bindView(R.id.notes_edit_text)
    private val binding!!.checklistTitle: TextView by bindView(R.id.checklist_title)
    private val binding!!.checklistContainer: ChecklistContainer by bindView(R.id.checklist_container)
    private val binding!!.habitResetStreakTitle: TextView by bindView(R.id.habit_reset_streak_title)
    private val binding!!.habitResetStreakButtons: HabitResetStreakButtons by bindView(R.id.habit_reset_streak_buttons)
    private val binding!!.schedulingTitle: TextView by bindView(R.id.scheduling_title)
    private val binding!!.adjustStreakWrapper: ViewGroup by bindView(R.id.adjust_streak_wrapper)
    private val binding!!.adjustStreakTitle: TextView by bindView(R.id.adjust_streak_title)
    private val binding!!.habitAdjustPositiveStreak: EditText by bindView(R.id.habit_adjust_positive_streak)
    private val binding!!.habitAdjustNegativeStreak: EditText by bindView(R.id.habit_adjust_negative_streak)
    private val binding!!.remindersTitle: TextView by bindView(R.id.reminders_title)
    private val binding!!.remindersContainer: ReminderContainer by bindView(R.id.reminders_container)

    private val binding!!.taskDifficultyTitle: TextView by bindView(R.id.task_difficulty_title)
    private val binding!!.taskDifficultyButtons: TaskDifficultyButtons by bindView(R.id.task_difficulty_buttons)

    private val binding!!.statWrapper: ViewGroup by bindView(R.id.stat_wrapper)
    private val binding!!.statStrengthButton: TextView by bindView(R.id.stat_strength_button)
    private val binding!!.statIntelligenceButton: TextView by bindView(R.id.stat_intelligence_button)
    private val binding!!.statConstitutionButton: TextView by bindView(R.id.stat_constitution_button)
    private val binding!!.statPerceptionButton: TextView by bindView(R.id.stat_perception_button)

    private val binding!!.rewardValueTitle: TextView by bindView(R.id.reward_value_title)
    private val binding!!.rewardValue: RewardValueFormView by bindView(R.id.reward_value)

    private val binding!!.tagsTitle: TextView by bindView(R.id.tags_title)
    private val binding!!.tagsWrapper: LinearLayout by bindView(R.id.tags_wrapper)

*/

    private val schedulingControls: TaskSchedulingControls by bindView(R.id.scheduling_controls)


    private var isCreating = true
    private var isChallengeTask = false
    private var usesTaskAttributeStats = false
    private var task: Task? = null
    private var taskType: String = ""
    private var tags = listOf<Tag>()
    private var preselectedTags: ArrayList<String>? = null
    private var hasPreselectedTags = false
    private var selectedStat = Stats.STRENGTH
    set(value) {
        field = value
        setSelectedAttribute(value)
    }

    private var canSave: Boolean = false

    private var tintColor: Int = 0
    set(value) {
        field = value
        binding!!.upperTextWrapper.setBackgroundColor(value)
        binding!!.taskDifficultyButtons.tintColor = value
        binding!!.habitScoringButtons.tintColor = value
        binding!!.habitResetStreakButtons.tintColor = value
        schedulingControls.tintColor = value
        supportActionBar?.setBackgroundDrawable(ColorDrawable(value))
        updateTagViewsColors()
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_task_form
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val bundle = intent.extras ?: return

        val taskId = bundle.getString(TASK_ID_KEY)
        forcedTheme = if (taskId != null) {
            val taskValue = bundle.getDouble(TASK_VALUE_KEY)
            when {
                taskValue < -20 -> "maroon"
                taskValue < -10 -> "red"
                taskValue < -1 -> "orange"
                taskValue < 1 -> "yellow"
                taskValue < 5 -> "green"
                taskValue < 10 -> "teal"
                else -> "blue"
            }
        } else {
            "purple"
        }

        super.onCreate(savedInstanceState)

        binding= ActivityTaskFormBinding.inflate(layoutInflater)

        setSupportActionBar(binding!!.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        tintColor = getThemeColor(R.attr.taskFormTint)

        isChallengeTask = bundle.getBoolean(IS_CHALLENGE_TASK, false)

        taskType = bundle.getString(TASK_TYPE_KEY) ?: Task.TYPE_HABIT
        preselectedTags = bundle.getStringArrayList(SELECTED_TAGS_KEY)

        compositeSubscription.add(tagRepository.getTags()
                .map { tagRepository.getUnmanagedCopy(it) }
                .subscribe(Consumer {
                    tags = it
                    setTagViews()
                }, RxErrorHandler.handleEmptyError()))
        compositeSubscription.add(userRepository.getUser().subscribe(Consumer {
            usesTaskAttributeStats = it.preferences?.allocationMode == "taskbased"
            configureForm()
        }, RxErrorHandler.handleEmptyError()))


        binding!!.textEditText.addTextChangedListener(OnChangeTextWatcher { _, _, _, _ ->
            checkCanSave()
        })
        binding!!.statStrengthButton.setOnClickListener { selectedStat = Stats.STRENGTH }
        binding!!.statIntelligenceButton.setOnClickListener { selectedStat = Stats.INTELLIGENCE }
        binding!!.statConstitutionButton.setOnClickListener { selectedStat = Stats.CONSTITUTION }
        binding!!.statPerceptionButton.setOnClickListener { selectedStat = Stats.PERCEPTION }
        binding!!.scrollView.setOnTouchListener { view, event ->
            userScrolled = view == binding!!.scrollView && (event.action == MotionEvent.ACTION_SCROLL || event.action == MotionEvent.ACTION_MOVE)
            return@setOnTouchListener false
        }
        binding!!.scrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, _: Int, _: Int, _: Int ->
            if (userScrolled) {
                dismissKeyboard()
            }
        }

        title = ""
        when {
            taskId != null -> {
                isCreating = false
                compositeSubscription.add(taskRepository.getUnmanagedTask(taskId).firstElement().subscribe(Consumer {
                    task = it
                    //tintColor = ContextCompat.getColor(this, it.mediumTaskColor)
                    fillForm(it)
                }, RxErrorHandler.handleEmptyError()))
            }
            bundle.containsKey(PARCELABLE_TASK) -> {
                isCreating = false
                task = bundle.getParcelable(PARCELABLE_TASK)
                task?.let { fillForm(it) }
            }
            else -> title = getString(R.string.create_task, getString(when (taskType) {
                Task.TYPE_DAILY -> R.string.daily
                Task.TYPE_TODO -> R.string.todo
                Task.TYPE_REWARD -> R.string.reward
                else -> R.string.habit
            }))
        }
        configureForm()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (isCreating) {
            menuInflater.inflate(R.menu.menu_task_create, menu)
        } else {
            menuInflater.inflate(R.menu.menu_task_edit, menu)
        }
        menu.findItem(R.id.action_save).isEnabled = canSave
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_save -> saveTask()
            R.id.action_delete -> deleteTask()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkCanSave() {
        val newCanSave = binding!!.textEditText.text!!.isNotBlank()
        if (newCanSave != canSave) {
            invalidateOptionsMenu()
        }
        canSave = newCanSave
    }

    private fun configureForm() {
        val habitViewsVisibility = if (taskType == Task.TYPE_HABIT) View.VISIBLE else View.GONE
        binding!!.habitScoringButtons.visibility = habitViewsVisibility
        binding!!.habitResetStreakTitle.visibility = habitViewsVisibility
        binding!!.habitResetStreakButtons.visibility = habitViewsVisibility
        binding!!.habitAdjustNegativeStreak.visibility = habitViewsVisibility

        val habitDailyVisibility = if (taskType == Task.TYPE_DAILY || taskType == Task.TYPE_HABIT) View.VISIBLE else View.GONE
        binding!!.adjustStreakTitle.visibility = habitDailyVisibility
        binding!!.adjustStreakWrapper.visibility = habitDailyVisibility
        if (taskType == Task.TYPE_HABIT) {
            binding!!.habitAdjustPositiveStreak.hint = getString(R.string.positive_habit_form)
        } else {
            binding!!.habitAdjustPositiveStreak.hint = getString(R.string.streak)
        }

        val todoDailyViewsVisibility = if (taskType == Task.TYPE_DAILY || taskType == Task.TYPE_TODO) View.VISIBLE else View.GONE

        binding!!.checklistTitle.visibility = if (isChallengeTask) View.GONE else todoDailyViewsVisibility
        binding!!.checklistContainer.visibility = if (isChallengeTask) View.GONE else todoDailyViewsVisibility

        binding!!.remindersTitle.visibility = if (isChallengeTask) View.GONE else todoDailyViewsVisibility
        binding!!.remindersContainer.visibility = if (isChallengeTask) View.GONE else todoDailyViewsVisibility
        binding!!.remindersContainer.taskType = taskType

        binding!!.schedulingTitle.visibility = todoDailyViewsVisibility
        binding!!.schedulingControls.visibility = todoDailyViewsVisibility
        binding!!.schedulingControls.taskType = taskType

        val rewardHideViews = if (taskType == Task.TYPE_REWARD) View.GONE else View.VISIBLE
        binding!!.taskDifficultyTitle.visibility = rewardHideViews
        binding!!.taskDifficultyButtons.visibility = rewardHideViews

        val rewardViewsVisibility = if (taskType == Task.TYPE_REWARD) View.VISIBLE else View.GONE
        binding!!.rewardValueTitle.visibility = rewardViewsVisibility
        binding!!.rewardValue.visibility = rewardViewsVisibility

        binding!!.tagsTitle.visibility = if (isChallengeTask) View.GONE else View.VISIBLE
        binding!!.tagsWrapper.visibility = if (isChallengeTask) View.GONE else View.VISIBLE

        binding!!.statWrapper.visibility = if (usesTaskAttributeStats) View.VISIBLE else View.GONE
        if (isCreating) {
            binding!!.adjustStreakTitle.visibility = View.GONE
            binding!!.adjustStreakWrapper.visibility = View.GONE
        }
    }

    private fun setTagViews() {
        binding!!.tagsWrapper.removeAllViews()
        val padding = 20.dpToPx(this)
        for (tag in tags) {
            val view = CheckBox(this)
            view.setPadding(padding, view.paddingTop, view.paddingRight, view.paddingBottom)
            view.text = tag.name
            if (preselectedTags?.contains(tag.id) == true) {
                view.isChecked = true
            }
            binding!!.tagsWrapper.addView(view)
        }
        setAllTagSelections()
        updateTagViewsColors()
    }

    private fun setAllTagSelections() {
        if (hasPreselectedTags) {
            tags.forEachIndexed { index, tag ->
                val view = binding!!.tagsWrapper.getChildAt(index) as? CheckBox
                view?.isChecked = task?.tags?.find { it.id == tag.id } != null
            }
        } else {
            hasPreselectedTags = true
        }
    }

    private fun fillForm(task: Task) {
        if (!task.isValid) {
            return
        }
        canSave = true
        binding!!.textEditText.setText(task.text)
        binding!!.notesEditText.setText(task.notes)
        binding!!.taskDifficultyButtons.selectedDifficulty = task.priority
        when (taskType) {
            Task.TYPE_HABIT -> {
                binding!!.habitScoringButtons.isPositive = task.up ?: false
                binding!!.habitScoringButtons.isNegative = task.down ?: false
                task.frequency?.let {
                    if (it.isNotBlank()) {
                        binding!!.habitResetStreakButtons.selectedResetOption = HabitResetOption.valueOf(it.toUpperCase(Locale.US))
                    }
                }
                binding!!.habitAdjustPositiveStreak.setText((task.counterUp ?: 0).toString())
                binding!!.habitAdjustNegativeStreak.setText((task.counterDown ?: 0).toString())
                binding!!.habitAdjustPositiveStreak.visibility = if (task.up == true) View.VISIBLE else View.GONE
                binding!!.habitAdjustNegativeStreak.visibility = if (task.down == true) View.VISIBLE else View.GONE
                if (task.up != true && task.down != true) {
                    binding!!.adjustStreakTitle.visibility = View.GONE
                    binding!!.adjustStreakWrapper.visibility = View.GONE
                }
            }
            Task.TYPE_DAILY -> {
                binding!!.schedulingControls.startDate = task.startDate ?: Date()
                binding!!.schedulingControls.everyX = task.everyX ?: 1
                task.repeat?.let { binding!!.schedulingControls.weeklyRepeat = it }
                binding!!.schedulingControls.daysOfMonth = task.getDaysOfMonth()
                binding!!.schedulingControls.weeksOfMonth = task.getWeeksOfMonth()
                binding!!.habitAdjustPositiveStreak.setText((task.streak ?: 0).toString())
                binding!!.schedulingControls.frequency = task.frequency ?: Task.FREQUENCY_DAILY
            }
            Task.TYPE_TODO -> binding!!.schedulingControls.dueDate = task.dueDate
            Task.TYPE_REWARD -> binding!!.rewardValue.value = task.value
        }
        if (taskType == Task.TYPE_DAILY || taskType == Task.TYPE_TODO) {
            task.checklist?.let { binding!!.checklistContainer.checklistItems = it }
            binding!!.remindersContainer.taskType = taskType
            task.reminders?.let { binding!!.remindersContainer.reminders = it }
        }
        task.attribute?.let { selectedStat = it }
        setAllTagSelections()
    }

    private fun setSelectedAttribute(attributeName: String) {
        if (!usesTaskAttributeStats) return
        configureStatsButton(binding!!.statStrengthButton, attributeName == Stats.STRENGTH )
        configureStatsButton(binding!!.statIntelligenceButton, attributeName == Stats.INTELLIGENCE )
        configureStatsButton(binding!!.statConstitutionButton, attributeName == Stats.CONSTITUTION )
        configureStatsButton(binding!!.statPerceptionButton, attributeName == Stats.PERCEPTION )
    }

    private fun configureStatsButton(button: TextView, isSelected: Boolean) {
        button.background.setTint(if (isSelected) tintColor else ContextCompat.getColor(this, R.color.taskform_gray))
        val textColorID = if (isSelected) R.color.white else R.color.gray_100
        button.setTextColor(ContextCompat.getColor(this, textColorID))
    }

    private fun updateTagViewsColors() {
        binding!!.tagsWrapper.children.forEach { view ->
            val tagView = view as? AppCompatCheckBox
            val colorStateList = ColorStateList(
                    arrayOf(intArrayOf(-android.R.attr.state_checked), // unchecked
                            intArrayOf(android.R.attr.state_checked)  // checked
                    ),
                    intArrayOf(ContextCompat.getColor(this, R.color.gray_400), tintColor)
            )
            tagView?.buttonTintList = colorStateList
        }
    }

    private fun saveTask() {
        if (isSaving) {
            return
        }
        isSaving = true
        var thisTask = task
        if (thisTask == null) {
            thisTask = Task()
            thisTask.type = taskType
            thisTask.dateCreated = Date()
        } else {
            if (!thisTask.isValid) return
        }
        thisTask.text = binding!!.textEditText.text.toString()
        thisTask.notes = binding!!.notesEditText.text.toString()
        thisTask.priority = binding!!.taskDifficultyButtons.selectedDifficulty
        if (usesTaskAttributeStats) {
            thisTask.attribute = selectedStat
        }
        if (taskType == Task.TYPE_HABIT) {
            thisTask.up = binding!!.habitScoringButtons.isPositive
            thisTask.down = binding!!.habitScoringButtons.isNegative
            thisTask.frequency = binding!!.habitResetStreakButtons.selectedResetOption.value
            if (binding!!.habitAdjustPositiveStreak.text!!.isNotEmpty()) thisTask.counterUp = binding!!.habitAdjustPositiveStreak.text.toString().toIntCatchOverflow()
            if (binding!!.habitAdjustNegativeStreak.text!!.isNotEmpty()) thisTask.counterDown = binding!!.habitAdjustNegativeStreak.text.toString().toIntCatchOverflow()
        } else if (taskType == Task.TYPE_DAILY) {
            thisTask.startDate = binding!!.schedulingControls.startDate
            thisTask.everyX = binding!!.schedulingControls.everyX
            thisTask.frequency = binding!!.schedulingControls.frequency
            thisTask.repeat = binding!!.schedulingControls.weeklyRepeat
            thisTask.setDaysOfMonth(binding!!.schedulingControls.daysOfMonth)
            thisTask.setWeeksOfMonth(binding!!.schedulingControls.weeksOfMonth)
            if (binding!!.habitAdjustPositiveStreak.text!!.isNotEmpty()) thisTask.streak = binding!!.habitAdjustPositiveStreak.text.toString().toIntCatchOverflow()
        } else if (taskType == Task.TYPE_TODO) {
            thisTask.dueDate = binding!!.schedulingControls.dueDate
        } else if (taskType == Task.TYPE_REWARD) {
            thisTask.value = binding!!.rewardValue.value
        }

        val resultIntent = Intent()
        resultIntent.putExtra(TASK_TYPE_KEY, taskType)
        if (!isChallengeTask) {
            if (taskType == Task.TYPE_DAILY || taskType == Task.TYPE_TODO) {
                thisTask.checklist = binding!!.checklistContainer.checklistItems
                thisTask.reminders = binding!!.remindersContainer.reminders
            }
            thisTask.tags = RealmList()
            binding!!.tagsWrapper.forEachIndexed { index, view ->
                val tagView = view as? CheckBox
                if (tagView?.isChecked == true) {
                    thisTask.tags?.add(tags[index])
                }
            }

            if (isCreating) {
                taskRepository.createTaskInBackground(thisTask)
            } else {
                taskRepository.updateTaskInBackground(thisTask)
            }

            if (thisTask.type == Task.TYPE_DAILY || thisTask.type == Task.TYPE_TODO) {
                taskAlarmManager.scheduleAlarmsForTask(thisTask)
            }
        } else {
                resultIntent.putExtra(PARCELABLE_TASK, thisTask)
        }

        val mainHandler = Handler(this.mainLooper)
        mainHandler.postDelayed({
            setResult(Activity.RESULT_OK, resultIntent)
            dismissKeyboard()
            finish()
        }, 500)
    }

    private fun deleteTask() {
        val alert = HabiticaAlertDialog(this)
        alert.setTitle(R.string.are_you_sure)
        alert.addButton(R.string.delete_task, true) { _, _ ->
            if (task?.isValid != true) return@addButton
            task?.id?.let { taskRepository.deleteTask(it).subscribe(Consumer {  }, RxErrorHandler.handleEmptyError()) }
            dismissKeyboard()
            finish()
        }
        alert.addCancelButton()
        alert.show()
    }

    private fun dismissKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        val currentFocus = currentFocus
        if (currentFocus != null && !binding!!.habitAdjustPositiveStreak.isFocused && !binding!!.habitAdjustNegativeStreak.isFocused) {
            imm?.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }

    companion object {
        val SELECTED_TAGS_KEY = "selectedTags"
        const val TASK_ID_KEY = "taskId"
        const val TASK_VALUE_KEY = "taskValue"
        const val USER_ID_KEY = "userId"
        const val TASK_TYPE_KEY = "type"
        const val IS_CHALLENGE_TASK = "isChallengeTask"

        const val PARCELABLE_TASK = "parcelable_task"

        // in order to disable the event handler in MainActivity
        const val SET_IGNORE_FLAG = "ignoreFlag"
    }
}

private fun String.toIntCatchOverflow(): Int? {
    return try {
        toInt()
    } catch (e: NumberFormatException) {
        0
    }
}
