package ru.netology.nmedia.repository

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import java.io.IOException
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


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

        PostsApi.retrofitService.getAll().enqueue(object : Callback<List<Post>> {

            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                if (!response.isSuccessful) {
                    callback.onError(RuntimeException(response.message()))
                    return
                }
                callback.onSuccess(response.body() ?: throw RuntimeException("body is null"))
            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun likeByIdAsync(id: Long, callback: PostRepository.Callback<Post>) {

        PostsApi.retrofitService.likeById(id).enqueue(object : Callback<Post> {

            override fun onFailure(call: Call<Post>, t: Throwable) {
                callback.onError(Exception(t))
            }

            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    callback.onSuccess(response.body()!!)
                } else {
                    callback.onError(RuntimeException("Response is not successful"))
                }
            }
        })
    }

    override fun unlikeByIdAsync(id: Long, callback: PostRepository.Callback<Post>) {

        PostsApi.retrofitService.unlikeById(id).enqueue(object : Callback<Post> {

            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    callback.onSuccess(response.body()!!)
                } else if (response.code() == 500) {
                    callback.onError(Exception("фиксики уже чинят сервер!"))
                } else {
                    callback.onError(RuntimeException("Response is not successful"))
                    }
                }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                callback.onError(Exception(t))
            }
        }
        )
    }


    override fun saveAsync(post: Post, callback: PostRepository.Callback<Unit>) {

        PostsApi.retrofitService.save(post).enqueue(object : Callback<Unit> {

            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    callback.onSuccess(Unit)
                } else
                    callback.onError(RuntimeException("error code: ${response.code()} with ${response.message()}"))
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                callback.onError(Exception(t))
            }
        })
    }

    override fun removeByIdAsync(id: Long, callback: PostRepository.Callback<Unit>) {

        PostsApi.retrofitService.removeById(id).enqueue(object : Callback<Unit> {


            override fun onFailure(call: Call<Unit>, t: Throwable) {
                callback.onError(Exception(t))
            }

            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val old = _data.value?.posts.orEmpty()
                _data.postValue(
                    _data.value?.copy(posts = _data.value?.posts.orEmpty()
                        .filter { it.id != id }
                    )
                )
                try {
                    callback.onSuccess(Unit)
                } catch (e: IOException) {
                    _data.postValue(_data.value?.copy(posts = old))
                }
            }
        })
    }
}
