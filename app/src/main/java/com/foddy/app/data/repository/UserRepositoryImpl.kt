package com.foddy.app.data.repository

import android.content.Context
import com.foddy.app.data.local.UserDao
import com.foddy.app.data.model.UserEntity
import com.foddy.app.domain.model.User
import com.foddy.app.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context
) : UserRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _currentUser = MutableStateFlow<User?>(null)

    init {
        // Listen to Firebase Auth state changes
        firebaseAuth.addAuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                // We will rely on the Firestore sync for the full user object
                syncUserProfile(firebaseUser.email ?: "")
            } else {
                _currentUser.value = null
            }
        }
    }

    override fun getCurrentUser(): Flow<User?> = _currentUser.asStateFlow()

    private fun syncUserProfile(email: String) {
        if (email.isEmpty()) return
        
        firestore.collection("users").document(email)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    // If document doesn't exist yet, create a basic one from Auth
                    val firebaseUser = firebaseAuth.currentUser
                    if (firebaseUser != null && snapshot != null && !snapshot.exists()) {
                        val newUser = User(
                            email = firebaseUser.email ?: "",
                            name = firebaseUser.displayName ?: "User",
                            isLoggedIn = true
                        )
                        saveUserToFirestore(newUser)
                    }
                    return@addSnapshotListener
                }

                val user = User(
                    email = snapshot.getString("email") ?: email,
                    name = snapshot.getString("name") ?: "User",
                    phoneNumber = snapshot.getString("phoneNumber") ?: "",
                    address = snapshot.getString("address") ?: "",
                    profilePictureUrl = snapshot.getString("profilePictureUrl") ?: "",
                    role = snapshot.getString("role") ?: "USER",
                    isLoggedIn = true
                )

                _currentUser.value = user
                
                // Cache to Room
                repositoryScope.launch {
                    userDao.insertUser(
                        UserEntity(
                            email = user.email,
                            name = user.name,
                            phoneNumber = user.phoneNumber,
                            address = user.address,
                            profilePictureUrl = user.profilePictureUrl,
                            role = user.role
                        )
                    )
                }
            }
    }

    private fun saveUserToFirestore(user: User) {
        val userMap = hashMapOf(
            "email" to user.email,
            "name" to user.name,
            "phoneNumber" to user.phoneNumber,
            "address" to user.address,
            "profilePictureUrl" to user.profilePictureUrl,
            "role" to user.role
        )
        firestore.collection("users").document(user.email).set(userMap)
    }

    override suspend fun registerUser(name: String, email: String, password: String): Result<Unit> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            firebaseUser?.updateProfile(profileUpdates)?.await()

            val newUser = User(email = email, name = name, isLoggedIn = true)
            saveUserToFirestore(newUser)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                // Syncing will be handled by the SnapshotListener in init
                Result.success(User(email = email, name = firebaseUser.displayName ?: "", isLoggedIn = true))
            } else {
                Result.failure(Exception("Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(name: String, email: String): Result<Unit> {
        return try {
            val updates = mapOf("name" to name)
            firestore.collection("users").document(email).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
        userDao.clearUser()
        _currentUser.value = null
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                // Logic to check if user exists in Firestore, if not create
                val doc = firestore.collection("users").document(firebaseUser.email!!).get().await()
                if (!doc.exists()) {
                    val newUser = User(
                        email = firebaseUser.email!!,
                        name = firebaseUser.displayName ?: "Google User",
                        isLoggedIn = true,
                        profilePictureUrl = firebaseUser.photoUrl?.toString() ?: ""
                    )
                    saveUserToFirestore(newUser)
                }
                Result.success(User(email = firebaseUser.email!!, name = firebaseUser.displayName ?: "", isLoggedIn = true))
            } else {
                Result.failure(Exception("Google Sign-In failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
