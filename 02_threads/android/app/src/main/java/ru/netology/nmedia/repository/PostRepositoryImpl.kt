package ru.netology.nmedia.repository

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.internal.EMPTY_REQUEST
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.viewmodel.PostViewModel
import java.io.IOException
import java.util.concurrent.TimeUnit


class PostRepositoryImpl : PostRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Post>>() {}
    private val _data = MutableLiveData(FeedModel())

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999"
        private val jsonType = "application/json".toMediaType()
    }

    override fun getAllAsync(callback: PostRepository.Callback<List<Post>>) {
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts")
            .build()

        return client.newCall(request)
            .enqueue(
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        callback.onError(e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseText = response.body?.string()
                        if (responseText == null) {
                            callback.onError(java.lang.RuntimeException("body is null"))
                            return
                        }
                        try {
                            callback.onSuccess(gson.fromJson(responseText, typeToken.type))
                        } catch (e: Exception) {
                            callback.onError(java.lang.RuntimeException("body is null"))
                        }
                    }
                }
            )
    }

    override fun likeByIdAsync(id: Long, callback: PostRepository.Callback<Post>) {
        val request: Request = Request.Builder()
            .post(EMPTY_REQUEST)
            .url("${BASE_URL}/api/slow/posts/$id/likes")
            .build()
        return client.newCall(request)
            .enqueue(
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        callback.onError(e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseText = response.body?.string()
                        if (responseText == null) {
                            callback.onError(java.lang.RuntimeException("body is null"))
                            return
                        }
                        try {
                            callback.onSuccess(gson.fromJson(responseText, Post::class.java))
                        } catch (e: Exception) {
                            callback.onError(java.lang.RuntimeException("body is null"))
                        }
                    }
                }
            )

    }

    override fun unlikeByIdAcync(id: Long, callback: PostRepository.Callback<Post>) {
        val request: Request = Request.Builder()
            .delete()
            .url("${BASE_URL}/api/slow/posts/$id/likes")
            .build()

        return client.newCall(request)
            .enqueue(
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        callback.onError(e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseText = response.body?.string()
                        if (responseText == null) {
                            callback.onError(java.lang.RuntimeException("body is null"))
                            return
                        }
                        try {
                            callback.onSuccess(gson.fromJson(responseText, Post::class.java))
                        } catch (e: Exception) {
                            callback.onError(java.lang.RuntimeException("body is null"))
                        }
                    }
                }
            )
    }


    override fun saveAsync(post: Post, callback: PostRepository.Callback<Post>) {
        val request: Request = Request.Builder()
            .post(gson.toJson(post).toRequestBody(jsonType))
            .url("${BASE_URL}/api/slow/posts")
            .build()

        return client.newCall(request)
            .enqueue(
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        callback.onError(e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseText = response.body?.string()
                        if (responseText == null) {
                            callback.onError(java.lang.RuntimeException("body is null"))
                            return
                        }
                        try {
                            callback.onSuccess(gson.fromJson(responseText, typeToken.type))
                        } catch (e: Exception) {
                            callback.onError(java.lang.RuntimeException("body is null"))
                        }
                    }
                }
            )
    }

    override fun removeByIdAsync(id: Long, callback: PostRepository.Callback<Post>) {
        val request: Request = Request.Builder()
            .delete()
            .url("${BASE_URL}/api/slow/posts/$id")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val old = _data.value?.posts.orEmpty()
                _data.postValue(
                    _data.value?.copy(posts = _data.value?.posts.orEmpty()
                        .filter { it.id != id }
                    )
                )
                val responseText = response.body?.string()
                if (responseText != null) {
                    try {
                        callback.onSuccess(gson.fromJson(responseText, Post::class.java))
                    } catch (e: IOException) {
                        _data.postValue(_data.value?.copy(posts = old))
                    }
                }
            }
        } )
    }
}
