package org.androidbh.firechat

import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.item_message.view.*

/**
 * Created by felipets on 6/20/17.
 */
class MessageViewHolder(itemView: View, var requestOptions: RequestOptions) : RecyclerView.ViewHolder(itemView){
    fun bindMessage(message: Message) {
        with(message) {
            itemView.messageTextView.text = text
            itemView.messengerTextView.text = user?.displayName ?: ""

            Glide.with(itemView.context)
                    .load(user?.photoURL)
                    .apply(requestOptions)
                    .into(itemView.messengerImageView)
        }
    }
}