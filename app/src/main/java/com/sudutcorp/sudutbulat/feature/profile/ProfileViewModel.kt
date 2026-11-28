package com.sudutcorp.sudutbulat.feature.profile

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sudutcorp.sudutbulat.core.util.ProfileImageProcessor
import com.sudutcorp.sudutbulat.core.util.buildInitials
import com.sudutcorp.sudutbulat.data.auth.AuthRepository
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.data.model.User
import com.sudutcorp.sudutbulat.data.remote.CloudinaryMediaUploader
import com.sudutcorp.sudutbulat.data.repository.UserRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class ProfileIdentityUi(
    val userId: String,
    val displayName: String,
    val email: String,
    val roleLabel: String,
    val initials: String,
    val photoUrl: String?
)

@Immutable
data class ProfileUiState(
    val users: List<User> = emptyList(),
    val localPhotoOverrides: Map<String, String?> = emptyMap(),
    val isPhotoUpdating: Boolean = false,
    val showPhotoOptionsSheet: Boolean = false,
    val showDeletePhotoConfirmDialog: Boolean = false
)

sealed interface ProfileUiEvent {
    data object OpenPhotoOptionsSheet : ProfileUiEvent
    data object DismissPhotoOptionsSheet : ProfileUiEvent
    data object RequestDeletePhoto : ProfileUiEvent
    data object DismissDeletePhotoConfirm : ProfileUiEvent
    data class UploadProfilePhoto(
        val userId: String,
        val jpegBytes: ByteArray
    ) : ProfileUiEvent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is UploadProfilePhoto) return false
            return userId == other.userId && jpegBytes.contentEquals(other.jpegBytes)
        }

        override fun hashCode(): Int = 31 * userId.hashCode() + jpegBytes.contentHashCode()
    }
    data class ConfirmDeleteProfilePhoto(val userId: String) : ProfileUiEvent
    data class PhotoProcessFailed(val errorMessage: String) : ProfileUiEvent
}

sealed interface ProfileUiEffect {
    data class ShowSnackbar(val message: String) : ProfileUiEffect
}

/**
 * Resolves the active user's profile identity (name, email, role label, photo URL, and 1-or-2 word initials)
 * from Firestore users + local overrides with deterministic fallback for seeded/dev accounts.
 */
