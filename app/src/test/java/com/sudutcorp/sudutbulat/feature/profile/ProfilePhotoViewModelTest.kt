package com.sudutcorp.sudutbulat.feature.profile

import com.sudutcorp.sudutbulat.core.util.ProfileImageProcessor
import com.sudutcorp.sudutbulat.core.util.buildInitials
import com.sudutcorp.sudutbulat.data.auth.AuthRepository
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.data.model.User
import com.sudutcorp.sudutbulat.data.remote.CloudinaryMediaUploader
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toFirestoreMap
import com.sudutcorp.sudutbulat.data.repository.FakeUserRemoteDataSource
import com.sudutcorp.sudutbulat.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeCloudinaryMediaUploader : CloudinaryMediaUploader() {
    var shouldFail: Boolean = false
    var lastUploadedUserId: String? = null
    var lastUploadedFolder: String? = null
    var uploadedUrlToReturn: String =
        "https://res.cloudinary.com/fp7jzide/image/upload/v1732781000/pp/pp_staff.jpg"

    override suspend fun uploadProfilePhoto(
        userId: String,
        jpegBytes: ByteArray,
        folder: String
    ): Result<String> = runCatching {
        lastUploadedUserId = userId
        lastUploadedFolder = folder
        if (shouldFail) throw IllegalStateException("Simulasi gagal upload Cloudinary")
        uploadedUrlToReturn
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProfilePhotoViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `buildInitials uses 1 initial for single-word name and first 2 initials for multi-word name`() {
        assertEquals("A", buildInitials("Arif"))
        assertEquals("O", buildInitials("Owner"))
        assertEquals("D", buildInitials("  dimas  "))
        assertEquals("H", buildInitials("Hendra (Owner)"))

        assertEquals("BS", buildInitials("Budi Santoso"))
        assertEquals("DP", buildInitials("Dimas Pratama"))
        assertEquals("DA", buildInitials("Dimas Aditya Pratama"))
        assertEquals("HW", buildInitials("Hendra Wijaya (Owner)"))
        assertEquals("DS", buildInitials("Developer SDB"))

        assertEquals("?", buildInitials("   "))
    }

    @Test
    fun `ProfileViewModel uploads to Cloudinary pp folder, saves secure_url to Firestore, and removes photo`() = runTest {
        val fakeUserDs = FakeUserRemoteDataSource().apply {
            users["staff"] = User(
                id = "staff",
                name = "Dimas",
                email = "sdb.staff@gmail.com",
                role = Role.OUTLET
            )
            users["gudang"] = User(
                id = "gudang",
                name = "Budi Santoso",
                email = "sdb.gudang@gmail.com",
                role = Role.WAREHOUSE
            )
        }
        val fakeUploader = FakeCloudinaryMediaUploader()
        val vm = ProfileViewModel(
            userRepository = UserRepository(fakeUserDs),
            authRepository = AuthRepository(),
            mediaUploader = fakeUploader
        )

        // 1. Before photo is added: single-word "Dimas" uses 1 initial "D" and null photoUrl
        val initialOutletIdentity = resolveProfileIdentity(
            state = vm.state.value,
            userRole = Role.OUTLET,
            userEmail = "sdb.staff@gmail.com",
            isDevAccount = false
        )
        assertEquals("staff", initialOutletIdentity.userId)
        assertEquals("D", initialOutletIdentity.initials)
        assertNull(initialOutletIdentity.photoUrl)

        // Multi-word "Budi Santoso" uses 2 initials "BS"
        val warehouseIdentity = resolveProfileIdentity(
            state = vm.state.value,
            userRole = Role.WAREHOUSE,
            userEmail = "sdb.gudang@gmail.com",
            isDevAccount = false
        )
        assertEquals("BS", warehouseIdentity.initials)

        // 2. Upload failure must NOT overwrite Firestore
        fakeUploader.shouldFail = true
        vm.onEvent(ProfileUiEvent.UploadProfilePhoto(userId = "staff", jpegBytes = byteArrayOf(1, 2, 3)))
        assertFalse(vm.state.value.isPhotoUpdating)
        assertNull(fakeUserDs.users["staff"]?.photoUrl)

        // 3. Upload success writes to Cloudinary folder "pp" and stores secure_url in Firestore
        fakeUploader.shouldFail = false
        vm.onEvent(ProfileUiEvent.OpenPhotoOptionsSheet)
        assertTrue(vm.state.value.showPhotoOptionsSheet)

        vm.onEvent(ProfileUiEvent.UploadProfilePhoto(userId = "staff", jpegBytes = byteArrayOf(1, 2, 3, 4)))

        assertEquals("staff", fakeUploader.lastUploadedUserId)
        assertEquals("pp", fakeUploader.lastUploadedFolder)
        assertFalse(vm.state.value.showPhotoOptionsSheet)
        assertFalse(vm.state.value.isPhotoUpdating)
        assertEquals(fakeUploader.uploadedUrlToReturn, fakeUserDs.users["staff"]?.photoUrl)

        val updatedIdentity = resolveProfileIdentity(
            state = vm.state.value,
            userRole = Role.OUTLET,
            userEmail = "sdb.staff@gmail.com",
            isDevAccount = false
        )
        assertEquals(fakeUploader.uploadedUrlToReturn, updatedIdentity.photoUrl)

        // 4. Delete photo clears photoUrl back to null and restores initials
        vm.onEvent(ProfileUiEvent.RequestDeletePhoto)
        assertTrue(vm.state.value.showDeletePhotoConfirmDialog)

        vm.onEvent(ProfileUiEvent.ConfirmDeleteProfilePhoto(userId = "staff"))
        assertFalse(vm.state.value.showDeletePhotoConfirmDialog)
        assertNull(fakeUserDs.users["staff"]?.photoUrl)

        val afterDeleteIdentity = resolveProfileIdentity(
            state = vm.state.value,
            userRole = Role.OUTLET,
            userEmail = "sdb.staff@gmail.com",
            isDevAccount = false
        )
        assertNull(afterDeleteIdentity.photoUrl)
        assertEquals("D", afterDeleteIdentity.initials)
    }

    @Test
    fun `CloudinaryMediaUploader parses secure_url and error messages and defaults to fp7jzide sdb_dev pp`() {
        assertEquals("fp7jzide", CloudinaryMediaUploader.DEFAULT_CLOUD_NAME)
        assertEquals("sdb_dev", CloudinaryMediaUploader.DEFAULT_UPLOAD_PRESET)
        assertEquals("pp", CloudinaryMediaUploader.DEFAULT_PROFILE_PHOTO_FOLDER)

        val uploader = CloudinaryMediaUploader()
        val okJson = """{"asset_id":"123","public_id":"pp/avatar","secure_url":"https://res.cloudinary.com/fp7jzide/image/upload/v1/pp/avatar.jpg"}"""
        assertEquals(
            "https://res.cloudinary.com/fp7jzide/image/upload/v1/pp/avatar.jpg",
            uploader.parseSecureUrl(okJson)
        )

        val errJson = """{"error":{"message":"Upload preset not found"}}"""
        assertEquals("Upload preset not found", uploader.parseErrorMessage(errJson))
    }

    @Test
    fun `User toFirestoreMap persists Cloudinary photo_url and calculateInSampleSize downscales large camera dimensions`() {
        val cloudinaryUrl = "https://res.cloudinary.com/fp7jzide/image/upload/v1/pp/owner.jpg"
        val userWithPhoto = User(
            id = "owner",
            name = "Arif",
            email = "sdb.owner@gmail.com",
            role = Role.OWNER,
            photoUrl = cloudinaryUrl
        ).toFirestoreMap()
        assertEquals(cloudinaryUrl, userWithPhoto["photo_url"])

        val userWithoutPhoto = User(
            id = "owner",
            name = "Arif",
            email = "sdb.owner@gmail.com",
            role = Role.OWNER,
            photoUrl = "   "
        ).toFirestoreMap()
        assertNull(userWithoutPhoto["photo_url"])

        assertEquals(1, ProfileImageProcessor.calculateInSampleSize(512, 512, 1024))
        assertEquals(2, ProfileImageProcessor.calculateInSampleSize(2400, 1800, 1024))
        assertEquals(4, ProfileImageProcessor.calculateInSampleSize(4096, 3072, 1024))
    }
}
