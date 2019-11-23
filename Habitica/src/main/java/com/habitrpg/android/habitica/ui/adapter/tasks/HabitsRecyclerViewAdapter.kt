package com.habitrpg.android.habitica.ui.adapter.tasks

import android.view.ViewGroup
import com.habitrpg.android.habitica.helpers.TaskFilterHelper
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.viewHolders.tasks.HabitViewHolder
import io.realm.OrderedRealmCollection
import android.view.LayoutInflater
import android.view.View
import com.habitrpg.android.habitica.databinding.HabitItemCardBinding
import androidx.recyclerview.widget.RecyclerView




class HabitsRecyclerViewAdapter(data: OrderedRealmCollection<Task>?, autoUpdate: Boolean, layoutResource: Int, taskFilterHelper: TaskFilterHelper) : RealmBaseTasksRecyclerViewAdapter<HabitViewHolder>(data, autoUpdate, layoutResource, taskFilterHelper) {

    var cardBinding: HabitItemCardBinding?=null

    override fun getContentView(parent: ViewGroup): View = cardBinding?.root!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder{
        val layoutInflater = LayoutInflater.from(parent.context)
        cardBinding = HabitItemCardBinding.inflate(layoutInflater, parent, false)

        return HabitViewHolder(cardBinding, { task, direction -> taskScoreEventsSubject.onNext(Pair(task, direction)) }) {
            task -> taskOpenEventsSubject.onNext(task)
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        cardBinding=null
    }

}