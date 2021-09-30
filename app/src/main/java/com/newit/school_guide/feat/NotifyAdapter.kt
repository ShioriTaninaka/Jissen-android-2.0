package com.newit.school_guide.feat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.newit.school_guide.core.common.Utils
import com.newit.school_guide.databinding.ItemNotificationBinding
import com.newit.school_guide.feat.model.Info

class NotifyAdapter(var list: List<Info>, var onClickItem: ((Info) -> Unit)? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bindData(this.list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: Info) {
            binding.tvTitle.text = item.title
            binding.tvDescription.text = Utils.strDateToStringJP(item.published)
            if(item.unread == 1){
                binding.tvUnread.visibility = View.VISIBLE
            }else{
                binding.tvUnread.visibility = View.INVISIBLE
            }
            binding.root.setOnClickListener {
                onClickItem?.invoke(item)
            }
        }
    }

    fun setData(list: List<Info>){
        this.list = list
        notifyDataSetChanged()
    }

}