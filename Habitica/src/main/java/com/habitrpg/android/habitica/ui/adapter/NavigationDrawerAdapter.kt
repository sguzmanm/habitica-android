package com.habitrpg.android.habitica.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.DrawerMainItemBinding
import com.habitrpg.android.habitica.databinding.HabitItemCardBinding
import com.habitrpg.android.habitica.extensions.dpToPx
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.ui.helpers.bindOptionalView
import com.habitrpg.android.habitica.ui.menu.HabiticaDrawerItem
import com.habitrpg.android.habitica.ui.views.promo.SubscriptionBuyGemsPromoView
import com.habitrpg.android.habitica.ui.views.promo.SubscriptionBuyGemsPromoViewHolder
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject


class NavigationDrawerAdapter(tintColor: Int, backgroundTintColor: Int): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var drawerMainItemBinding: DrawerMainItemBinding?=null

    var tintColor: Int = tintColor
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var backgroundTintColor: Int = backgroundTintColor
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    internal val items: MutableList<HabiticaDrawerItem> = ArrayList()
    var selectedItem: Int? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val itemSelectedEvents = PublishSubject.create<HabiticaDrawerItem>()


    fun getItemSelectionEvents(): Flowable<HabiticaDrawerItem> = itemSelectedEvents.toFlowable(BackpressureStrategy.DROP)

    fun getItemWithTransitionId(transitionId: Int): HabiticaDrawerItem? =
            items?.find { it.transitionId == transitionId }
    fun getItemWithIdentifier(identifier: String): HabiticaDrawerItem? =
            items?.find { it.identifier == identifier }

    private fun getItemPosition(transitionId: Int): Int =
            items?.indexOfFirst { it.transitionId == transitionId }
    private fun getItemPosition(identifier: String): Int =
            items?.indexOfFirst { it.identifier == identifier }

    fun updateItem(item: HabiticaDrawerItem) {
        val position = getItemPosition(item.identifier)
        items[position] = item
        notifyDataSetChanged()
    }

    fun updateItems(newItems: List<HabiticaDrawerItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val drawerItem = getItem(position)
        if (getItemViewType(position) == 0) {
            val itemHolder = holder as? DrawerItemViewHolder
            itemHolder?.tintColor = tintColor
            itemHolder?.backgroundTintColor = backgroundTintColor
            itemHolder?.bind(drawerItem, drawerItem.transitionId == selectedItem)
            itemHolder?.itemView?.setOnClickListener { itemSelectedEvents.onNext(drawerItem) }
        } else if (getItemViewType(position) == 1) {
            (holder as? SectionHeaderViewHolder)?.backgroundTintColor = backgroundTintColor
            (holder as? SectionHeaderViewHolder)?.bind(drawerItem)
        }
    }

    private fun getItem(position: Int) = items.filter { it.isVisible }[position]

    override fun getItemCount(): Int = items.count { it.isVisible }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isHeader) {
            1
        } else {
            if (getItem(position).isPromo) {
                2
            } else {
                0
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if(viewType == 0){
            val layoutInflater = LayoutInflater.from(parent.context)
            drawerMainItemBinding = DrawerMainItemBinding.inflate(layoutInflater, parent, false)
        }

        return when (viewType) {
            0 -> DrawerItemViewHolder(drawerMainItemBinding)
            1 -> SectionHeaderViewHolder(parent.inflate(R.layout.drawer_main_section_header))
            else -> {
                val itemView = SubscriptionBuyGemsPromoView(parent.context)
                itemView.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        148.dpToPx(parent.context)
                )
                SubscriptionBuyGemsPromoViewHolder(itemView)
            }
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        drawerMainItemBinding=null
        super.onDetachedFromRecyclerView(recyclerView)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        drawerMainItemBinding=null
        super.onViewDetachedFromWindow(holder)
    }

    class DrawerItemViewHolder(private val binding: DrawerMainItemBinding?) : RecyclerView.ViewHolder(binding!!.root) {

        var tintColor: Int = 0
        var backgroundTintColor: Int = 0

        /*
        private val titleTextView: TextView? by bindOptionalView(itemView, R.id.titleTextView)
        private val pillView: TextView? by bindOptionalView(itemView, R.id.pillView)
        private val bubbleView: View? by bindOptionalView(itemView, R.id.bubble_view)
        private val additionalInfoView: TextView? by bindOptionalView(itemView, R.id.additionalInfoView)
        */

        fun bind(drawerItem: HabiticaDrawerItem, isSelected: Boolean) {
            binding!!.titleTextView?.text = drawerItem.text

            if (isSelected) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.gray_600))
                binding.titleTextView?.setTextColor(tintColor)
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.white))
                binding.titleTextView?.setTextColor(ContextCompat.getColor(itemView.context, R.color.gray_10))
            }

            if (drawerItem.additionalInfo == null) {
                binding.pillView?.visibility = View.GONE
                binding.additionalInfoView?.visibility = View.GONE
            } else {
                if (drawerItem.additionalInfoAsPill) {
                    binding.additionalInfoView?.visibility = View.GONE
                    val pillView = binding.pillView
                    if (pillView != null) {
                        pillView.visibility = View.VISIBLE
                        pillView.text = drawerItem.additionalInfo

                        val pL = pillView.paddingLeft
                        val pT = pillView.paddingTop
                        val pR = pillView.paddingRight
                        val pB = pillView.paddingBottom

                        pillView.background = ContextCompat.getDrawable(itemView.context, R.drawable.pill_bg_purple_200)
                        pillView.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                        pillView.setPadding(pL, pT, pR, pB)
                    }
                } else {
                    binding.pillView?.visibility = View.GONE
                    val additionalInfoView = binding.additionalInfoView
                    if (additionalInfoView != null) {
                        additionalInfoView.text = drawerItem.additionalInfo
                        additionalInfoView.visibility = View.VISIBLE
                        additionalInfoView.setTextColor(drawerItem.additionalInfoTextColor ?: tintColor)
                    }
                }
            }
            binding.bubbleView?.visibility = if (drawerItem.showBubble) View.VISIBLE else View.GONE
        }
    }

    class SectionHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var backgroundTintColor: Int = 0

        fun bind(drawerItem: HabiticaDrawerItem) {
            (itemView as? TextView)?.text = drawerItem.text
            itemView.setBackgroundColor(backgroundTintColor)
        }
    }
}