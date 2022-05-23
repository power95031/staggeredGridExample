package com.example.staggeredgridexample.Adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.staggeredgridexample.Objects.SearchObject
import com.example.staggeredgridexample.R
import com.example.staggeredgridexample.databinding.ItemStaggeredBinding

class StaggeredAdapter(
    var context:Context,
    var arrayList:ArrayList<SearchObject.SearchItem>?,
    val callback:(selectedPosition:Int, status:Boolean, idValue:String)->Unit
) : RecyclerView.Adapter<StaggeredAdapter.StaggeredViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaggeredViewHolder {
        return StaggeredViewHolder(
            ItemStaggeredBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: StaggeredViewHolder, position: Int) {
        arrayList?.also {
            Glide.with(context).load("https://data.dal-da.co.kr"+ it[position].thumbnail_uri).into(holder.binding.ThumbnailImage)

            Glide.with(context).load("https://data.dal-da.co.kr"+ it[position].userImage)
                .placeholder(R.drawable.user_default).into(holder.binding.profileImage)

            holder.binding.VideoTitle.text = it[position].text

            holder.binding.userName.text = it[position].userName

            if(it[position].isSelected) {
                holder.binding.CheckIcon.visibility = View.VISIBLE
            } else {
                holder.binding.CheckIcon.visibility = View.INVISIBLE
            }

            holder.binding.root.setOnClickListener { _ ->
                if(it[position].isSelected) {
                    // 선택중 상태이므로 해제 이벤트
                    it[position].isSelected = false
                    callback(position, false, it[position].dicId)
                } else {
                    // 미선택중 상태이므로 선택 이벤트
                    it[position].isSelected = true
                    callback(position, true, it[position].dicId)
                }
                notifyItemChanged(position)
            }
        } ?: run {
            Glide.with(context).clear(holder.binding.ThumbnailImage)

            Glide.with(context).clear(holder.binding.profileImage)

            holder.binding.VideoTitle.text = ""

            holder.binding.userName.text = ""
        }
    }

    override fun getItemCount(): Int {
        return arrayList?.size?:0
    }

    class StaggeredViewHolder(var binding:ItemStaggeredBinding) : RecyclerView.ViewHolder(binding.root)
}