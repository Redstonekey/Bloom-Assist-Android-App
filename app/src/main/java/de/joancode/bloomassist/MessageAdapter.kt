package de.joancode.bloomassist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(private val messages: List<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_USER = 1
    private val VIEW_TYPE_AI = 2

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_AI
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_USER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_user, parent, false)
                UserMessageViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_ai, parent, false)
                AIMessageViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        when (holder) {
            is UserMessageViewHolder -> holder.bind(message)
            is AIMessageViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    inner class UserMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val messageTime: TextView = itemView.findViewById(R.id.messageTime)
        private val messageImage: ImageView = itemView.findViewById(R.id.messageImage)
        private val plantLabel: TextView = itemView.findViewById(R.id.plantLabel)

        fun bind(message: Message) {
            messageText.text = message.text
            messageTime.text = message.time

            // Handle image if present
            if (message.imageUri != null) {
                messageImage.visibility = View.VISIBLE
                messageImage.setImageURI(message.imageUri)
            } else {
                messageImage.visibility = View.GONE
            }

            // Handle plant label if present
            if (message.plantName != null) {
                plantLabel.visibility = View.VISIBLE
                plantLabel.text = "Plant: ${message.plantName}"
            } else {
                plantLabel.visibility = View.GONE
            }
        }
    }

    inner class AIMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val messageTime: TextView = itemView.findViewById(R.id.messageTime)

        fun bind(message: Message) {
            messageText.text = message.text
            messageTime.text = message.time
        }
    }
}