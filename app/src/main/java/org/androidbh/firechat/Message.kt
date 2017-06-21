package org.androidbh.firechat

import com.google.gson.annotations.SerializedName

/**
 * Created by felipets on 6/20/17.
 */
class Message {

    var id: String? = null
    var text: String? = null
    var user: User? = null
    var photo: String? = null
    var typeMessage: String? = null
//    @SerializedName("likes_count")
    var likes_count: Int = 0

    constructor() {}

    constructor(text: String, user: User, typeMessage: String = "message") {
        this.text = text
        this.user = user
        this.typeMessage = typeMessage
    }
}