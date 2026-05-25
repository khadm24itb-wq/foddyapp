package com.foddy.app.di

import com.foddy.app.data.repository.MenuRepositoryImpl
import com.foddy.app.data.repository.OrderRepositoryImpl
import com.foddy.app.data.repository.PostRepositoryImpl
import com.foddy.app.data.repository.RestaurantRepositoryImpl
import com.foddy.app.data.repository.UserRepositoryImpl
import com.foddy.app.domain.repository.MenuRepository
import com.foddy.app.domain.repository.OrderRepository
import com.foddy.app.domain.repository.PostRepository
import com.foddy.app.domain.repository.RestaurantRepository
import com.foddy.app.domain.repository.UserRepository
import com.foddy.app.data.repository.NotificationRepositoryImpl
import com.foddy.app.domain.repository.NotificationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindPostRepository(
        postRepositoryImpl: PostRepositoryImpl
    ): PostRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(
        orderRepositoryImpl: OrderRepositoryImpl
    ): OrderRepository

    @Binds
    @Singleton
    abstract fun bindMenuRepository(
        menuRepositoryImpl: MenuRepositoryImpl
    ): MenuRepository

    @Binds
    @Singleton
    abstract fun bindRestaurantRepository(
        restaurantRepositoryImpl: RestaurantRepositoryImpl
    ): RestaurantRepository
}
