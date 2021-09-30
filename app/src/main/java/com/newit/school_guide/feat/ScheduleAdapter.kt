package com.newit.school_guide.feat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.newit.school_guide.core.common.Utils
import com.newit.school_guide.databinding.ItemScheduleBinding
import com.newit.school_guide.databinding.LayoutMonthBinding
import com.newit.school_guide.feat.model.EventModel

class ScheduleAdapter(var list : ArrayList<EventModel>,var onClickItem : ((EventModel)-> Unit)?= null) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    companion object{
        val TYPE_SESSION = 1
        val TYPE_EVENT = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == TYPE_EVENT){
            val binding = ItemScheduleBinding.inflate(LayoutInflater.from(parent.context),parent,false)
            return EventViewHoler(binding)
        }else{
            val binding = LayoutMonthBinding.inflate(LayoutInflater.from(parent.context),parent,false)
            return MonthViewHoler(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder.itemViewType == TYPE_EVENT){
            (holder as EventViewHoler).bindData(list.get(position))
        }else{
            (holder as MonthViewHoler).bindData(list.get(position))
        }
    }

    override fun getItemCount(): Int {
       return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return list[position].type
    }

    fun setData(data : ArrayList<EventModel>){
        this.list = data
        notifyDataSetChanged()
    }

    inner class MonthViewHoler(val binding: LayoutMonthBinding) : RecyclerView.ViewHolder(binding.root){
        fun bindData(event: EventModel){
            binding.tvMonth.text = event.title
        }
    }

    inner class EventViewHoler(val binding: ItemScheduleBinding) : RecyclerView.ViewHolder(binding.root){
        fun bindData(event: EventModel){
            binding.tvTitle.text = event.title

            binding.tvDate.text = Utils.dateToStringDateJP(event.timeStart)
            if(Utils.dateToStringDateJP(event.timeStart).equals(Utils.dateToStringDateJP(event.timeEnd))){
                binding.tvHour.text = Utils.dateToStringHour(event.timeStart) + " - " + Utils.dateToStringHour(event.timeEnd)
            }else{
                binding.tvHour.text = Utils.dateToStringHour(event.timeStart)
            }
            binding.root.setOnClickListener {
                onClickItem?.invoke(event)
            }
        }
    }

}