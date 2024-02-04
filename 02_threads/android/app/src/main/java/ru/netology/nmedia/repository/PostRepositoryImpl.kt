package ru.netology.nmedia.repository


import androidx.lifecycle.map
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dto.Post
import java.io.IOException
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiException
import ru.netology.nmedia.error.NetworkException
import ru.netology.nmedia.error.UnknownException


class PostRepositoryImpl(private val dao: PostDao) : PostRepository {

    override val data = dao.getAll().map { it.toDto() }

    override suspend fun getAll() {
        try {
            val response = PostsApi.retrofitService.getAll()
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiException(response.code(), response.message())
            dao.insert(body.toEntity())
        } catch (e: ApiException) {
            throw e
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun likeById(id: Long) {
        PostsApi.retrofitService.likeById(id)
    }

    override suspend fun unlikeById(id: Long) {
        PostsApi.retrofitService.unlikeById(id)
    }

    override suspend fun save(post: Post) {
        PostsApi.retrofitService.save(post)
    }

    override suspend fun removeById(id: Long) {
        PostsApi.retrofitService.removeById(id)
    }

}
