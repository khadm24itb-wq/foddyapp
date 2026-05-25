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
import javax.inject.Inject

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
            }
        }
    }

    override fun getCurrentUser(): Flow<User?> = _currentUser.asStateFlow()

    private fun syncUserProfile(uid: String) {
        userListener?.remove()
        userListener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    val firebaseUser = firebaseAuth.currentUser
                    if (firebaseUser != null && snapshot != null && !snapshot.exists()) {
                        val newUser = User(
                            id = firebaseUser.uid,
                            email = firebaseUser.email ?: "",
                            name = firebaseUser.displayName ?: "User",
                            isLoggedIn = true
                        )
                        saveUserToFirestore(newUser)
                    }
                    return@addSnapshotListener
                }

                val user = User(
                    id = snapshot.id,
                    email = snapshot.getString("email") ?: "",
                    name = snapshot.getString("name") ?: "User",
                    phoneNumber = snapshot.getString("phoneNumber") ?: "",
                    address = snapshot.getString("address") ?: "",
                    profilePictureUrl = snapshot.getString("profilePictureUrl") ?: "",
                    role = snapshot.getString("role") ?: "USER",
                    isLoggedIn = true
                )

                _currentUser.value = user
                
                repositoryScope.launch {
                    userDao.insertUser(user.toEntity())
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
        firestore.collection("users").document(user.id).set(userMap)
    }

    override suspend fun register(name: String, email: String, password: String): Result<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            
            result.user?.updateProfile(
                userProfileChangeRequest {
                    displayName = name
                }
            )?.await()

            val firebaseUser = result.user ?: return Result.failure(Exception("Register failed"))

            val user = User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                name = firebaseUser.displayName ?: name,
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
            val firebaseUser = result.user ?: return Result.failure(Exception("Login failed"))

            val user = User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                name = firebaseUser.displayName ?: "",
                isLoggedIn = true
            )

            // Re-sync with Firestore to get extra fields
            val doc = firestore.collection("users").document(user.id).get().await()
            val finalUser = if (doc.exists()) {
                user.copy(
                    phoneNumber = doc.getString("phoneNumber") ?: "",
                    address = doc.getString("address") ?: "",
                    role = doc.getString("role") ?: "USER"
                )
            } else {
                saveUserToFirestore(user)
                user
            }

            userDao.insertUser(finalUser.toEntity())
            Result.success(finalUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateName(name: String): Result<Unit> {
        return try {
            firebaseAuth.currentUser?.updateProfile(
                userProfileChangeRequest {
                    displayName = name
                }
            )?.await()
            
            val uid = firebaseAuth.currentUser?.uid
            if (uid != null) {
                firestore.collection("users").document(uid).update("name", name).await()
            }
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
            val firebaseUser = authResult.user ?: return Result.failure(Exception("Google Sign-In failed"))
            
            val doc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val user = if (!doc.exists()) {
                val newUser = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    name = firebaseUser.displayName ?: "Google User",
                    isLoggedIn = true,
                    profilePictureUrl = firebaseUser.photoUrl?.toString() ?: ""
                )
                saveUserToFirestore(newUser)
                newUser
            } else {
                User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    name = doc.getString("name") ?: firebaseUser.displayName ?: "User",
                    isLoggedIn = true,
                    phoneNumber = doc.getString("phoneNumber") ?: "",
                    address = doc.getString("address") ?: "",
                    role = doc.getString("role") ?: "USER"
                )
            }
            
            userDao.insertUser(user.toEntity())
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
