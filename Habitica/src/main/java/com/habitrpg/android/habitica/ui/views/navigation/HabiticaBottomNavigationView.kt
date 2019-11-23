package com.habitrpg.android.habitica.ui.views.navigation

import com.habitrpg.android.habitica.databinding.MainNavigationViewBinding


import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ActivityTaskFormBinding
import com.habitrpg.android.habitica.extensions.getThemeColor
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.helpers.bindView


class HabiticaBottomNavigationView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private var binding: MainNavigationViewBinding? = null

    var flipAddBehaviour = true
    private var isShowingSubmenu: Boolean = false
    var selectedPosition: Int
        get() {
            return when (activeTaskType) {
                Task.TYPE_DAILY -> 1
                Task.TYPE_REWARD -> 2
                Task.TYPE_TODO -> 3
                else -> 0
            }
        }
        set(value) {
            activeTaskType = when (value) {
                1 -> Task.TYPE_DAILY
                2 -> Task.TYPE_TODO
                3 -> Task.TYPE_REWARD
                else -> Task.TYPE_HABIT
            }
        }
    var onTabSelectedListener: ((String) -> Unit)? = null
    var onAddListener: ((String) -> Unit)? = null
    var activeTaskType: String = Task.TYPE_HABIT
        set(value) {
            field = value
            updateItemSelection()
            onTabSelectedListener?.invoke(value)
        }

    val barHeight: Int
        get() = binding!!.itemWrapper.measuredHeight

    /*
    private val cutoutBackgroundView: ImageView by bindView(R.id.cutout_background)
    private val habitsTab: BottomNavigationItem by bindView(R.id.tab_habits)
    private val dailiesTab: BottomNavigationItem by bindView(R.id.tab_dailies)
    private val todosTab: BottomNavigationItem by bindView(R.id.tab_todos)
    private val rewardsTab: BottomNavigationItem by bindView(R.id.tab_rewards)
    private val addButton: ImageButton by bindView(R.id.add)
    private val addButtonBackground: ViewGroup by bindView(R.id.add_wrapper)
    private val submenuWrapper: LinearLayout by bindView(R.id.submenu_wrapper)
    private val itemWrapper: ViewGroup by bindView(R.id.item_wrapper)
    */


    init {
        binding = DataBindingUtil.inflate(context.layoutInflater, R.layout.main_navigation_view, this, true)

        binding!!.tabHabits.setOnClickListener { activeTaskType = Task.TYPE_HABIT }
        binding!!.tabDailies.setOnClickListener { activeTaskType = Task.TYPE_DAILY }
        binding!!.tabTodos.setOnClickListener { activeTaskType = Task.TYPE_TODO }
        binding!!.tabRewards.setOnClickListener { activeTaskType = Task.TYPE_REWARD }
        binding!!.add.setOnClickListener {
            if (flipAddBehaviour) {
                if (isShowingSubmenu) {
                    hideSubmenu()
                } else {
                    onAddListener?.invoke(activeTaskType)
                }
            } else {
                showSubmenu()
            }
            animateButtonTap()
        }
        binding!!.add.setOnLongClickListener {
            if (flipAddBehaviour) {
                showSubmenu()
            } else {
                onAddListener?.invoke(activeTaskType)
            }
            animateButtonTap()
            true
        }
        /*
        binding!!.add.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val animX = ObjectAnimator.ofFloat(binding!!.add, "scaleX", 1f, 1.1f)
                animX.duration = 100
                animX.interpolator = LinearInterpolator()
                animX.start()
                val animY = ObjectAnimator.ofFloat(binding!!.add, "scaleY", 1f, 1.1f)
                animY.duration = 100
                animY.interpolator = LinearInterpolator()
                animY.start()
                val animXBackground = ObjectAnimator.ofFloat(binding!!.addWrapper, "scaleX", 1f, 0.9f)
                animXBackground.duration = 100
                animXBackground.interpolator = LinearInterpolator()
                animXBackground.start()
                val animYBackground = ObjectAnimator.ofFloat(binding!!.addWrapper, "scaleY", 1f, 0.9f)
                animYBackground.duration = 100
                animYBackground.interpolator = LinearInterpolator()
                animYBackground.start()
            }
            false
        }*/
        binding!!.submenuWrapper.setOnClickListener { hideSubmenu() }
        updateItemSelection()

        val cutout = context.getDrawable(R.drawable.bottom_navigation_inset)
        cutout?.setColorFilter(context.getThemeColor(R.attr.barColor), PorterDuff.Mode.MULTIPLY)
        binding!!.cutoutBackground.setImageDrawable(cutout)
        val fabBackground = context.getDrawable(R.drawable.fab_background)
        fabBackground?.setColorFilter(context.getThemeColor(R.attr.colorAccent), PorterDuff.Mode.MULTIPLY)
        binding!!.addWrapper.background = fabBackground
    }

    private fun animateButtonTap() {
        return

        val animX = ObjectAnimator.ofFloat(binding!!.add, "scaleX", 1.3f, 1f)
        animX.duration = 400
        animX.interpolator = BounceInterpolator()
        animX.start()
        val animY = ObjectAnimator.ofFloat(binding!!.add, "scaleY", 1.3f, 1f)
        animY.duration = 400
        animY.interpolator = BounceInterpolator()
        animY.start()
        val animXBackground = ObjectAnimator.ofFloat(binding!!.addWrapper, "scaleX", 0.9f, 1f)
        animXBackground.duration = 600
        animXBackground.interpolator = BounceInterpolator()
        animXBackground.start()
        val animYBackground = ObjectAnimator.ofFloat(binding!!.addWrapper, "scaleY", 0.9f, 1f)
        animYBackground.duration = 600
        animYBackground.interpolator = BounceInterpolator()
        animYBackground.start()
    }

    private fun showSubmenu() {
        if (isShowingSubmenu) return
        isShowingSubmenu = true

        val rotate = RotateAnimation(0f, 135f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotate.duration = 250
        rotate.interpolator = LinearInterpolator()
        rotate.fillAfter = true
        binding!!.add.startAnimation(rotate)

        var pos = 4
        binding!!.submenuWrapper.removeAllViews()
        for (taskType in listOf(Task.TYPE_HABIT, Task.TYPE_DAILY, Task.TYPE_TODO, Task.TYPE_REWARD)) {
            val view = BottomNavigationSubmenuItem(context)
            when (taskType) {
                Task.TYPE_HABIT -> {
                    view.icon = context.getDrawable(R.drawable.add_habit)
                    view.title = context.getString(R.string.habit)
                }
                Task.TYPE_DAILY -> {
                    view.icon = context.getDrawable(R.drawable.add_daily)
                    view.title = context.getString(R.string.daily)
                }
                Task.TYPE_TODO -> {
                    view.icon = context.getDrawable(R.drawable.add_todo)
                    view.title = context.getString(R.string.todo)
                }
                Task.TYPE_REWARD -> {
                    view.icon = context.getDrawable(R.drawable.add_rewards)
                    view.title = context.getString(R.string.reward)
                }
            }
            view.onAddListener = {
                onAddListener?.invoke(taskType)
                hideSubmenu()
            }
            binding!!.submenuWrapper.addView(view)
            view.alpha = 0f
            view.scaleY = 0.7f
            ViewCompat.animate(view).alpha(1f).setDuration(250.toLong()).startDelay = (100 * pos).toLong()
            ViewCompat.animate(view).scaleY(1f).setDuration(250.toLong()).startDelay = (100 * pos).toLong()
            pos -= 1
        }
        var widestWidth = 0
        for (view in binding!!.submenuWrapper.children) {
            if (view is BottomNavigationSubmenuItem) {
                val width = view.measuredTitleWidth
                if (widestWidth < width) {
                    widestWidth = width
                }
            }
        }
        for (view in binding!!.submenuWrapper.children) {
            if (view is BottomNavigationSubmenuItem) {
                view.setTitleWidth(widestWidth)
            }
        }
    }

    private fun hideSubmenu() {
        isShowingSubmenu = false
        var pos = 0

        val rotate = RotateAnimation(135f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotate.duration = 250
        rotate.interpolator = LinearInterpolator()
        rotate.fillAfter = true
        binding!!.add.startAnimation(rotate)

        for (view in binding!!.submenuWrapper.children) {
            view.alpha = 1f
            view.scaleY = 1f
            ViewCompat.animate(view).alpha(0f).setDuration(200.toLong()).startDelay = (150 * pos).toLong()
            ViewCompat.animate(view).scaleY(0.7f).setDuration(250.toLong()).setStartDelay((100 * pos).toLong()).withEndAction { binding!!.submenuWrapper.removeView(view) }
            pos += 1
        }
    }

    fun tabWithId(id: Int): BottomNavigationItem? {
        return when(id) {
            R.id.tab_habits -> binding!!.tabHabits
            R.id.tab_dailies -> binding!!.tabDailies
            R.id.tab_todos -> binding!!.tabRewards
            R.id.tab_rewards -> binding!!.tabTodos
            else -> null
        }
    }

    private fun updateItemSelection() {
        binding!!.tabHabits.isActive = activeTaskType == Task.TYPE_HABIT
        binding!!.tabDailies.isActive = activeTaskType == Task.TYPE_DAILY
        binding!!.tabTodos.isActive = activeTaskType == Task.TYPE_TODO
        binding!!.tabRewards.isActive = activeTaskType == Task.TYPE_REWARD
    }

    override fun onDetachedFromWindow() {
        binding=null
        onTabSelectedListener=null
        onAddListener=null
        super.onDetachedFromWindow()
    }
}