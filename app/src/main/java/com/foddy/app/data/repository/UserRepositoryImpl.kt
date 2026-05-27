package com.foddy.app.data.repository

import com.foddy.app.data.local.UserDao
import com.foddy.app.data.mapper.toEntity
import com.foddy.app.domain.model.User
import com.foddy.app.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _currentUser = MutableStateFlow<User?>(null)
    private var userListener: ListenerRegistration? = null

    init {
        firebaseAuth.addAuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                syncUserProfile(firebaseUser.uid)
            } else {
                _currentUser.value = null
                userListener?.remove()
            }
        }
    }

    override fun getCurrentUser(): Flow<User?> = _currentUser.asStateFlow()

    private fun syncUserProfile(uid: String) {
        userListener?.remove()
        userListener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error listening to user profile")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)?.copy(id = snapshot.id, isLoggedIn = true)
                    _currentUser.value = user
                    
                    user?.let {
                        repositoryScope.launch {
                            userDao.insertUser(it.toEntity())
                        }
                    }
                } else {
                    // Create profile if it doesn't exist (e.g., after Google Sign-in)
                    firebaseAuth.currentUser?.let { firebaseUser ->
                        val newUser = User(
                            id = firebaseUser.uid,
                            email = firebaseUser.email ?: "",
                            name = firebaseUser.displayName ?: "User",
                            avatar = firebaseUser.photoUrl?.toString() ?: "",
                            isLoggedIn = true
                        )
                        saveUserToFirestore(newUser)
                    }
                }
            }
    }

    private fun saveUserToFirestore(user: User) {
        firestore.collection("users").document(user.id).set(user)
            .addOnFailureListener { Timber.e(it, "Failed to save user to Firestore") }
    }

    override suspend fun register(name: String, email: String, password: String): Result<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("Registration failed")
            
            firebaseUser.updateProfile(userProfileChangeRequest { displayName = name }).await()

            val user = User(
                id = firebaseUser.uid,
                email = email,
                name = name,
                role = "USER",
                isLoggedIn = true
            )

            saveUserToFirestore(user)
            userDao.insertUser(user.toEntity())
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("Login failed")

            val doc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val user = if (doc.exists()) {
                doc.toObject(User::class.java)?.copy(id = doc.id, isLoggedIn = true) 
                    ?: throw Exception("User profile data error")
            } else {
                val newUser = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    name = firebaseUser.displayName ?: "User",
                    isLoggedIn = true
                )
                saveUserToFirestore(newUser)
                newUser
            }

            userDao.insertUser(user.toEntity())
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateName(name: String): Result<Unit> {
        return try {
            val uid = firebaseAuth.currentUser?.uid ?: throw Exception("Not logged in")
            firebaseAuth.currentUser?.updateProfile(userProfileChangeRequest { displayName = name })?.await()
            firestore.collection("users").document(uid).update("name", name).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateFcmToken(token: String): Result<Unit> {
        return try {
            val uid = firebaseAuth.currentUser?.uid ?: return Result.success(Unit)
            firestore.collection("users").document(uid).update("fcmToken", token).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        userListener?.remove()
        firebaseAuth.signOut()
        userDao.clearUser()
        _currentUser.value = null
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw Exception("Google Sign-In failed")
            
            val doc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val user = if (!doc.exists()) {
                val newUser = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    name = firebaseUser.displayName ?: "User",
                    avatar = firebaseUser.photoUrl?.toString() ?: "",
                    role = "USER",
                    isLoggedIn = true
                )
                saveUserToFirestore(newUser)
                newUser
            } else {
                doc.toObject(User::class.java)?.copy(id = doc.id, isLoggedIn = true) 
                    ?: throw Exception("User profile data error")
            }
            
            userDao.insertUser(user.toEntity())
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
