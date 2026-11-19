package com.sudutcorp.sudutbulat.data.repository

import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.data.model.User
import com.sudutcorp.sudutbulat.data.remote.UserRemoteDataSource
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing user data.
 * Wraps [UserRemoteDataSource] to provide a clean data API.
 */
class UserRepository(
    private val remoteDataSource: UserRemoteDataSource
) {
    suspend fun getUserById(userId: String): Result<User?> {
        return remoteDataSource.getUserById(userId)
    }

    fun getUserFlow(userId: String): Flow<User?> {
        return remoteDataSource.getUserFlow(userId)
    }

    fun getAllUsersFlow(): Flow<List<User>> {
        return remoteDataSource.getAllUsersFlow()
    }

    suspend fun getAllUsers(): Result<List<User>> {
        return remoteDataSource.getAllUsers()
    }

    suspend fun getUsersByRole(role: Role): Result<List<User>> {
        return remoteDataSource.getUsersByRole(role)
    }

    suspend fun getUsersByOutlet(outletId: String): Result<List<User>> {
        return remoteDataSource.getUsersByOutlet(outletId)
    }

    suspend fun saveUser(user: User): Result<Unit> {
        return remoteDataSource.saveUser(user)
    }

    suspend fun updateUser(user: User): Result<Unit> {
        return remoteDataSource.updateUser(user)
    }

    suspend fun setUserActive(userId: String, isActive: Boolean): Result<Unit> {
        return remoteDataSource.setUserActive(userId, isActive)
    }

    suspend fun assignUsersToOutlet(userIds: List<String>, outletId: String): Result<Unit> {
        return remoteDataSource.assignUsersToOutlet(userIds, outletId)
    }
}
