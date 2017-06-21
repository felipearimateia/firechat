package org.androidbh.firechat

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.request.RequestOptions


/**
 * Created by felipets on 6/20/17.
 */
class MessageAdapter(context: Context, private var items: MutableList<Message>, val itemClick:(Message) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val layoutInflater: LayoutInflater
    private val requestOptions: RequestOptions

    init {
        this.layoutInflater = LayoutInflater.from(context)
        requestOptions = RequestOptions()
                .placeholder(R.drawable.ic_account_circle_black_36dp)
                .error(R.drawable.ic_account_circle_black_36dp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == 2) {
            val viewRoot = layoutInflater.inflate(R.layout.item_photo, parent, false)
            return PhotoViewHolder(viewRoot, requestOptions, itemClick)

        } else {
            val viewRoot = layoutInflater.inflate(R.layout.item_message, parent, false)
            return MessageViewHolder(viewRoot, requestOptions)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)

        if (getType(message) == 2) {
            (holder as PhotoViewHolder).bindMessage(message)
        }
        else {
            (holder as MessageViewHolder).bindMessage(message)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return getType(items[position])
    }

    fun getItem(position: Int): Message {
        return items[position]
    }

    fun addAll(messages: Collection<Message>) {
        items.addAll(messages)
        notifyDataSetChanged()
    }

    fun add(message: Message) {
        items.add(message)
        notifyItemInserted(itemCount - 1)
    }

    fun insert(message: Message, index: Int) {
        items.add(index, message)
        notifyItemInserted(index)
    }

    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }

    private fun getType(message: Message): Int {
        when (message.typeMessage) {
            "photo" -> return 2
            else -> return 1
        }
    }
}