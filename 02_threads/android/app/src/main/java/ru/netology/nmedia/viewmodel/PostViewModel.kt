package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import org.chromium.base.Callback
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.IOException
import kotlin.Exception

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    likedByMe = false,
    likes = 0,
    published = "",
    authorAvatar = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    fun loadPosts() {
        _data.postValue(FeedModel(loading = true))

        repository.getAllAsync(object : PostRepository.Callback<List<Post>> {
            override fun onSuccess(result: List<Post>) {
                _data.postValue(FeedModel(posts = result, empty = result.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    fun save() {
        edited.value?.let {
            repository.saveAsync(it, object : PostRepository.Callback<Post> {

                override fun onSuccess(result: Post) {
                    _postCreated.postValue(Unit)
                   /* _data.postValue(FeedModel(posts = result, empty = result.isEmpty())) */
                }

                override fun onError(e: Exception) {
                    _data.postValue(FeedModel(error = true))
                }
            })
        }
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun likeById(post: Post) {
        try {
            val updated = if (post.likedByMe) {
                repository.unlikeByIdAcync(post.id, object : PostRepository.Callback<Post> {

                    override fun onSuccess(result: Post) {
                        val newPosts = _data.value?.posts?.map {
                            if (it.id == post.id) {
                                result
                            } else {
                                it
                            }
                        }.orEmpty()
                        _data.postValue(data.value?.copy(posts = newPosts))
                    }

                    override fun onError(e: Exception) {
                        _data.postValue(FeedModel(error = true))
                    }
                })
            } else {
                repository.likeByIdAsync(post.id, object : PostRepository.Callback<Post> {
                    override fun onSuccess(result: Post) {
                        val newPosts = _data.value?.posts?.map {
                            if (it.id == post.id) {
                                result
                            } else {
                                it
                            }
                        }.orEmpty()
                        _data.postValue(data.value?.copy(posts = newPosts))
                    }

                    override fun onError(e: Exception) {
                        _data.postValue(FeedModel(error = true))
                    }
                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    fun removeById(id: Long) {

        val old = _data.value?.posts.orEmpty()
        _data.postValue(
            _data.value?.copy(posts = _data.value?.posts.orEmpty()
                .filter { it.id != id }
            )
        )
        try {
            repository.removeByIdAsync(id, object : PostRepository.Callback<Post> {

                override fun onSuccess(result: Post) {
                   /* _data.postValue(FeedModel(posts = result, empty = result.isEmpty())) */
                }

                override fun onError(e: Exception) {
                    _data.postValue(_data.value?.copy(posts = old))
                }
            })
        } catch (e: IOException) {
            _data.postValue(_data.value?.copy(posts = old))
        }
    }

}
