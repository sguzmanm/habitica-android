package com.habitrpg.android.habitica.ui.viewHolders.tasks

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.HabitItemCardBinding
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.responses.TaskDirection
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.helpers.*
import com.habitrpg.android.habitica.ui.views.EllipsisTextView
import io.noties.markwon.utils.NoCopySpannableFactory
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

open class HabitViewHolder(private val cardBinding: HabitItemCardBinding?, private val scoreTaskFunc: ((Task, TaskDirection) -> Unit), private val openTaskFunc: ((Task) -> Unit)): RecyclerView.ViewHolder(cardBinding?.root!!),View.OnClickListener {
    var task: Task? = null
    var movingFromPosition: Int? = null
    var errorButtonClicked: Action? = null


    private var openTaskDisabled: Boolean = false
    private var taskActionsDisabled: Boolean = false
    private var notesExpanded = false


    // UI
    private val btnPlusWrapper: FrameLayout?
        get()=cardBinding?.btnPlusWrapper
    private val btnPlusIconView: ImageView?
        get()=cardBinding?.btnPlusIconView
    private val btnPlus: Button?
        get()=cardBinding?.btnPlus
    private val btnMinusWrapper: FrameLayout?
        get()=cardBinding?.btnMinusWrapper
    private val btnMinusIconView: ImageView?
        get()=cardBinding?.btnMinusIconView
    private val btnMinus: Button?
        get()=cardBinding?.btnMinus
    private val streakTextView: TextView?
        get()=cardBinding?.streakTextView
    private val titleTextView: EllipsisTextView?
        get()=cardBinding?.checkedTextView
    private val notesTextView: EllipsisTextView?
        get()=cardBinding?.notesTextView
    private var rightBorderView: View?=null

    private val specialTaskTextView: TextView?
        get()=cardBinding?.specialTaskText

    private val iconViewChallenge: ImageView?
        get()=cardBinding?.iconviewChallenge

    private val iconViewReminder: ImageView?
        get()=cardBinding?.iconviewReminder

    private val iconViewTag: ImageView?
        get()=cardBinding?.iconviewTag

    private val taskIconWrapper: LinearLayout?
        get()=cardBinding?.taskIconWrapper

    private val approvalRequiredTextView: TextView?
        get()=cardBinding?.approvalRequiredTextField

    private val expandNotesButton: Button?
        get()=cardBinding?.expandNotesButton

    private val syncingView: ProgressBar?
        get()=cardBinding?.syncingView

    private val errorIconView: ImageButton?
        get()=cardBinding?.errorIcon

    protected var taskGray: Int?=null


    init {
        cardBinding?.root?.setOnClickListener { onClick(it) }
        cardBinding?.root?.isClickable = true

        titleTextView?.setOnClickListener { onClick(it) }
        notesTextView?.setOnClickListener { onClick(it) }
        errorIconView?.setOnClickListener { errorButtonClicked?.run()}

        //Re enable when we find a way to only react when a link is tapped.
        //notesTextView.movementMethod = LinkMovementMethod.getInstance()
        //titleTextView.movementMethod = LinkMovementMethod.getInstance()

        expandNotesButton?.setOnClickListener { expandTask() }
        notesTextView?.addEllipsesListener(object : EllipsisTextView.EllipsisListener {
            override fun ellipsisStateChanged(ellipses: Boolean) {
                GlobalScope.launch(Dispatchers.Main.immediate) {
                    expandNotesButton?.visibility = if (ellipses || notesExpanded) View.VISIBLE else View.GONE
                }
            }
        })

        btnPlus?.setOnClickListener { onPlusButtonClicked() }
        btnMinus?.setOnClickListener { onMinusButtonClicked() }
    }

    //---------Old------------&&

    protected open val taskIconWrapperIsVisible: Boolean
        get() {
            var isVisible = false

            if (iconViewReminder?.visibility == View.VISIBLE) {
                isVisible = true
            }
            if (iconViewTag?.visibility == View.VISIBLE) {
                isVisible = true
            }
            if (iconViewChallenge?.visibility == View.VISIBLE) {
                isVisible = true
            }
            if (iconViewReminder?.visibility == View.VISIBLE) {
                isVisible = true
            }
            if (specialTaskTextView?.visibility == View.VISIBLE) {
                isVisible = true
            }

            // New function
            if (this.streakTextView?.visibility == View.VISIBLE) {
                isVisible = true
            }
            return isVisible
        }

    private fun expandTask() {
        notesExpanded = !notesExpanded
        if (notesExpanded) {
            notesTextView?.maxLines = 100
            expandNotesButton?.text = cardBinding?.root?.context?.getString(R.string.collapse_notes)
        } else {
            notesTextView?.maxLines = 3
            expandNotesButton?.text = cardBinding?.root?.context?.getString(R.string.expand_notes)
        }
    }

