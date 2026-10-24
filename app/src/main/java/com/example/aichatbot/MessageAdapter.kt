package com.example.aichatbot

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(private val messages: List<Message>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessage: TextView = view.findViewById(R.id.tvMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.tvMessage.text = message.text

        val params = holder.tvMessage.layoutParams as FrameLayout.LayoutParams
        if (message.isUser) {
            params.gravity = Gravity.END
            holder.tvMessage.setBackgroundResource(R.drawable.bg_user)
            holder.tvMessage.setTextColor(0xFFECECEC.toInt())
        } else {
            params.gravity = Gravity.START
            holder.tvMessage.setBackgroundResource(R.drawable.bg_ai)
            holder.tvMessage.setTextColor(0xFFECECEC.toInt())
        }
        holder.tvMessage.layoutParams = params
    }

    override fun getItemCount() = messages.size
}