fun resolveProfileIdentity(
    state: ProfileUiState,
    userRole: Role,
    userEmail: String?,
    isDevAccount: Boolean,
    ownerFallbackName: String = "Owner",
    ownerFallbackEmail: String = "sdb.owner@gmail.com"
): ProfileIdentityUi {
    val cleanEmail = userEmail?.trim()?.takeIf { it.isNotBlank() }
    val matchedByEmail = cleanEmail?.let { email ->
        state.users.firstOrNull { it.email.equals(email, ignoreCase = true) }
    }
    val matchedByRole = when {
        isDevAccount -> state.users.firstOrNull { it.role == Role.DEV || it.id == "dev" }
        else -> state.users.firstOrNull { it.role == userRole }
    }
    val matchedUser = if (isDevAccount) {
        matchedByEmail?.takeIf { it.role == Role.DEV } ?: matchedByRole
    } else {
        matchedByEmail?.takeIf { it.role == userRole } ?: matchedByRole ?: matchedByEmail
    }

    val defaultUserId = when {
        isDevAccount -> "dev"
        userRole == Role.DEV -> "dev"
        userRole == Role.OWNER -> "owner"
        userRole == Role.WAREHOUSE -> "gudang"
        userRole == Role.OUTLET -> "staff"
        else -> "owner"
    }
    val resolvedUserId = matchedUser?.id?.takeIf { it.isNotBlank() } ?: defaultUserId

    val rawDisplayName = when {
        isDevAccount -> matchedUser?.name?.takeIf { it.isNotBlank() } ?: "Developer SDB"
        matchedUser != null && matchedUser.name.isNotBlank() -> matchedUser.name
        userRole == Role.DEV -> "Developer SDB"
        userRole == Role.OWNER -> ownerFallbackName.ifBlank { "Owner" }
        userRole == Role.WAREHOUSE -> "Budi Santoso"
        userRole == Role.OUTLET -> "Dimas Pratama"
        else -> ownerFallbackName
    }
    val cleanDisplayName = rawDisplayName.replace(Regex("\\s*\\(.*?\\)"), "").trim().ifBlank { rawDisplayName }

    val resolvedEmail = cleanEmail
        ?: matchedUser?.email?.takeIf { it.isNotBlank() }
        ?: when {
            isDevAccount || userRole == Role.DEV -> "sdb.dev@gmail.com"
            userRole == Role.OWNER -> ownerFallbackEmail.ifBlank { "sdb.owner@gmail.com" }
            userRole == Role.WAREHOUSE -> "sdb.gudang@gmail.com"
            userRole == Role.OUTLET -> "sdb.staff@gmail.com"
            else -> "sdb.owner@gmail.com"
        }

    val roleLabel = if (isDevAccount) {
        "Developer (${userRole.displayName})"
    } else {
        userRole.displayName
    }

    val hasLocalOverride = state.localPhotoOverrides.containsKey(resolvedUserId)
    val resolvedPhotoUrl = if (hasLocalOverride) {
        state.localPhotoOverrides[resolvedUserId]
    } else {
        matchedUser?.photoUrl?.takeIf { it.isNotBlank() }
    }

    return ProfileIdentityUi(
        userId = resolvedUserId,
        displayName = cleanDisplayName,
        email = resolvedEmail,
        roleLabel = roleLabel,
        initials = buildInitials(cleanDisplayName),
        photoUrl = resolvedPhotoUrl
    )
}

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val mediaUploader: CloudinaryMediaUploader = CloudinaryMediaUploader()
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    private val _effects = Channel<ProfileUiEffect>(Channel.BUFFERED)
    val effects: Flow<ProfileUiEffect> = _effects.receiveAsFlow().shareIn(viewModelScope, SharingStarted.Eagerly)

    init {
        observeUsers()
    }

    private fun observeUsers() {
        viewModelScope.launch {
            userRepository.getAllUsersFlow()
                .catch { /* Fallback gracefully to seeded / local state when offline */ }
                .collect { users ->
                    _state.update { current ->
                        // Once Firestore reflects the updated photoUrl, drop redundant override entry
                        val cleanedOverrides = current.localPhotoOverrides.filter { (uid, overrideUrl) ->
                            val remoteUser = users.firstOrNull { it.id == uid }
                            remoteUser == null || remoteUser.photoUrl != overrideUrl
                        }
                        current.copy(
                            users = users,
                            localPhotoOverrides = cleanedOverrides
                        )
                    }
                }
        }
    }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            ProfileUiEvent.OpenPhotoOptionsSheet -> {
                _state.update { it.copy(showPhotoOptionsSheet = true) }
            }
            ProfileUiEvent.DismissPhotoOptionsSheet -> {
                _state.update { it.copy(showPhotoOptionsSheet = false) }
            }
            ProfileUiEvent.RequestDeletePhoto -> {
                _state.update {
                    it.copy(
                        showPhotoOptionsSheet = false,
                        showDeletePhotoConfirmDialog = true
                    )
                }
            }
            ProfileUiEvent.DismissDeletePhotoConfirm -> {
                _state.update { it.copy(showDeletePhotoConfirmDialog = false) }
            }
            is ProfileUiEvent.UploadProfilePhoto -> {
                uploadAndSaveProfilePhoto(event.userId, event.jpegBytes)
            }
            is ProfileUiEvent.ConfirmDeleteProfilePhoto -> {
                deleteProfilePhoto(event.userId)
            }
            is ProfileUiEvent.PhotoProcessFailed -> {
                _state.update { it.copy(isPhotoUpdating = false, showPhotoOptionsSheet = false) }
                viewModelScope.launch {
                    _effects.send(ProfileUiEffect.ShowSnackbar(event.errorMessage))
                }
            }
        }
    }

    private fun uploadAndSaveProfilePhoto(userId: String, jpegBytes: ByteArray) {
        val targetId = userId.trim().ifBlank { authRepository.getCurrentUserUid() ?: "owner" }
        if (jpegBytes.isEmpty()) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isPhotoUpdating = true,
                    showPhotoOptionsSheet = false
                )
            }

            val uploadResult = mediaUploader.uploadProfilePhoto(
                userId = targetId,
                jpegBytes = jpegBytes,
                folder = CloudinaryMediaUploader.DEFAULT_PROFILE_PHOTO_FOLDER
            )
            if (uploadResult.isFailure) {
                _state.update { it.copy(isPhotoUpdating = false) }
                val detail = uploadResult.exceptionOrNull()?.localizedMessage?.takeIf { it.isNotBlank() }
                    ?: "Gagal mengunggah foto ke Cloudinary."
                _effects.send(ProfileUiEffect.ShowSnackbar(detail))
                return@launch
            }

            val secureUrl = uploadResult.getOrThrow()
            ProfileImageProcessor.cacheJpegForUrl(secureUrl, jpegBytes)

            userRepository.updateUserPhoto(targetId, secureUrl)
                .onSuccess {
                    _state.update { current ->
                        val updatedUsers = current.users.map { u ->
                            if (u.id == targetId) u.copy(photoUrl = secureUrl) else u
                        }
                        current.copy(
                            users = updatedUsers,
                            localPhotoOverrides = current.localPhotoOverrides + (targetId to secureUrl),
                            isPhotoUpdating = false
                        )
                    }
                    _effects.send(ProfileUiEffect.ShowSnackbar("Foto profil berhasil diperbarui."))
                }
                .onFailure { e ->
                    _state.update { it.copy(isPhotoUpdating = false) }
                    val detail = e.localizedMessage?.takeIf { it.isNotBlank() } ?: e.javaClass.simpleName
                    _effects.send(ProfileUiEffect.ShowSnackbar("Gagal menyimpan URL foto profil: $detail"))
                }
        }
    }

    private fun deleteProfilePhoto(userId: String) {
        val targetId = userId.trim().ifBlank { authRepository.getCurrentUserUid() ?: "owner" }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isPhotoUpdating = true,
                    showPhotoOptionsSheet = false,
                    showDeletePhotoConfirmDialog = false
                )
            }
            userRepository.updateUserPhoto(targetId, null)
                .onSuccess {
                    _state.update { current ->
                        val updatedUsers = current.users.map { u ->
                            if (u.id == targetId) u.copy(photoUrl = null) else u
                        }
                        current.copy(
                            users = updatedUsers,
                            localPhotoOverrides = current.localPhotoOverrides + (targetId to null),
                            isPhotoUpdating = false
                        )
                    }
                    _effects.send(ProfileUiEffect.ShowSnackbar("Foto profil berhasil dihapus."))
                }
                .onFailure { e ->
                    _state.update { it.copy(isPhotoUpdating = false) }
                    val detail = e.localizedMessage?.takeIf { it.isNotBlank() } ?: e.javaClass.simpleName
                    _effects.send(ProfileUiEffect.ShowSnackbar("Gagal menghapus foto profil: $detail"))
                }
        }
    }
}
