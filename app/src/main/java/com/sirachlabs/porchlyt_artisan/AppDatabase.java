package com.sirachlabs.porchlyt_artisan;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import dao.LocationDao;
import dao.appSettingsDao;
import dao.mArtisanDao;
import dao.mArtisanRatingDao;
import dao.mJobsDao;
import dao.mNotificationDao;
import dao.mTaskDao;
import models.appSettings;
import models.mArtisan.mLocation;
import models.mArtisan.artisanRating;
import models.mArtisan.mArtisan;
import models.mJobs.mJobs;
import models.mJobs.mTask;
import models.mNotification;

@Database(version = 13, entities = {
        mTask.class,
        mNotification.class,
        mArtisan.class,
        mJobs.class,
        appSettings.class,
        mLocation.class,
        artisanRating.class
        })

public abstract class AppDatabase extends RoomDatabase {
    abstract public mTaskDao taskDao();
    abstract public appSettingsDao appSettingsDao();
    abstract public LocationDao LocationDao();
    abstract public mArtisanDao mArtisanDao();
    abstract public mJobsDao mJobsDao();
    abstract public mNotificationDao mNotificationDao();
    abstract public mArtisanRatingDao mArtisanRatingDao();
}
