package org.androidbh.firechat

import android.support.v7.widget.RecyclerView
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.item_photo.view.*

/**
 * Created by felipets on 6/20/17.
 */
class PhotoViewHolder(itemView: View, var requestOptions: RequestOptions, val itemClick:(Message) -> Unit) : RecyclerView.ViewHolder(itemView){

    fun bindMessage(message: Message) {
        with(message) {

            itemView.messengerTextView.text = user?.displayName ?: ""
            itemView.likesTextView.text = "%d Likes".format(message.likes_count)

            Glide.with(itemView.context)
                    .load(user?.photoURL)
                    .apply(requestOptions)
                    .into(itemView.messengerImageView)

            Glide.with(itemView.context)
                    .load(photo)
                    .into(itemView.messagePhoto)

            itemView.setOnClickListener { itemClick(this) }
        }
    }
}