    fun bind(data: Task, position: Int) {
        this.task = data
        if (data.up == true) {
            this.btnPlusWrapper?.setBackgroundResource(data.lightTaskColor)
            if (data.lightTaskColor == R.color.yellow_100) {
                this.btnPlusIconView?.setImageResource(R.drawable.habit_plus_yellow)
            } else {
                this.btnPlusIconView?.setImageResource(R.drawable.habit_plus)
            }
            this.btnPlus?.visibility = View.VISIBLE
            this.btnPlus?.isClickable = true
        } else {
            this.btnPlusWrapper?.setBackgroundResource(R.color.habit_inactive_gray)
            this.btnPlusIconView?.setImageResource(R.drawable.habit_plus_disabled)
            this.btnPlus?.visibility = View.GONE
            this.btnPlus?.isClickable = false
        }

        if (data.down == true) {
            this.btnMinusWrapper?.setBackgroundResource(data.lightTaskColor)
            if (data.lightTaskColor == R.color.yellow_100) {
                this.btnMinusIconView?.setImageResource(R.drawable.habit_minus_yellow)
            } else {
                this.btnMinusIconView?.setImageResource(R.drawable.habit_minus)
            }
            this.btnMinus?.visibility = View.VISIBLE
            this.btnMinus?.isClickable = true
        } else {
            this.btnMinusWrapper?.setBackgroundResource(R.color.habit_inactive_gray)
            this.btnMinusIconView?.setImageResource(R.drawable.habit_minus_disabled)
            this.btnMinus?.visibility = View.GONE
            this.btnMinus?.isClickable = false
        }

        var streakString = ""
        if (data.counterUp != null && data.counterUp ?: 0 > 0 && data.counterDown != null && data.counterDown ?: 0 > 0) {
            streakString = streakString + "+" + data.counterUp.toString() + " | -" + data.counterDown?.toString()
        } else if (data.counterUp != null && data.counterUp ?: 0 > 0) {
            streakString = streakString + "+" + data.counterUp.toString()
        } else if (data.counterDown != null && data.counterDown ?: 0 > 0) {
            streakString = streakString + "-" + data.counterDown.toString()
        }
        if (streakString.isNotEmpty()) {
            streakTextView?.text = streakString
            streakTextView?.visibility = View.VISIBLE
        } else {
            streakTextView?.visibility = View.GONE
        }
        superBind(data, position)
    }

    @SuppressLint("CheckResult")
    fun superBind(data: Task, position: Int) {
        task = data
        itemView.setBackgroundResource(R.color.white)

        expandNotesButton?.visibility = View.GONE
        if (data.notes?.isNotEmpty() == true) {
            notesTextView?.visibility = View.VISIBLE
            //expandNotesButton.visibility = if (notesTextView.hadEllipses() || notesExpanded) View.VISIBLE else View.GONE
        } else {
            notesTextView?.visibility = View.GONE
        }

        if (canContainMarkdown()) {
            if (data.parsedText != null) {
                titleTextView?.setParsedMarkdown(data.parsedText)
            } else {
                titleTextView?.text = data.text
                titleTextView?.setSpannableFactory(NoCopySpannableFactory.getInstance());
                if (data.text.isNotEmpty()) {
                    Single.just(data.text)
                            .map { MarkdownParser.parseMarkdown(it) }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(Consumer{ parsedText ->
                                data.parsedText = parsedText
                                titleTextView?.setParsedMarkdown(parsedText)
                            }, RxErrorHandler.handleEmptyError())
                }
                if (data.parsedNotes != null) {
                    notesTextView?.setParsedMarkdown(data.parsedText)
                } else {
                    notesTextView?.text = data.notes
                    notesTextView?.setSpannableFactory(NoCopySpannableFactory.getInstance());
                    data.notes?.let {notes ->
                        if (notes.isEmpty()) {
                            return@let
                        }
                        Single.just(notes)
                                .map { MarkdownParser.parseMarkdown(it) }
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(Consumer { parsedNotes ->
                                    notesTextView?.text = parsedNotes
                                    notesTextView?.setParsedMarkdown(parsedNotes)
                                }, RxErrorHandler.handleEmptyError())
                    }
                }
            }
        } else {
            titleTextView?.text = data.text
            notesTextView?.text = data.notes
        }

        rightBorderView?.setBackgroundResource(data.lightTaskColor)
        iconViewReminder?.visibility = if (data.reminders?.size ?: 0 > 0) View.VISIBLE else View.GONE
        iconViewTag?.visibility = if (data.tags?.size ?: 0 > 0) View.VISIBLE else View.GONE

        iconViewChallenge?.visibility = View.GONE

        configureSpecialTaskTextView(data)

        taskIconWrapper?.visibility = if (taskIconWrapperIsVisible) View.VISIBLE else View.GONE

        if (data.isPendingApproval) {
            approvalRequiredTextView?.visibility = View.VISIBLE
        } else {
            approvalRequiredTextView?.visibility = View.GONE
        }

        syncingView?.visibility = if (task?.isSaving == true) View.VISIBLE else View.GONE
        errorIconView?.visibility = if (task?.hasErrored == true) View.VISIBLE else View.GONE
    }


    protected open fun configureSpecialTaskTextView(task: Task) {
        specialTaskTextView?.visibility = View.INVISIBLE
    }

    override fun onClick(v: View) {
        task?.let { openTaskFunc(it) }
    }

    open fun canContainMarkdown(): Boolean {
        return true
    }

    open fun setDisabled(openTaskDisabled: Boolean, taskActionsDisabled: Boolean) {
        this.openTaskDisabled = openTaskDisabled
        this.taskActionsDisabled = taskActionsDisabled

        this.btnPlus?.isEnabled = !taskActionsDisabled
        this.btnMinus?.isEnabled = !taskActionsDisabled
    }

    //------New-------//
    private fun onPlusButtonClicked() {
        task?.let { scoreTaskFunc.invoke(it, TaskDirection.UP) }
    }

    private fun onMinusButtonClicked() {
        task?.let { scoreTaskFunc.invoke(it, TaskDirection.DOWN) }
    }


}
