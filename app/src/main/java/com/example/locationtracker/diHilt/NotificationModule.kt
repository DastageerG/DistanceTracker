package com.example.locationtracker.diHilt

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.locationtracker.MainActivity
import com.example.locationtracker.R
import com.example.locationtracker.utils.Constants
import com.example.locationtracker.utils.Constants.PENDING_INTENT_REQUEST_CODE
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(ServiceComponent::class)
object NotificationModule
{


    @ServiceScoped
    @Provides
    fun providePendingIntent(@ApplicationContext context: Context): PendingIntent
    {
        val intent = Intent(context,MainActivity::class.java).apply()
        {
            action = Constants.ACTION_NAVIGATE_TO_MAPS_ACTIVITY
        }
        return PendingIntent.getActivity(context,PENDING_INTENT_REQUEST_CODE,intent,PendingIntent.FLAG_UPDATE_CURRENT)
    } // pending Intent closed



    @ServiceScoped
    @Provides
    fun provideNotificationBuilder(@ApplicationContext context:Context,intent:PendingIntent) : NotificationCompat.Builder =
        NotificationCompat.Builder(context,Constants.NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_baseline_directions_run_24)
            .setContentIntent(intent)


    @ServiceScoped
    @Provides
    fun provideNotificationManager(@ApplicationContext context: Context) : NotificationManager
        =  context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


}